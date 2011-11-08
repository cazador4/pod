package ar.edu.itba.pod.agent.market;

import static com.google.common.base.Preconditions.checkNotNull;
import ar.edu.itba.pod.agent.runner.Agent;

/**
 * Agent template for agents that deal with a resource
 */
public abstract class ResourceAgent extends Agent {
	private final Resource resource;
	
	public ResourceAgent(String name, Resource resource) {
		super(name);
		checkNotNull(resource, "Resource cannot be null");
		this.resource = resource;
	}
	
	protected final Resource resource() {
		return this.resource;
	}
}
