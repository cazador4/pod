package ar.edu.itba.pod.agent.market;

import java.io.Serializable;

import ar.edu.itba.pod.doc.Immutable;

@Immutable
public class Provides implements Serializable {
	private final String source;
	private final Resource resource;
	private final int amount;

	public Provides(String source, Resource resource, int amount) {
		super();
		this.source = source;
		this.resource = resource;
		this.amount = amount;
	}
	
	public Resource resource() {
		return resource;
	}
	
	public int amount() {
		return amount;
	}
	
	public String source() {
		return source;
	}
}
