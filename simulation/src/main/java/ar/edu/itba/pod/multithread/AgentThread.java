package ar.edu.itba.pod.multithread;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.agent.runner.Environment;
import ar.edu.itba.pod.agent.runner.FinishException;
import ar.edu.itba.pod.agent.runner.SuspendException;
import ar.edu.itba.pod.time.TimeMapper;

import com.google.common.base.Preconditions;

/**
 * Thread that runs an agent
 */
public class AgentThread extends Thread implements Environment {
	private final EventDispatcher dispatcher;
	private final TimeMapper mapper;
	private final Agent agent;
	
	private AtomicBoolean stop = new AtomicBoolean();
	private AtomicBoolean suspend = new AtomicBoolean();
	
	public AgentThread(TimeMapper mapper, EventDispatcher dispatcher, Agent agent) {
		super(agent.name());
		this.mapper = mapper;
		this.dispatcher = dispatcher;
		this.agent = agent;
	}
	
	@Override
	public void run() {
		stop.set(false);
		suspend.set(false);
		
		try {
			agent.beforeStart(this);
			
			while(true) {
				agent.execute(this);
				checkState();
			}
		}
		catch (FinishException e) {
			return;
		}
		catch (SuspendException e) {
			return;
		}
	}
	
	/**
	 * Signal the agent to finish. Doesn't wait for the agent to finish
	 */
	public void finishAgent() {
		Preconditions.checkState(stop.compareAndSet(false, true), "Thread already stopping or stopped!!");
		interrupt();
	}
	
	/**
	 *  Signal the agent to suspend it's execution. Doesn't wait for the agent to finish
	 */
	public void suspendAgent() {
		Preconditions.checkState(suspend.compareAndSet(false, true), "Thread already suspended!!");
		interrupt();
	}

	
	
	@Override
	public void checkState() {
		if (stop.get()) {
			throw new FinishException();
		}
		if (suspend.get()) {
			throw new SuspendException();
		}
		
	}
	
	@Override
	public void wait(Duration duration) {
		Preconditions.checkArgument(!Duration.ZERO.equals(duration));
		Preconditions.checkNotNull(duration, "A duration must be specified!");

		DateTime end = DateTime.now().plus(mapper.toMillis(duration));
		while (end.isAfterNow()) {
			try {
				Thread.sleep(new Duration(null, end).getMillis());
			} catch (InterruptedException e) {
				checkState();
			}
		}
	}
	
	@Override
	public void listenTo(Class<? extends Serializable> eventType) {
		dispatcher.register(agent, eventType);
	}
	
	@Override
	public void dontListenTo(Class<? extends Serializable> eventType) {
		dispatcher.deregister(agent, eventType);
		
	}
	
	@Override
	public void publish(Serializable event) {
		while(true) {
			try {
				dispatcher.publish(agent, event);
				return;
			} catch (InterruptedException e) {
				checkState();
			}
		}
	}
	
	@Override
	public <T extends Serializable> T waitFor(Class<T> baseType) {
		while(true) {
			try {
				return dispatcher.waitFor(agent, baseType);
			} catch (InterruptedException e) {
				checkState();
			}
		}
	}

	public boolean isRunning(Agent agent) {
		return this.agent.equals(agent);
	}
	
	public Agent getAgent() {
		return agent;
	}
}
