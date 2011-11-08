package ar.edu.itba.pod.agent.market;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Transfer history done in a market
 * 
 * @author POD
 */
public class TransferHistory implements Serializable {
	private static final long serialVersionUID = 6003956857275629231L;
	private final List<TransferHistoryItem> historyItems;
	private final double transactionsPerSecond;

	/**
	 * @param historyItems
	 * @param transactionsPerSecond
	 */
	public TransferHistory(List<TransferHistoryItem> historyItems, double transactionsPerSecond) {
		this.transactionsPerSecond = transactionsPerSecond;
		Preconditions.checkNotNull(historyItems, "history items could not be null");
		this.historyItems = historyItems;
	}

	/**
	 * @return the transactionsPerSecond
	 */
	public double getTransactionsPerSecond() {
		return transactionsPerSecond;
	}

	/**
	 * @return the historyItems
	 */
	public List<TransferHistoryItem> getHistoryItems() {
		return historyItems;
	}
}