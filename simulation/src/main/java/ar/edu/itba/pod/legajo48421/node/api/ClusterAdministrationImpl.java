package ar.edu.itba.pod.legajo48421.node.api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;

import com.google.common.base.Preconditions;

public class ClusterAdministrationImpl implements ClusterAdministration{

	private String group;
	private Host host;
	private CopyOnWriteArraySet<NodeInformation> connectedNodes = new CopyOnWriteArraySet<NodeInformation>();

	public ClusterAdministrationImpl(Host host){
		try {
			Preconditions.checkNotNull(host, "Host must not be null");
			this.host = host;
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createGroup() throws RemoteException {
		group = "Godzilla";
		connectedNodes.add(this.host.getNodeInformation());
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
		connectedNodes.add(this.host.getNodeInformation());
		ClusterAdministration connectedCluster = (ClusterAdministration)connectedRegistry.lookup(Node.CLUSTER_COMUNICATION);
		group = connectedCluster.getGroupId();
		connectedNodes.addAll(connectedCluster.addNewNode(this.host.getNodeInformation()));
		
	}

	@Override
	public void disconnectFromGroup(NodeInformation node)
			throws RemoteException, NotBoundException {
		//Preconditions.checkNotNull(group,"Group not exist");
		if(connectedNodes.contains(node)){
			connectedNodes.remove(node);
			for(NodeInformation connectedNode : connectedNodes){
				//if(!connectedNode.equals(this.node)){
					Registry registry = LocateRegistry.getRegistry(connectedNode.host(), connectedNode.port());
					ClusterAdministration connectedCluster = (ClusterAdministration)registry.lookup(Node.CLUSTER_COMUNICATION);
					connectedCluster.disconnectFromGroup(node);
				//}
			}
			if(node.equals(host.getAgentsBalancer().getCoordinator())){
				host.getAgentsBalancer().bullyElection(host.getNodeInformation(), System.currentTimeMillis());
				/*if(connectedNodes.size()>0){
					NodeInformation nodeRandom = connectedNodes.iterator().next();
					Registry reg = LocateRegistry.getRegistry(nodeRandom.host(), nodeRandom.port());
					AgentsBalancer agentsBalancer = (AgentsBalancer)reg.lookup(Node.AGENTS_BALANCER);
					agentsBalancer.bullyElection(host.getNodeInformation(), System.currentTimeMillis());
				}*/
				//host.getAgentsBalancer().bullyElection(host.getNodeInformation(), System.currentTimeMillis());
			}
		}

	}

	@Override
	public Set<NodeInformation> addNewNode(NodeInformation node)
			throws RemoteException, NotBoundException {
		Preconditions.checkNotNull(node, "Node must not be null");
		if(!connectedNodes.contains(node)){
			connectedNodes.add(node);
			for(NodeInformation connectedNode : connectedNodes){
				if(!connectedNode.equals(this.host.getNodeInformation())){
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
