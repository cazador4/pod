package ar.edu.itba.pod.legajo48421.node.api;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.google.common.base.Preconditions;

import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;

public class Host {

	private ClusterAdministration cluster;
	private NodeInformation node;
	private Registry registry;
	//private ClusterAdministration connectedCluster;
	
	public Host(String host, int port, String id) throws RemoteException, AlreadyBoundException {
		node = new NodeInformation(host, port, id);
		cluster = new ClusterAdministrationImpl(node);
		registry = LocateRegistry.createRegistry(port);
		registry.bind(Node.CLUSTER_COMUNICATION, cluster);
	}
	
	public void connect(String host, int port) throws RemoteException, NotBoundException{
		cluster.connectToGroup(host, port);
		Registry connectedRegistry = LocateRegistry.getRegistry(host, port);
		//connectedCluster = (ClusterAdministration)connectedRegistry.lookup(Node.CLUSTER_COMUNICATION);
	}
	
	public ClusterAdministration getCluster(){
		Preconditions.checkNotNull(cluster, "Cluster must not be null");
		return cluster;
	}
	
	public NodeInformation getNodeInformation(){
		Preconditions.checkNotNull(node, "Node Information must not be null");
		return node;
	}
}
