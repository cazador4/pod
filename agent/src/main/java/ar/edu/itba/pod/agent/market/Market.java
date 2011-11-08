package ar.edu.itba.pod.agent.market;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import ar.edu.itba.pod.agent.runner.Environment;

import com.google.common.collect.Lists;


/**
 * Agent that stocks produced units of a given resource and sells them to 
 * agents requiring it.
 */
public class Market extends ResourceAgent {
	private Deque<Requires> orders = new LinkedList<Requires>();
	private int stock;
	private final List<TransferHistoryItem> history = Lists.newArrayList();
	private volatile int transactionCount;
	
	public Market(String name, Resource resource) {
		super(name, resource);
	}
	
	@Override
	public void beforeStart(Environment env) {
		env.listenTo(Requires.class);
		env.listenTo(Provides.class);
	}
	
	@Override
	public void execute(Environment env) {
		Serializable event = env.waitFor(Serializable.class);
		if (event instanceof Requires) {
			Requires msg = (Requires) event;
			if (resource().equals(msg.resource())) {
				log("Receiving order for %d units of %s from %s", msg.amount(), msg.resource(), msg.source());
				orders.add(msg);
			}
		}
		else if (event instanceof Provides) {
			Provides msg = (Provides) event;
			if (resource().equals(msg.resource())) {
				stock += msg.amount();
				log("Receiving %d units of %s from %s", msg.amount(), msg.resource(), msg.source());
			}
		}
		
		// Attempt to fullfill as many orders as possible
		while(stock > 0 && !orders.isEmpty()) {
			Requires order = orders.removeFirst();
			int amount = Math.min(order.amount() , stock);
			env.publish(new ResourceTransfer(order.source(), resource(), amount));
			transactionCount++;
			history.add(createhistory(amount));
			log("transfering %d of %s to %s", amount, resource(), order.source());
			if (amount < order.amount()) {
				orders.addFirst(order.remaining(amount));
				log("queueing partially filled order.");
			}
			stock -= amount;
		}
	}

	/**
	 * @return the snapshot of the market state
	 */
	public AgentState state() {
		return new MarketState(name(), super.resource(), getOrdersAmount(), 
				stock, history());
	}
	/**
	 * @param amount transfered
	 */
	private TransferHistoryItem createhistory(int amount) {
		return new TransferHistoryItem(super.resource(), amount);
	}
	
	
	/**
	 * @return the transfer history in this market
	 */
	private TransferHistory history() {
		return new TransferHistory(history, transactionsPerSecond());
	}
	/**
	 * @return the amount of resource needed
	 */
	private int getOrdersAmount() {
		int ordersAmount = 0;
		List<Requires> snapshot = new ArrayList<Requires>(orders);
		for (Requires require : snapshot) {
			ordersAmount += require.amount();
		}
		return ordersAmount;
	}
	
	/**
	 * @return the average number of transactions per second
	 */
	private double transactionsPerSecond() {
		long time = System.currentTimeMillis() - startTime().getTime();
		return ((double)transactionCount())/time*1000;
	}

	/**
	 * @return the transaction count
	 */
	private double transactionCount() {
		return transactionCount;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Market agent (%s) - manages %s", name(), resource()));
		return builder.toString();
	}
}