package ar.edu.itba.balance.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Used for transfering agents between nodes
 */
public interface AgentsTransfer extends Remote {
	
	/**
	 * Run these aqents on this node 
	 * @param agents agents to run
	 */
	void runAgentsOnNode(List<NodeAgent> agents) throws RemoteException;
	/**
	 * @return the number of agents running on this node.
	 */
	int getNumberOfAgents() throws RemoteException;
	
	/**
	 * Stops and return a number of agents for redistribution. This node should still
	 * stores events for this event until a proper events synchronization with the destination
	 * node is done through RemoteEventDispatcher#moveQueueFor.
	 * @return numberOfAgents agents to stop and return.
	 */
	List<NodeAgent> stopAndGet(int numberOfAgents) throws RemoteException;
}
