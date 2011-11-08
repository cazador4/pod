package ar.edu.itba.pod.agent.market;

import org.joda.time.Duration;

import ar.edu.itba.pod.agent.runner.Environment;

/**
 * Agent that consumes resources 
 */
public class Consumer extends ResourceAgent {
	private Duration rate;
	private int amount;
	
	private int stock;

	public Consumer(String name, Resource resource, Duration rate, int amount) {
		super(name, resource);
		this.rate = rate;
		this.amount = amount;
	}

	@Override
	public void beforeStart(Environment env) {
		env.listenTo(ResourceTransfer.class);
	}
	
	@Override
	public void execute(Environment env) {
		env.wait(rate);
		env.publish(new Requires(name(), resource(), amount));
		log("buying %d units of %s", amount, resource());
		
		while(stock < amount) {
			ResourceTransfer tr = env.waitFor(ResourceTransfer.class);
			if (tr.resource().equals(resource())) {
				log("%d resourced received!", tr.amount());
				stock += tr.amount();
			}
		}
		stock=0;
	}

	/** @see ar.edu.itba.pod.agent.runner.Agent#state()*/
	@Override
	public AgentState state() {
		return new ConsumerState(name(), super.resource());
	}
	
	@Override
	public String toString() {
		return String.format("Consumer agent (%s) - consumes %s", name(), resource());
	}
}
