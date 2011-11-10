package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;

import com.google.common.base.Preconditions;

public class AgentsBalancerImpl implements AgentsBalancer{

	private NodeInformation node;
	private NodeInformation coordinator;
	private long timestampCoordinator;
	private Set<NodeInformation> higherNodes; 


	public AgentsBalancerImpl(NodeInformation node){
		try {
			UnicastRemoteObject.exportObject(this, 0);
			this.node = node;
			higherNodes = new CopyOnWriteArraySet<NodeInformation>();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void bullyElection(NodeInformation node, long timestamp)
			throws RemoteException {
		Preconditions.checkNotNull(node);
		//my id is bigger, Stop the election and start (nodes.count) election/s
		if(Integer.valueOf(node.id())<Integer.valueOf(this.node.id())){
			Registry registry = LocateRegistry.getRegistry(node.host(), node.port());
			AgentsBalancer agentBalancer;
			try {
				agentBalancer = (AgentsBalancer)(registry.lookup(Node.AGENTS_BALANCER));
				agentBalancer.bullyOk(this.node);
				Registry nodeRegistry = LocateRegistry.getRegistry(this.node.host(), this.node.port());
				ClusterAdministration nodeCluster = (ClusterAdministration) nodeRegistry.lookup(Node.CLUSTER_COMUNICATION);
				for(NodeInformation nodeConnected : nodeCluster.connectedNodes()){
					if(!higherNodes.contains(nodeConnected)){
						Registry registryNode = LocateRegistry.getRegistry(nodeConnected.host(), nodeConnected.port());
						try {
							AgentsBalancer agentBalancerNode = (AgentsBalancer)registryNode.lookup(Node.AGENTS_BALANCER);
							agentBalancerNode.bullyElection(this.node, timestamp);
						} catch (NotBoundException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (NotBoundException e1) {
				e1.printStackTrace();
			}
		}
		else{

		}
	}

	@Override
	public void bullyOk(NodeInformation node) throws RemoteException {
		higherNodes.add(node);
	}

	@Override
	public void bullyCoordinator(NodeInformation node, long timestamp)
			throws RemoteException {
		coordinator = node;
		higherNodes.clear();
		Registry nodeRegistry = LocateRegistry.getRegistry(this.node.host(), this.node.port());
		ClusterAdministration nodeCluster;
		try {
			nodeCluster = (ClusterAdministration) nodeRegistry.lookup(Node.CLUSTER_COMUNICATION);
			for(NodeInformation nodeConnected : nodeCluster.connectedNodes()){
				if(nodeConnected!=this.node && coordinator!=this.node && timestamp>timestampCoordinator) {
					Registry registryNode = LocateRegistry.getRegistry(nodeConnected.host(), nodeConnected.port());
					try {
						AgentsBalancer agentBalancerNode = (AgentsBalancer)registryNode.lookup(Node.AGENTS_BALANCER);
						agentBalancerNode.bullyCoordinator(node, timestamp);
					} catch (NotBoundException e) {
						e.printStackTrace();
					}
				}

			}
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		}
		System.out.println("Coord is: " + coordinator);
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
