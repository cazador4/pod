package ar.edu.itba.pod.legajo48421.node.api;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ar.edu.itba.event.RemoteEventDispatcher;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.legajo48421.event.RemoteEventDispatcherImpl;

import com.google.common.base.Preconditions;

public class Host {

	private ClusterAdministration cluster;
	private NodeInformation node;
	private Registry registry;
	private RemoteEventDispatcher remoteEventDispatcher;
	//private ClusterAdministration connectedCluster;

	public Host(String host, int port, String id) throws RemoteException, AlreadyBoundException {
		node = new NodeInformation(host, port, id);
		cluster = new ClusterAdministrationImpl(node);
		remoteEventDispatcher = new RemoteEventDispatcherImpl(node); 
		registry = LocateRegistry.createRegistry(port);
		registry.bind(Node.CLUSTER_COMUNICATION, cluster);
		registry.bind(Node.DISTRIBUTED_EVENT_DISPATCHER, remoteEventDispatcher);
		Thread checkConnectedNodes = new Thread(){
			public void run() {
				try {
					Thread.sleep(1000);
					try {
						for(NodeInformation connectedNode : cluster.connectedNodes()){
							try {
								Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
							} catch (RemoteException e) {
								System.out.println("Node falling down!");
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
		};
		checkConnectedNodes.start();
	}

	public void connect(String host, int port) throws RemoteException, NotBoundException{
		cluster.connectToGroup(host, port);
		Registry connectedRegistry = LocateRegistry.getRegistry(host, port);
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
}
