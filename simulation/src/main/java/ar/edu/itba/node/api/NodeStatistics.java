package ar.edu.itba.node.api;

import java.io.Serializable;
import java.util.List;

import ar.edu.itba.pod.agent.market.AgentState;

import com.google.common.base.Preconditions;

/**
 * Node statistics
 * @author POD
 *
 */
public class NodeStatistics implements Serializable {
	private int numberOfAgents;
	private List<AgentState> agentStates;
	
	/**
	 * @param numberOfAgents the number of running agents on this node
	 * @param marketStates the current markets state if any
	 */
	public NodeStatistics(int numberOfAgents, List<AgentState> agentStates) {
		Preconditions.checkNotNull(agentStates);
		Preconditions.checkArgument(numberOfAgents >= 0);
		this.numberOfAgents = numberOfAgents;
		this.agentStates = agentStates;
	}
	/**
	 * @return the number of agents
	 */
	public int getNumberOfAgents() {
		return numberOfAgents;
	}
	/**
	 * @return the currents markets states
	 */
	public List<AgentState> getAgentState() {
		return agentStates;
	}
}
