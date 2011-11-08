package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.doc.Immutable;

/**
 * Event sent by agents that require a specific amount of resources
 */
@Immutable
public class Requires implements Serializable {
	private final String source;
	private final Resource resource;
	private final int amount;

	public Requires(String source, Resource resource, int amount) {
		super();
		this.source = source;
		this.resource = resource;
		this.amount = amount;
	}
	
	public Resource resource() {
		return resource;
	}
	
	public int amount() {
		return amount;
	}
	
	public String source() {
		return source;
	}

	/**
	 * Returns a new order for the remaining resources 
	 */
	public Requires remaining(int amount) {
		Preconditions.checkState(this.amount > amount, "Amount requested (%d) exeeds current order amount (%d)", amount, this.amount);
		return new Requires(source, resource, this.amount - amount);
	}
}
