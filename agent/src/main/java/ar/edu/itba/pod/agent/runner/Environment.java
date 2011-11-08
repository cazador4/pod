package ar.edu.itba.pod.agent.runner;

import java.io.Serializable;

import org.joda.time.Duration;

/**
 * Simulation environment
 * <p>This service provides each agent with a way to interact with their environment </p>
 */
public interface Environment {

	/**
	 * Waits for a period of time.
	 * <p> It is expected that agents that simulate events that require timming considerations use real world 
	 * durations. This method provides a uniform way for agents to simulate delays, while allowing the whole simulation
	 * to work on different time scales </p>
	 * <p>For example, an agent may do something each hour, but to analyze long term behavior, the simulation may be 
	 * run on a scale of one hour==one second, causing effectively this method to wait only one second instead of the
	 * expected hour, without any need to modify the agent
	 * </p>
	 * @throws IllegalArgumentException if the duration is Zero
     * @throws FinishException If the agent should finish it's processing
     * @throws SuspendException If the agent should suspend it's processing
	 */
	public void wait(Duration duration);
	
	/**
	 * Checks the current state of the agent and returns if no external conditions are found.
	 * <p> This method is meant to be used in agents that have very long processing sections where there is no event
	 * processing o waiting. Note that these are special cases, and hence this method should be used as a last
	 * resort.</p>  
     * @throws FinishException If the agent should finish it's processing
     * @throws SuspendException If the agent should suspend it's processing
	 */
	public void checkState();
	
	/**
	 * Start listenning for events of the given type
	 * <p> Event types are strict, in the sense that listennign for a type will not mean listenning for any of 
	 * it's supertypes </p>
	 */
	public void listenTo(Class<? extends Serializable> eventType);
	
	/**
	 * Stop listenning for events of the given type.
	 */
	public void dontListenTo(Class<? extends Serializable> eventType);
	
	/**
	 * Waits for a period of time for an event. Note that this method will return only events of types 
	 * previously registered. 
	 * If the duration is Zero, the method will wait forever till an event is received or the agent is stopped
	 * <p>In cases where there are several events that do not follow a unique hierarchy, it is expected to use
	 * <code>Serializable.class</code> as baseType </p> 
	 * @return The event received or null if timeout
	 * @throws IllegalStateException if no event type has been registered before calling <code>listenTo()</code>
	 * @throws ClassCastException if the event that can't be cast to baseType 
     * @throws FinishException If the agent should finish it's processing
     * @throws SuspendException If the agent should suspend it's processing
	 */
	public <T extends Serializable> T waitFor(Class<T> baseType);
	
	/**
	 * Publishes an event to the environment
	 * This method may not return immediately if the event queue of the system is full
     * @throws FinishException If the agent should finish it's processing
     * @throws SuspendException If the agent should suspend it's processing
	 */
	public void publish(Serializable event);

}
