package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.prefs.NodeChangeEvent;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;

import com.google.common.base.Preconditions;

public class AgentsBalancerImpl implements AgentsBalancer{

	private NodeInformation node;
	private ClusterAdministration cluster;
	private NodeInformation coordinator;

	public AgentsBalancerImpl(NodeInformation node, ClusterAdministration cluster){
		this.node = node;
		this.cluster = cluster;
	}

	@Override
	public void bullyElection(NodeInformation node, long timestamp)
			throws RemoteException {
		Preconditions.checkNotNull(node);
		//my id is bigger, Stop the election and start (nodes.count) election/s
		if(Integer.valueOf(node.id())<Integer.valueOf(this.node.id())){
			bullyOk(node);
			for(NodeInformation nodeConnected : cluster.connectedNodes()){
				Registry registryNode = LocateRegistry.getRegistry(nodeConnected.host(), nodeConnected.port());
				try {
					AgentsBalancer agentBalancerNode = (AgentsBalancer)registryNode.lookup(Node.AGENTS_BALANCER);
					agentBalancerNode.bullyElection(this.node, timestamp);
				} catch (NotBoundException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public void bullyOk(NodeInformation node) throws RemoteException {
		//que hace esto!
	}

	@Override
	public void bullyCoordinator(NodeInformation node, long timestamp)
			throws RemoteException {
		coordinator = node;
		for(NodeInformation nodeConnected : cluster.connectedNodes()){
			if(nodeConnected!=this.node) {
				Registry registryNode = LocateRegistry.getRegistry(nodeConnected.host(), nodeConnected.port());
				try {
					AgentsBalancer agentBalancerNode = (AgentsBalancer)registryNode.lookup(Node.AGENTS_BALANCER);
					agentBalancerNode.bullyCoordinator(node, timestamp);
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}

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
