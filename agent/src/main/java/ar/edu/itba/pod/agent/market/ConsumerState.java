package ar.edu.itba.pod.agent.market;

import com.google.common.base.Preconditions;

/**
 * The consumer state
 * @author POD
 *
 */
public class ConsumerState extends NameableAgentState {
	
	private Resource resource;

	/**
	 * @param name
	 * @param resource
	 */
	public ConsumerState(String name, Resource resource) {
		super(name);
		Preconditions.checkNotNull(resource);
		this.resource = resource;
	}

	@Override
	public AgentType agentType() {
		return AgentType.CONSUMER;
	}
	
	/**
	 * @return the resource it consumes 
	 */
	public Resource consumes() {
		return resource;
	}
	
	@Override
	public String toString() {
		return String.format("Consumer agent (%s) - consumes %s", name(), resource);
	}
}
