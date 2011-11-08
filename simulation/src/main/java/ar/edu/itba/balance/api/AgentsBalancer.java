package ar.edu.itba.balance.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ar.edu.itba.node.NodeInformation;

/**
 * Agents Load balancing interface.
 * Bully algorithm is used in order to choose a coordinator in charge of the load balancing.
 */
public interface AgentsBalancer extends Remote {
	/**
	 * Called when a node initiates an election. If the node who receives the call has
	 * an id that is less than the received id. Otherwise it should call bullyOk on the
	 * election node and start a new election in order to become the coordinator
	 * As this event should be broadcasted, this method should replicate the call
	 * to all its connected nodes. 
	 * @param node the node who wants to become a coordinator
	 * @param timestamp The node timestamp. Useful for broadcasting methods.
	 */
	void bullyElection(NodeInformation node, long timestamp) throws RemoteException;
	/**
	 * Called when this node has initiated an election and another node "bullies" it.
	 * This node should stop its attempt to become a coordinator.
	 * This call should not be broadcasted as it is only intended to stop this node
	 * attempt to become the coordinator.
	 * @param node the bully node
	 */
	void bullyOk(NodeInformation node) throws RemoteException;
	/**
	 * Call when a node becomes the coordinator.
	 * As this event should be broadcasted, this method should replicate the call
	 * to all its connected nodes. 
	 * @param node coordinator node
	 * @param timestamp The node timestamp. Useful for broadcasting methods.
	 */
	void bullyCoordinator(NodeInformation node, long timestamp) throws RemoteException;
	
	/**
	 * Call when a node wants to inform the coordinator that it will shutdown, so its agents
	 * should be redistributed. This node could not be the coordinator anymore, due to 
	 * synchronization times, so it can throw an exception.
	 * @param agents agents to redistribute
	 * @throws NotCoordinatorException if this is not the current coordinator. The exception
	 * contains information about the new coordinator.
	 */
	void shutdown(List<NodeAgent> agents) throws RemoteException, NotCoordinatorException;
	/**
	 * Adds an agent to the cluster so it can be balanced. This method should be called
	 * on the coordinator. So if this node is not the current one, it throws an exception.
	 * @param agent to balance.
	 * @throws NotCoordinatorException if this is not the current coordinator. The exception
	 * contains information about the new coordinator.
	 */
	void addAgentToCluster(NodeAgent agent) throws RemoteException, NotCoordinatorException;
}