package ar.edu.itba.node.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface used for node statistics.
 * @author POD
 *
 */
public interface StatisticReports extends Remote {
	/**
	 * @return the node statistics
	 */
	NodeStatistics getNodeStatistics() throws RemoteException;
}
