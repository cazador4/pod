package ar.edu.itba.pod.agent.runner;

import java.io.Serializable;
import java.util.Date;

import ar.edu.itba.pod.agent.market.AgentState;

/**
 * Simulation agent.
 * <p> An agent is a small program that performs a simple and specific task </p>
 * Each agent runs in it's own thread, and a simulation may have several thousand agents running at the same time.
 * Agents perform an action in isolation, and create or react to events in order to provide a global response.
 * </p> 
 * <p>
 * The main function of an agent is run in a loop, meaning that once the method <code>execute</p> finishes, it may be 
 * called again (and will be called, unless the agent is suspended or stopped). 
 * </p>
 * Agents may be suspended and restarted. When an agent is restarted, it may run on a different thread, or even on 
 * a different computer, hence agents must be both serializable and self contained.
 * </p>
 */
public abstract class Agent implements Serializable {
	private final String name;
	private Date startTime;
	
	public Agent(String name) {
		this.startTime = new Date();
		this.name = name;
	}
	
	public final String name() {
		return name;
	}
	/**
	 * @return the start date
	 */
	public final Date startTime() {
		return startTime;
	}
	/**
	 * Callback method executed before starting an agent for the first time
	 * <p>If an agent is suspended an later resumed, this method WILL NOT be called
	 * <p> The environemnt received will be the same that will be used for <code>execute</code> 
	 * @param env The environment where the agent will be run. 
	 */
	public void beforeStart(Environment env) {
		return;
	}
	
	/**
	 * Callback method executed before starting an agent after a suspend.
	 * <p>This method will NOT be called the first time an agent is executed.
	 * <p> The environemnt received will be the same that will be used for <code>execute</code> 
	 * @param env The environment where the agent will be run. 
	 */
	public void beforeResume(Environment env) {
		return;
	}

	/**
	 * Execution point for the agent. The contents of this method will be executed indefinitively as long
	 * as the agent is active
	 * @param environment The environment running the agent
	 */
	public abstract void execute(Environment env);
	
	protected final void log(String format, Object... params) {
		synchronized (System.out) {
			System.out.printf("%s --> ", name());
			System.out.printf(format + "\n", params);
		}
	}
	
	/**
	 * @return the agent current state. Intended only for
	 * reports purposes.
	 */
	public abstract AgentState state();
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
