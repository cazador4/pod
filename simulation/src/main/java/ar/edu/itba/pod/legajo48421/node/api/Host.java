package ar.edu.itba.pod.legajo48421.node.api;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.legajo48421.balance.api.AgentsBalancerImpl;
import ar.edu.itba.pod.legajo48421.event.RemoteEventDispatcherImpl;

import com.google.common.base.Preconditions;

public class Host {

	private ClusterAdministration cluster;
	private NodeInformation node;
	private Registry registry;
	private RemoteEventDispatcher remoteEventDispatcher;
	private AgentsBalancer agentsBalancer;
	private NodeInformation coordinator;
	private long timestampCoordinator;

	public AgentsBalancer getAgentsBalancer() {
		return agentsBalancer;
	}

	public Host(String host, int port, String id) throws RemoteException, AlreadyBoundException {
		node = new NodeInformation(host, port, id);
		cluster = new ClusterAdministrationImpl(node);
		remoteEventDispatcher = new RemoteEventDispatcherImpl(node); 
		agentsBalancer = new AgentsBalancerImpl(this);
		registry = LocateRegistry.createRegistry(port);
		registry.bind(Node.CLUSTER_COMUNICATION, cluster);
		registry.bind(Node.DISTRIBUTED_EVENT_DISPATCHER, remoteEventDispatcher);
		registry.bind(Node.AGENTS_BALANCER, agentsBalancer);

		Thread checkConnectedNodes = new Thread(){
			public void run() {
				while(true){
					try {

						Thread.sleep(1000);
						try {

							for(NodeInformation connectedNode : cluster.connectedNodes()){
								try {
									Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
									connectedRegistry.lookup(Node.DISTRIBUTED_EVENT_DISPATCHER);
								} catch (RemoteException e) {
									System.out.println("Node falling down!");
									try {
										cluster.disconnectFromGroup(connectedNode);
										if(connectedNode.equals(coordinator)){
											System.out.println("Coord is down!");
											coordinator=null;
											Set<NodeInformation> connectedNodes = cluster.connectedNodes();
											if(connectedNodes.size()>1){
												boolean flag=true;
												int i=0;
												Object[] arrayNodes= connectedNodes.toArray(); 
												while(flag){
													NodeInformation nodoToSendElection = (NodeInformation) arrayNodes[i];
													if(!node.equals(nodoToSendElection)){
														Registry electionRegistry = LocateRegistry.getRegistry(nodoToSendElection.host(), nodoToSendElection.port());
														final AgentsBalancer electionBalancer = (AgentsBalancer) electionRegistry.lookup(Node.AGENTS_BALANCER);
														Thread newElection = new Thread(){
															public void run(){
																try {
																	electionBalancer.bullyElection(node, System.currentTimeMillis());
																	Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
																	if(coordinator==null){
																		electionBalancer.bullyCoordinator(node, System.currentTimeMillis());
																	}
																} catch (RemoteException e) {
																	e.printStackTrace();
																} catch (InterruptedException e) {
																	e.printStackTrace();
																}
															}
														};
														newElection.start();
														flag=false;

													}
													i++;
												}
											}

										}
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}
								} catch (NotBoundException e) {
									try {
										cluster.disconnectFromGroup(connectedNode);
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}
								}
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		checkConnectedNodes.start();
	}

	public void connect(String host, int port) throws RemoteException, NotBoundException{
		cluster.connectToGroup(host, port);
	}

	public ClusterAdministration getCluster(){
		Preconditions.checkNotNull(cluster, "Cluster must not be null");
		return cluster;
	}

	public NodeInformation getNodeInformation(){
		Preconditions.checkNotNull(node, "Node Information must not be null");
		return node;
	}

	public RemoteEventDispatcher getRemoteEventDispatcher(){
		return remoteEventDispatcher;
	}

	public NodeInformation getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(NodeInformation coordinator) {
		this.coordinator = coordinator;
	}
}
