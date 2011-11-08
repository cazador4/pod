package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

import ar.edu.itba.pod.agent.runner.TargetedEvent;
import ar.edu.itba.pod.doc.Immutable;

/**
 * Event sent by markets to agents that reuested resources to transfer them
 */
@Immutable
public final class ResourceTransfer implements TargetedEvent, Serializable {
	private final String target;
	private final Resource resource;
	private final int amount;

	public ResourceTransfer(String target, Resource resource, int amount) {
		super();
		this.target = target;
		this.resource = resource;
		this.amount = amount;
	}
	
	@Override
	public String target() {
		return target;
	}
	
	public Resource resource() {
		return resource;
	}
	
	public int amount() {
		return amount;
	}
}
