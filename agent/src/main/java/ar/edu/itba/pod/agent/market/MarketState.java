package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

import com.google.common.base.Preconditions;

/**
 * State of a market
 * @author POD
 *
 */
public class MarketState extends NameableAgentState {
	private Resource resource;
	private int order;
	private int stock;
	private TransferHistory history;
	
	/**
	 * The current market state.
	 * @param name the market name
	 * @param resource it manages
	 * @param order the amount of resource needed
	 * @param stock the amount of resource in stock
	 * @param history the transactions history
	 */
	public MarketState(String name, Resource resource, int order, int stock, 
			TransferHistory history) {
		super(name);
		Preconditions.checkNotNull(resource);
		Preconditions.checkNotNull(history);
		Preconditions.checkArgument(order >= 0);
		Preconditions.checkArgument(stock >= 0);
		this.resource = resource;
		this.order = order;
		this.stock = stock;
		this.history = history;
	}

	/**
	 * @return the transactions history
	 */
	public TransferHistory getHistory() {
		return history;
	}

	/**
	 * @return the resource this market is operating
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the amount of resource this market needs
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the amount of resource this market has
	 */
	public int getStock() {
		return stock;
	}

	@Override
	public AgentType agentType() {
		return AgentType.MARKET;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Market agent (%s) - manages %s", name(), resource));
		builder.append(String.format("Current stock: %d - Current requirements: %d", stock, order));
		builder.append(String.format("Transactions per second: %f", history.getTransactionsPerSecond()));
		return builder.toString();
	}
}
