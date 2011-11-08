package ar.edu.itba.balance.api;

import ar.edu.itba.node.NodeInformation;

/**
 * Thrown by a node when a coordinator method is call and it is not the current
 * coordinator. Additional information about the current coordinator is offered. 
 * @author POD
 *
 */
public class NotCoordinatorException extends Exception {
	private NodeInformation newCoordinator;

	/**
	 * @param newCoordinator the current coordinator
	 */
	public NotCoordinatorException(final NodeInformation newCoordinator) {
		super();
		this.newCoordinator = newCoordinator;
	}
	
	/**
	 * @return the current coordinator
	 */
	public NodeInformation getNewCoordinator() {
		return newCoordinator;
	}
	
	/** @see java.lang.Throwable#getMessage()*/
	@Override
	public String getMessage() {
		return "this is not the coordinator anymore. New coordinator is:" + newCoordinator;
	}
}
