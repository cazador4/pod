package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

import com.google.common.base.Preconditions;


/**
 * Represents a transfer done in a node
 * 
 * @author POD
 */
public class TransferHistoryItem implements Serializable {
	private static final long serialVersionUID = -5547373740419034073L;
	private Resource resource;
	private int amount;

	/**
	 * @param resource
	 *            transfered resource
	 * @param amount
	 *            amount of transfered resource
	 */
	public TransferHistoryItem(Resource resource, int amount) {
		Preconditions.checkNotNull(resource, "resource could not be null");
		Preconditions.checkArgument(amount > 0, "amount should be > 0");

		this.resource = resource;
		this.amount = amount;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
}
