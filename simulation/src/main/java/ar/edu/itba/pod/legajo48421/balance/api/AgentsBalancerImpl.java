package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.legajo48421.node.api.Constant;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.thread.CleanableThread;

public class AgentsBalancerImpl implements AgentsBalancer{

	private Host host;
	private NodeInformation myNode;
	private AtomicBoolean isOnElection = new AtomicBoolean(false);
	private Map<NodeInformation, Long> elections; //msg elections
	private Map<NodeInformation, Long> coordinators; //msg coordinators
	private AtomicBoolean isOk = new AtomicBoolean(true);

	public AgentsBalancerImpl(Host host){
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		this.host = host;
		myNode = host.getNodeInformation();
		elections = new ConcurrentHashMap<NodeInformation, Long>();
		coordinators = new ConcurrentHashMap<NodeInformation, Long>();
	}

	@Override
	public void bullyElection(final NodeInformation node, final long timestamp)
			throws RemoteException {
		if(checkElection(node, timestamp)){
			//elections.put(node, timestamp);
			//VEO SI TENGO MAYOR ID QUE EL QUE LLEGA
			if(myNode.id().compareTo(node.id())>0){
				isOk.set(true);
				//System.out.println("Hice un ok! al nodo: "+ node);
				if(!isOnElection.getAndSet(true)){
					Thread newElection = new CleanableThread("newElection"){
						public void run(){
							try {
								//System.out.println("Mande una eleccion!");
								getBalancer(node).bullyOk(myNode);
								long timeElection = System.currentTimeMillis();
								for(final NodeInformation connectedNode : host.getCluster().connectedNodes()){
									if(!connectedNode.equals(myNode) && !connectedNode.equals(node)){
										getBalancer(connectedNode).bullyElection(myNode, timeElection);
									}
								}
								try {
									Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
									isOnElection.set(false);
									if(isOk.get()){
//										System.out.println("Seteo que el coord soy yo!");
										long timeCoord=System.currentTimeMillis();
										for(NodeInformation connectedNode2 : host.getCluster().connectedNodes()){
											getBalancer(connectedNode2).bullyCoordinator(myNode, timeCoord);
										}
										host.setCoordinator(myNode);
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} catch (RemoteException e) {
							}
						}
					};
					newElection.start();
				}
				else{
					getBalancer(node).bullyOk(myNode);
				}
			}
			else{
//				System.out.println("Mi nodo es menor");
				Thread broadcastElection = new CleanableThread("broadcastElection") {
					public void run(){
						try {
//							System.out.println("Hago broadCast de la eleccion del nodo: "+ node + " con tiempo "+ timestamp);
							for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
								if(!connectedNode.equals(node) && !connectedNode.equals(myNode)){
									try{
										getBalancer(connectedNode).bullyElection(node, timestamp);
									} catch(RemoteException e1){
										e1.printStackTrace();
									}
								}
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};
				broadcastElection.start();
			}
		}
	}

	private AgentsBalancer getBalancer(NodeInformation node){
		try {
			Registry reg = LocateRegistry.getRegistry(node.host(), node.port());
			return (AgentsBalancer)reg.lookup(Node.AGENTS_BALANCER);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			/*try {
				host.getCluster().disconnectFromGroup(node);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			} catch (NotBoundException e1) {
				e1.printStackTrace();
			}*/
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void bullyOk(NodeInformation node) throws RemoteException {
		isOk.set(false);
		System.out.println("El nodo " + node + " Me hicieron un ok");
	}

	private synchronized boolean checkElection(NodeInformation node, long timestamp){
		/*System.out.println("Llego " + node + " tiempo " + timestamp);
		System.out.println("Size de elections: " + elections.size());
		System.out.println("elections contiene al nodo" + elections.containsKey(node));
		if(elections.containsKey(node)){
			System.out.println("el timestamp es mayor al que ya tengo? " + (elections.get(node).longValue()<timestamp));
			System.out.println(elections.get(node).longValue());
		}*/
		if(!elections.containsKey(node) || elections.get(node)<timestamp)
		{
			elections.put(node, timestamp);
			return true;
		}
		return false;
	}

	public synchronized boolean checkCoordinator(NodeInformation node, long timestamp){
		if(!coordinators.containsKey(node) || coordinators.get(node)<timestamp){
			coordinators.put(node, timestamp);
			return true;
		}
		return false;
	}

	@Override
	public void bullyCoordinator(final NodeInformation node, final long timestamp)
			throws RemoteException {
		if(checkCoordinator(node, timestamp)){
			host.setCoordinator(node);
//			System.out.println("El coordinador es: " + node +" en el tiempo: " + timestamp);
			Thread newCoordinator = new CleanableThread("newCoordinator") {
				public void run(){
					try {
						for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
							if(!connectedNode.equals(node) && !connectedNode.equals(myNode) ){
								try{
									getBalancer(connectedNode).bullyCoordinator(node, timestamp);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			};
			newCoordinator.start();
		}
	}

	@Override
	public void shutdown(List<NodeAgent> agents) throws RemoteException,
	NotCoordinatorException {
	}

	@Override
	public void addAgentToCluster(NodeAgent agent) throws RemoteException,
	NotCoordinatorException {
	}

}
