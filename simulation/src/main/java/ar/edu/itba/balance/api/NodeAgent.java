package ar.edu.itba.balance.api;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;

/**
 * Information about an agent running with its running node
 * @author POD
 *
 */
public class NodeAgent implements Serializable {
	private NodeInformation nodeInformation;
	private Agent agent;
	
	/**
	 * @param nodeInformation the node in which the agent is currently running. May be null
	 * if it is not running yet.
	 * @param agent the agent
	 */
	public NodeAgent(NodeInformation nodeInformation, Agent agent) {
		Preconditions.checkNotNull(agent);
		this.nodeInformation = nodeInformation;
		this.agent = agent;
	}
	
	/**
	 * @return the agent
	 */
	public Agent agent() {
		return agent;
	}
	
	/**
	 * @return the node in which the agent is currently running. May be null
	 * if it is not running yet.
	 */
	public NodeInformation node() {
		return nodeInformation;
	}
	
}
