package ar.edu.itba.pod.agent.market;

import com.google.common.base.Preconditions;

public class ProducerState extends NameableAgentState {

	private final Resource resource;

	/**
	 * @param name
	 * @param resource
	 */
	public ProducerState(String name, Resource resource) {
		super(name);
		Preconditions.checkNotNull(resource);
		this.resource = resource;
	}

	@Override
	public AgentType agentType() {
		return AgentType.PRODUCER;
	}
	
	/**
	 * @return the produced resource
	 */
	public Resource resource() {
		return resource;
	}
	@Override
	public String toString() {
		return String.format("Producer agent (%s) - produces %s", name(), resource);
	}
}
