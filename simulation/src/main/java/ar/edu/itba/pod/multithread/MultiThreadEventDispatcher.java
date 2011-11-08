package ar.edu.itba.pod.multithread;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.agent.runner.TargetedEvent;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Event dispatcher
 */
public class MultiThreadEventDispatcher implements EventDispatcher {
	protected static final int QUEUE_SIZE = 256;
	
	private SetMultimap<Class, Agent> listeners = Multimaps.synchronizedSetMultimap(HashMultimap.<Class, Agent>create());
	private Map<Agent, BlockingQueue<Object>> queues = new MapMaker()
		.makeComputingMap(new Function<Agent, BlockingQueue<Object>>() { 
			@Override
			public BlockingQueue<Object> apply(Agent input) {
				return new LinkedBlockingQueue<Object>(QUEUE_SIZE);
			}
		 });
	
	
	@Override
	public void register(Agent agent, Class<? extends Serializable> type) {
		listeners.put(type, agent);
	}
	
	@Override
	public void deregister(Agent agent, Class<? extends Serializable> type) {
		listeners.remove(type, agent);
		
	}
	
	/**
	 * Unregister an agent
	 * @param agent
	 * @return the pending events
	 */
	public BlockingQueue<Object> deregister(Agent agent) {
		Set<Entry<Class, Agent>> entries = listeners.entries();
		
		synchronized (listeners) {
			Iterator<Entry<Class, Agent>> entriesIter = entries.iterator();
			while(entriesIter.hasNext()) {
				Entry<Class, Agent> entry = entriesIter.next();
				if (agent.equals(entry.getValue())) {
					entriesIter.remove();
				}
			}
			BlockingQueue<Object> pendingEvents = queues.get(agent);
			queues.remove(agent);
			return pendingEvents;
		}
	}
	
	@Override
	public void publish(Agent source, Serializable event) throws InterruptedException {
		String target = (event instanceof TargetedEvent) ? ((TargetedEvent)event).target() : null;

		Set<Agent> agents = listeners.get(event.getClass());
		for (Agent agent : agents) {
			if (target == null || target.equals(agent.name())) {
				BlockingQueue<Object> queue = queues.get(agent);
				queue.put(event);
			}
		}
	}
	
	/**
	 * Returns the next event on the event queue, or waits for a set amount of time for an event to appear.
	 * If no event appears after timeout, it will be  
	 * @throws InterruptedException if there is an interruption while waiting
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T waitFor(Agent agent, Class<T> baseType) throws InterruptedException {
		return (T) queues.get(agent).take();
	}

	/**
	 * Sets a predefined events queue for an agent
	 * @param agent
	 * @param queue
	 */
	public void setAgentQueue(Agent agent, BlockingQueue<Object> queue) {
		queues.put(agent, queue);
	}
	
}
