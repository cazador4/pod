package ar.edu.itba.pod.agent.market;

import java.util.List;

import com.google.common.base.Preconditions;

public class FactoryState extends NameableAgentState  {

	private List<Resource> requirements;
	private Resource resource;
	
	/**
	 * @param name
	 * @param requirements
	 * @param resource
	 */
	public FactoryState(String name, List<Resource> requirements, Resource resource) {
		super(name);
		Preconditions.checkNotNull(requirements);
		Preconditions.checkNotNull(resource);
		this.requirements = requirements;
		this.resource = resource;
	}

	@Override
	public AgentType agentType() {
		return AgentType.FACTORY;
	}
	
	/**
	 * @return the production requirements
	 */
	public List<Resource> requirements() {
		return requirements;
	}
	
	/**
	 * @return the resource it produces
	 */
	public Resource resource() {
		return resource;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Factory agent (%s) - consumes ", name()));
		for (Resource r : requirements) {
			builder.append(String.format("%s ", r.toString()));	
		}
		builder.append(String.format(" and produces %s", resource));
		return builder.toString();
	}
}
