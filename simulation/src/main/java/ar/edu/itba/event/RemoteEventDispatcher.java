package ar.edu.itba.event;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;

/**
 * Dispatches events between nodes
 * @author POD
 * 
 */
public interface RemoteEventDispatcher extends Remote {

	/**
	 * Publish a new {@link EventInformation} in the target node. Target node
	 * should be in charge to continue the broadcast of the event if necessary.
	 * 
	 * @param event
	 *            the new event.
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @return true if this is a new event for this node, false otherwise
	 */
	boolean publish(EventInformation event) throws RemoteException, InterruptedException;

	/**
	 * Finds and returns all the new events for the receiving node. Messages
	 * that had already been sent to the receiving node should not appear in the
	 * returned {@link Set}
	 * 
	 * @param nodeInformation
	 *            the receiving node
	 * @return {@link Set} with the messages that had not been sent to the
	 *         receiving node
	 * @throws RemoteException
	 */
	Set<EventInformation> newEventsFor(NodeInformation nodeInformation) throws RemoteException;

	/**
	 * Moves the events queue for an agent between nodes. This method should be used with
	 * AgentsTransfer#stopAndGet, so the agent should be stopped and moved before its event queue
	 * is migrated. Before calling this method a proper event synchronization between both
	 * nodes should be performed in order to avoid losing events.
	 * 
	 * @param agent
	 *            the agent whose event queue is expected
	 * @return the agent queue for this agent. Never returns null.
	 */
	BlockingQueue<Object> moveQueueFor(Agent agent) throws RemoteException;
}
