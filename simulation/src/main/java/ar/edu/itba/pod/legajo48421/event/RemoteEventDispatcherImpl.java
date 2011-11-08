package ar.edu.itba.pod.legajo48421.event;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.event.EventInformation;
import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;

public class RemoteEventDispatcherImpl implements RemoteEventDispatcher {

	
	private BlockingQueue<Object> queue;
	
	public RemoteEventDispatcherImpl(){
		queue = new LinkedBlockingQueue<Object>();
	}
	
	@Override
	public boolean publish(EventInformation event) throws RemoteException,
			InterruptedException {
		boolean result = findInQueue(event.nodeId()).isEmpty();
		queue.add(event);
		return result;
	}

	private Set<EventInformation> findInQueue(String nodeId) {
		Set<EventInformation> result = new HashSet<EventInformation>();
		for(Object event : queue){
			EventInformation eventInfo = (EventInformation)event;
			if(eventInfo.nodeId().equals(nodeId))
				result.add(eventInfo);
		}
		return result;
	}

	@Override
	public Set<EventInformation> newEventsFor(NodeInformation nodeInformation)
			throws RemoteException {
		
		return findInQueue(nodeInformation.id());
	}

	@Override
	public BlockingQueue<Object> moveQueueFor(Agent agent)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
