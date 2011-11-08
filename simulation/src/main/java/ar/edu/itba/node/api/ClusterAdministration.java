package ar.edu.itba.node.api;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import ar.edu.itba.node.NodeInformation;

/**
 * @author POD
 * 
 *         Interface used for communication between nodes of a cluster.
 */
public interface ClusterAdministration extends Remote {

	/**
	 * Creates a new cluster group. The node must not be connected to a group,
	 * otherwise, a {@link IllegalStateException} is thrown. After this
	 * invocation, the node is connected to a group.
	 * 
	 * @throws RemoteException
	 */
	void createGroup() throws RemoteException;

	/**
	 * Returns the groupId of the group that the node is connected. If the node
	 * is not connected to a group, null value is returned.
	 * 
	 * @return The id of the group
	 * @throws RemoteException
	 */
	String getGroupId() throws RemoteException;

	/**
	 * @return <code>true</code> if the node is connected to a group
	 * @throws RemoteException
	 */
	boolean isConnectedToGroup() throws RemoteException;

	/**
	 * Connects the current node to a cluster, using one nod information as
	 * entry point. The entry point should be in charge to inform the rest of
	 * the node of the new node.
	 * 
	 * @param host
	 *            the entry point host name.
	 * @param port
	 *            the entry point port
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	void connectToGroup(String host, int port) throws RemoteException, NotBoundException;

	/**
	 * Disconnects node from the rest of the cluster. The receiver should be in
	 * charge to continue the broadcast of the message.
	 * 
	 * @param node
	 *            The node to disconnect
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	void disconnectFromGroup(NodeInformation node) throws RemoteException, NotBoundException;

	/**
	 * Adds a new node to the list of known nodes. The receiver should be in
	 * charge to continue the broadcast of the message.
	 * 
	 * @param node
	 *            The new node
	 * @return {@link Set} with all the known nodes
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	Set<NodeInformation> addNewNode(NodeInformation node) throws RemoteException, NotBoundException;

	/**
	 * Returns a {@link Set} of all the nodes that the current node is connected
	 * to. The {@link Set} can be empty. The {@link Set} might not have all the
	 * nodes of the cluster.
	 * 
	 * @return The connected nodes
	 * @throws RemoteException
	 */
	Set<NodeInformation> connectedNodes() throws RemoteException;

}
