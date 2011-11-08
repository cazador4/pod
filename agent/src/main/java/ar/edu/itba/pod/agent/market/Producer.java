package ar.edu.itba.pod.agent.market;

import org.joda.time.Duration;

import ar.edu.itba.pod.agent.runner.Environment;

/**
 * An agent that produces a resource 
 */
public class Producer extends ResourceAgent {
	private Duration rate;
	private int amount;

	public Producer(String name, Resource resource, Duration rate, int amount) {
		super(name, resource);
		this.rate = rate;
		this.amount = amount;
	}
	
	
	@Override
	public void execute(Environment env) {
		env.wait(rate);
		env.publish(new Provides(name(), resource(), amount));
		log("selling %d units of %s", amount, resource().toString());
	}


	@Override
	public AgentState state() {
		return new ProducerState(name(), super.resource());
	}
	
	@Override
	public String toString() {
		return String.format("Producer agent (%s) - produces %s", name(), resource());
	}
}
