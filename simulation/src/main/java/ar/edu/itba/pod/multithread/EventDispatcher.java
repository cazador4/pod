package ar.edu.itba.pod.multithread;

import java.io.Serializable;

import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.doc.ThreadSafe;


/**
 * Event dispatcher
 * <p>This service provides each agent with a way to interact each other </p>
 */
@ThreadSafe
public interface EventDispatcher {

	/**
	 * Registers an agent so that it starts receiving events of the given type
	 * <p> Event types are strict, in the sense that listennign for a type will not mean listenning for any of 
	 * it's supertypes </p>
	 */
	public void register(Agent agent, Class<? extends Serializable> eventType);
	
	/**
	 * Deregisters an agent so that it stops receiving events of the given type
	 */
	public void deregister(Agent agent, Class<? extends Serializable> eventType);
	
	/**
	 * Waits for an event. Note that this method will return only events of types 
	 * previously registered. 
	 * <p>In cases where there are several events that do not follow a unique hierarchy, it is expected to use
	 * <code>Object.class</code> as baseType </p> 
	 * @return The event received or null if timeout
	 * @throws IllegalStateException if no event type has been registered before calling <code>listenTo()</code>
	 * @throws InterruptedException if interrupted while waiting
	 */
	public <T extends Serializable> T waitFor(Agent agent, Class<T> baseType) throws InterruptedException;
	
	/**
	 * Publishes an event to the environment
	 * This method may not return immediately if the event queue of the system is full
	 * @throws InterruptedException if interrupted while waiting
	 */
	public void publish(Agent source, Serializable event) throws InterruptedException;

}
