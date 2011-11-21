package ar.edu.itba.pod.legajo48421.node.api;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.legajo48421.balance.api.AgentsBalancerImpl;
import ar.edu.itba.pod.legajo48421.balance.api.AgentsTransferImpl;
import ar.edu.itba.pod.legajo48421.event.RemoteEventDispatcherImpl;
import ar.edu.itba.pod.legajo48421.multithread.ClusterSimulation;
import ar.edu.itba.pod.legajo48421.multithread.ExtendedMultiThreadEventDispatcher;
import ar.edu.itba.pod.time.TimeMappers;

import com.google.common.base.Preconditions;

public class Host {

	private ClusterAdministration cluster;
	private NodeInformation node;
	private Registry registry;
	private RemoteEventDispatcher remoteEventDispatcher;
	private AgentsBalancerImpl agentsBalancer;
	private AgentsTransfer agentsTransfer;
	//private NodeInformation coordinator;
	private ExtendedMultiThreadEventDispatcher extendedMultiThreadEventDispatcher;
	private ClusterSimulation simulation;
	private Map<NodeInformation, Integer> nodeCountAgentMap = new HashMap<NodeInformation, Integer>();
	
	public Host(String host, int port, String id) throws RemoteException, AlreadyBoundException {
		registry = LocateRegistry.createRegistry(port);
		node = new NodeInformation(host, port, host+":"+port);
		cluster = new ClusterAdministrationImpl(this);
		remoteEventDispatcher = new RemoteEventDispatcherImpl(this); 
		agentsBalancer = new AgentsBalancerImpl(this);
		extendedMultiThreadEventDispatcher = new ExtendedMultiThreadEventDispatcher(this);
		agentsTransfer = new AgentsTransferImpl(this);
		registry.bind(Node.CLUSTER_COMUNICATION, cluster);
		registry.bind(Node.DISTRIBUTED_EVENT_DISPATCHER, remoteEventDispatcher);
		registry.bind(Node.AGENTS_BALANCER, agentsBalancer);
		registry.bind(Node.AGENTS_TRANSFER, agentsTransfer);
		
		
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
										if(connectedNode.equals(agentsBalancer.getCoordinator())){
											System.out.println("Coord is down!");
											//coordinator=null;
											Set<NodeInformation> connectedNodes = cluster.connectedNodes();
											if(connectedNodes.size()>1){
												boolean flag=true;
												List<NodeInformation> list = new ArrayList<NodeInformation>(connectedNodes);
												Collections.shuffle(list);
												while(flag){
													if(list.size()>0){
														NodeInformation nodoToSendElection = list.remove(0);
													//if(!node.equals(nodoToSendElection)){
														Registry electionRegistry = LocateRegistry.getRegistry(nodoToSendElection.host(), nodoToSendElection.port());
														final AgentsBalancer electionBalancer = (AgentsBalancer) electionRegistry.lookup(Node.AGENTS_BALANCER);
														Thread newElection = new Thread(){
															public void run(){
																try {
																	electionBalancer.bullyElection(node, System.currentTimeMillis());
																	Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
																	if(agentsBalancer.getCoordinator()==null){
																		electionBalancer.bullyCoordinator(node, System.currentTimeMillis());
																	}
																} catch (RemoteException e2) {
																	e2.printStackTrace();
																} catch (InterruptedException e3) {
																	e3.printStackTrace();
																}
															}
														};
														newElection.start();
														flag=false;
													}
													//}
												}
											}

										}
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}
								} catch (NotBoundException e) {
									/*try {
										cluster.disconnectFromGroup(connectedNode);
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}*/
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

	/*public NodeInformation getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(NodeInformation coordinator) {
		this.coordinator = coordinator;
	}*/
	
	public ExtendedMultiThreadEventDispatcher getExtendedMultiThreadEventDispatcher() {
		return extendedMultiThreadEventDispatcher;
	}

	/*public synchronized boolean isCoordinator(){
		return node.equals(coordinator);
	}*/
	
	public AgentsBalancerImpl getAgentsBalancer() {
		return agentsBalancer;
	}

	public AgentsTransfer getAgentsTransfer() {
		return agentsTransfer;
	}

	public void setSimulation(ClusterSimulation clusterSimulation){
		this.simulation = clusterSimulation;
	}
	
	public ClusterSimulation getSimulation() {
		return simulation;
	}
}
