package ar.edu.itba.pod.legajo48421.multithread;

import java.io.Serializable;
import java.rmi.RemoteException;

import ar.edu.itba.event.EventInformation;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.multithread.MultiThreadEventDispatcher;

public class ExtendedMultiThreadEventDispatcher extends MultiThreadEventDispatcher{

	private Host host;

	public ExtendedMultiThreadEventDispatcher(Host host){
		this.host = host;
	}

	@Override
	public void publish(Agent source, Serializable event)
			throws InterruptedException {
		synchronized(ExtendedMultiThreadEventDispatcher.class){
			super.publish(source, event);
		}
		EventInformation eventInformation = new EventInformation(event, host.getNodeInformation().id(), source);
		eventInformation.setReceivedTime(System.currentTimeMillis());
		try {
			host.getRemoteEventDispatcher().publish(eventInformation);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void publishIntern(Agent source, Serializable event) throws InterruptedException{
		synchronized(ExtendedMultiThreadEventDispatcher.class){
			super.publish(source, event);
		}
	}

}
