package ar.edu.itba.pod.legajo48421.event;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ar.edu.itba.event.EventInformation;
import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.thread.CleanableThread;

public class RemoteEventDispatcherImpl implements RemoteEventDispatcher {


	private final BlockingQueue<EventInformation> queue;
	private final Host host;
	ConcurrentMap<EventInformation, Long> processingEvents;
	ConcurrentMap<NodeInformation, Long> lastTimeSendEvent;
	private ReentrantReadWriteLock lock;


	public RemoteEventDispatcherImpl(final Host host){

		this.queue = new LinkedBlockingQueue<EventInformation>();
		this.host = host;
		processingEvents = new ConcurrentHashMap<EventInformation, Long>(); 
		lastTimeSendEvent = new ConcurrentHashMap<NodeInformation, Long>();
		lock = new ReentrantReadWriteLock(true);
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		Thread getQueueEvent = new CleanableThread("getQueueEvent") {
			@Override
			public void run() {
				while(true){
					EventInformation eventInformation;
					try {
						eventInformation = queue.take();
						processingEvents.put((EventInformation)eventInformation, System.currentTimeMillis());
						
						host.getExtendedMultiThreadEventDispatcher().publishIntern(eventInformation.source(), eventInformation.event());
						Registry registry = LocateRegistry.getRegistry(host.getNodeInformation().host(), host.getNodeInformation().port());
						ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
						int countFalse = 0;
						//ver cuantos false recibo del publish para no seguir mandando!
						Set<NodeInformation> connectedNodes = cluster.connectedNodes();
						for(NodeInformation connectedNode : connectedNodes){
							if(countFalse<=connectedNodes.size()/2){
								if(!connectedNode.equals(host.getNodeInformation())){
									Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
									RemoteEventDispatcher remoteEventDispatcher = (RemoteEventDispatcher)connectedRegistry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
									lastTimeSendEvent.put(connectedNode, System.currentTimeMillis());
									boolean answer = remoteEventDispatcher.publish((EventInformation)eventInformation);
									if(!answer)
										countFalse++;
								}
							}
						}
					} catch (RemoteException e) {
						//e.printStackTrace();
					} catch (NotBoundException e) {
						//e.printStackTrace();
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}
			}
		};
		getQueueEvent.start();

		Thread getNewEvent = new CleanableThread("getNewEvent"){
			@Override
			public void run() {
				while(true){
					Registry registry;
					NodeInformation connectedNode=null;
					try {
						Thread.sleep(5000);
						registry = LocateRegistry.getRegistry(host.getNodeInformation().host(), host.getNodeInformation().port());
						ClusterAdministration cluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
						Random random = new Random();
						Set<NodeInformation> nodes = cluster.connectedNodes();
						if(nodes.size()>1){
							int position = random.nextInt(nodes.size()-1);
							connectedNode = (NodeInformation)nodes.toArray()[position];
							if(!connectedNode.equals(host.getNodeInformation())){
								registry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
								RemoteEventDispatcher connectedEventDispatcher = (RemoteEventDispatcher)registry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
								Set<EventInformation> newEvents = connectedEventDispatcher.newEventsFor(host.getNodeInformation());
								queue.addAll(newEvents);
							}
						}
					} catch (RemoteException e) {
						try {
							host.getCluster().disconnectFromGroup(connectedNode);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						} catch (NotBoundException e1) {
							e1.printStackTrace();
						}
					} catch (NotBoundException e) {
						try {
							host.getCluster().disconnectFromGroup(connectedNode);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						} catch (NotBoundException e1) {
							e1.printStackTrace();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		};
		getNewEvent.start();
		
		Thread cleanProcessingEvents = new CleanableThread("cleanProcessingEvents") {
			public void run(){
				while(true){
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					processingEvents.clear();
				}
			}
		};
		cleanProcessingEvents.start();
	}

	@Override
	public boolean publish(final EventInformation event) throws RemoteException,
	InterruptedException {

		boolean result = false;
		
		getLock().readLock().lock();
		if(!queue.contains(event) && !processingEvents.containsKey(event)){
			queue.add(event);
			result = true;
		}
		getLock().readLock().unlock();
		
		return result;
	}

	private Set<EventInformation> findInQueue(String nodeId, long timestamp) {
		Set<EventInformation> result = new HashSet<EventInformation>();
		for(Entry<EventInformation, Long> entry : processingEvents.entrySet()){
			if(timestamp<entry.getValue()){
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public synchronized ReentrantReadWriteLock getLock(){
		return lock;
	}
	
	@Override
	public Set<EventInformation> newEventsFor(NodeInformation nodeInformation)
			throws RemoteException {
		Long timestamp = lastTimeSendEvent.get(nodeInformation);
		if(timestamp==null)
			timestamp=0l;
		return findInQueue(nodeInformation.id(), timestamp);
	}

	public BlockingQueue<EventInformation> getPendingEvents(){
		return queue;
	}
	
	@Override
	public BlockingQueue<Object> moveQueueFor(Agent agent)
			throws RemoteException {
		return host.getExtendedMultiThreadEventDispatcher().deregister(agent);
	}

}
