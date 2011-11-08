package ar.edu.itba.pod.agent.market;

import com.google.common.base.Preconditions;

public abstract class NameableAgentState implements AgentState {
	private String name;
	
	/**
	 * @param name
	 */
	public NameableAgentState(String name) {
		Preconditions.checkNotNull(name);
		this.name = name;
	}



	@Override
	public String name() {
		return name;
	}

}
