package ar.edu.itba.pod.agent.market;

import org.joda.time.Duration;


import ar.edu.itba.pod.agent.runner.Environment;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

/**
 * An agent that requires certain resources and produces other refined type
 */
public class Factory extends ResourceAgent {
	private final ImmutableMultiset<Resource> requirements;
	private final Duration rate;
	private final int amount;

	private final Multiset<Resource> stock = HashMultiset.create();
	
	public Factory(String name, Resource resource, Multiset<Resource> requirements, Duration rate, int amount) {
		super(name, resource);
		this.requirements = ImmutableMultiset.copyOf(requirements);
		this.rate = rate;
		this.amount = amount;
	}
	
	@Override
	public void beforeStart(Environment env) {
		env.listenTo(ResourceTransfer.class);
	}
	
	@Override
	public void execute(Environment env) {
		// Publish requirements to build
		for (Resource res : requirements.elementSet()) {
			env.publish(new Requires(name(), res, requirements.count(res)));
		}
		
		// Wait for resources
		while(areResourcesNeeded()) {
			ResourceTransfer tr = env.waitFor(ResourceTransfer.class);
			stock.add(tr.resource(), tr.amount());
		}
		
		env.wait(rate);
		consumeStock();
		env.publish(new Provides(name(), resource(), amount));
	}
	
	private boolean areResourcesNeeded() {
		for (Resource res : requirements.elementSet()) {
			if (stock.count(res) < requirements.count(res)) {
				return true;
			}
		}
		return false;
	}
	
	private void consumeStock() {
		for (Resource res : requirements.elementSet()) {
			stock.remove(res, requirements.count(res));
		}
	}

	@Override
	public AgentState state() {
		return new FactoryState(name(), Lists.newArrayList(requirements), super.resource());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Factory agent (%s) - consumes ", name()));
		for (Resource r : requirements) {
			builder.append(String.format("%s ", r.toString()));	
		}
		builder.append(String.format(" and produces %s", resource()));
		return builder.toString();
	}
}
