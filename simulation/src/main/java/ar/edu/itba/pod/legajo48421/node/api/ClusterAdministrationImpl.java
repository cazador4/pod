package ar.edu.itba.pod.legajo48421.node.api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;

public class ClusterAdministrationImpl implements ClusterAdministration{

	private String group;
	private NodeInformation node;
	private Set<NodeInformation> connectedNodes = new HashSet<NodeInformation>();

	public ClusterAdministrationImpl(NodeInformation node){
		try {
			Preconditions.checkNotNull(node, "Node Information must not be null");
			this.node = node;
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createGroup() throws RemoteException {
		group = "Godzilla";
		connectedNodes.add(this.node);
	}

	@Override
	public String getGroupId() throws RemoteException {
		return group;
	}

	@Override
	public boolean isConnectedToGroup() throws RemoteException {
		if(group!=null)
			return true;
		return false;
	}

	@Override
	public void connectToGroup(String host, int port) throws RemoteException,
	NotBoundException {
		Registry connectedRegistry = LocateRegistry.getRegistry(host, port);
		connectedNodes.add(node);
		ClusterAdministration connectedCluster = (ClusterAdministration)connectedRegistry.lookup(Node.CLUSTER_COMUNICATION);
		connectedNodes.addAll(connectedCluster.addNewNode(node));
		group = connectedCluster.getGroupId();
	}

	@Override
	public void disconnectFromGroup(NodeInformation node)
			throws RemoteException, NotBoundException {
		Preconditions.checkNotNull(group,"Group not exist");
		if(connectedNodes.contains(node)){
			connectedNodes.remove(node);
			/*for(NodeInformation connectedNode : connectedNodes){
				if(!connectedNode.equals(this.node)){
					Registry registry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
					ClusterAdministration connectedCluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
					connectedCluster.disconnectFromGroup(node);
				}
			}*/
		}

	}

	@Override
	public Set<NodeInformation> addNewNode(NodeInformation node)
			throws RemoteException, NotBoundException {
		Preconditions.checkNotNull(node, "Node must not be null");
		if(!connectedNodes.contains(node)){
			connectedNodes.add(node);
			for(NodeInformation connectedNode : connectedNodes){
				if(!connectedNode.equals(this.node)){
					Registry connectedRegistry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
					ClusterAdministration connectedCluster = (ClusterAdministration)connectedRegistry.lookup(Node.CLUSTER_COMUNICATION);
					connectedCluster.addNewNode(node);
				}
			}
		}

		return connectedNodes;
	}

	@Override
	public Set<NodeInformation> connectedNodes() throws RemoteException {
		Preconditions.checkNotNull(connectedNodes, "There is not nodes connected");
		return connectedNodes;
	}
}
