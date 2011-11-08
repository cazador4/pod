package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

/**
 * Represents the current agent state
 * @author POD
 *
 */
public interface AgentState extends Serializable {
	/**
	 * @return the agent type
	 */
	AgentType agentType();
	
	/**
	 * @return the agent name
	 */
	String name();
}
