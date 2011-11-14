package ar.edu.itba.pod.legajo48421.event;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.event.EventInformation;
import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.multithread.MsgError;

public class RemoteEventDispatcherImpl implements RemoteEventDispatcher {


	private final BlockingQueue<Object> queue;
	private final NodeInformation node;
	ConcurrentMap<EventInformation, Long> processingEvents;
	  


	public RemoteEventDispatcherImpl(final NodeInformation node){
		
		this.queue = new LinkedBlockingQueue<Object>();
		this.node = node;
		processingEvents = new ConcurrentHashMap<EventInformation, Long>(); 
	
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		Thread getQueueEvent = new Thread(){
			@Override
			public void run() {
				while(true){
					Object event;
					try {
						event = queue.take();
						if(!processingEvents.containsKey(event)){
							System.out.println(event);
							processingEvents.put((EventInformation)event, System.currentTimeMillis());
							Registry registry = LocateRegistry.getRegistry(node.host(), node.port());
							ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
							int countFalse = 0;
							//TODO ver cuantos false recibo del publish para no seguir mandando!
							Set<NodeInformation> connectedNodes = cluster.connectedNodes();
							for(NodeInformation connectedNode : connectedNodes){
								if(countFalse<=connectedNodes.size()/2){
									if(!connectedNode.equals(node)){
										Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
										RemoteEventDispatcher remoteEventDispatcher = (RemoteEventDispatcher)connectedRegistry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
										boolean answer = remoteEventDispatcher.publish((EventInformation)event);
										if(!answer)
											countFalse++;
									}
								}
							}
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (NotBoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		getQueueEvent.start();

		Thread getNewEvent = new Thread(){
			@Override
			public void run() {
				while(true){
					Registry registry;
					try {
						Thread.sleep(5000);
						registry = LocateRegistry.getRegistry(node.host(), node.port());
						ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
						Random random = new Random();
						Set<NodeInformation> nodes = cluster.connectedNodes();
						if(nodes.size()>1){
							int position = random.nextInt(nodes.size()-1);
							//for(NodeInformation connectedNode : cluster.connectedNodes()){
								NodeInformation connectedNode = (NodeInformation)nodes.toArray()[position];
								if(!connectedNode.equals(node)){
									registry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
									RemoteEventDispatcher connectedEventDispatcher = (RemoteEventDispatcher)registry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
									Set<EventInformation> newEvents = connectedEventDispatcher.newEventsFor(node);
									queue.addAll(newEvents);
									System.out.println("NEW EVENTS: " + newEvents);
								}
							//}
						}
					} catch (RemoteException e) {
						System.out.println(MsgError.CONNECTION_ERROR	);
					} catch (NotBoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		};
		getNewEvent.start();
	}

	@Override
	public boolean publish(EventInformation event) throws RemoteException,
	InterruptedException {
		if(!queue.contains(event) && !processingEvents.containsKey(event)){
			queue.add(event);
			return true;
		}
		return false;
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
		return null;
	}

}
