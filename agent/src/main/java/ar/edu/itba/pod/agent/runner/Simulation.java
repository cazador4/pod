package ar.edu.itba.pod.agent.runner;

import java.util.List;

import org.joda.time.Duration;


/**
 * Simulation engine
 */
public interface Simulation {

	/**
	 * Adds a new agent to the simulation. 
	 * Works even if the simulation is already running, inwhich case the agent is started as well.
	 */
	public void add(Agent agent);

	/**
	 * Removes an agent from the simulation
	 * If the simulation is running, the agent is notified that it should stop, but the method
	 * doesn't wait for the agent to stop.
	 */
	public void remove(Agent agent);

	/**
	 * Starts running the simulation.
	 * The simulation will run for the given time unless stopped by calling the method <code>stop()</code> before
	 * Calling with a duration of 0 (Duration.ZERO) means to run forever.
	 * @throws IllegalStateException if the simulation is already running 
	 */
	public void start(Duration duration);

	/**
	 * Starts running the simulation and waits until it finishes before returning.
	 * The simulation will run for the given time unless stopped by calling the method <code>stop()</code> before
	 * @throws IllegalArgumentException if duration is 0.
	 * @throws IllegalStateException if the simulation is already running 
	 */
	public void startAndWait(Duration duration) throws InterruptedException;
	
	/**
	 * Ends the simulation.
	 * This method will wait for all the agents to stop before returning.
	 * @throws IllegalStateException If the simulation is not running.
	 */
	public void stop() throws InterruptedException;
	
	
	/**
	 * Returns the time the simulation has been running, 0 if the simulation hasn't started 
	 */
	public Duration elapsed();
	
	/**
	 * Returns the remaining time of the simulation, 0 if the siomulation hasn't started or has finished
	 */
	public Duration remaining();
	
	/**
	 * Returns the amount of running agents
	 */
	public int agentsRunning();
	
	/**
	 * @return the agents currently running
	 */
	public List<Agent> getAgentsRunning();
	
}
