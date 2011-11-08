package ar.edu.itba.pod.agent.transfer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.joda.time.DateTime;


public class TransferHistoryItem {

	private final String source;
	private final String destiny;
	private final String resource;
	private final int amount;
	private final DateTime dateTime;
	
	public TransferHistoryItem(String source, String destiny, String resource, int amount) {
		super();
		checkNotNull(source, "Source cannot be null");
		checkNotNull(source, "Destiny cannot be null");
		checkNotNull(source, "Resource cannot be null");
		checkArgument(amount > 0, "Amount must be positive");
		this.source = source;
		this.destiny = destiny;
		this.resource = resource;
		this.amount = amount;
		this.dateTime = new DateTime();
	}
	
	public String source() {
		return this.source;
	}
	
	public String destiny() {
		return this.destiny;
	}
	
	public String resource() {
		return this.resource;
	}
	
	public int amount() {
		return this.amount;
	}
	
	public DateTime dateTime() {
		return this.dateTime;
	}
}
