package ar.edu.itba.event;

import java.io.Serializable;

import ar.edu.itba.pod.agent.runner.Agent;

import com.google.common.base.Preconditions;

/**
 * Information about an event
 * @author POD
 *
 */
public class EventInformation implements Serializable {

	private static final long serialVersionUID = -3559212153321679348L;
	
	/**
	 * The event.
	 */
	private final Serializable event;
	
	/**
	 * Id of the node where the event was generated.
	 */
	private final String nodeId;
	
	/**
	 * The source agent.
	 */
	private final Agent source;
	
	/**
	 * Local time that the event was received.
	 */
	private long receivedTime;
	
	/**
	 * @param event
	 * @param nodeId
	 * @param source
	 */
	public EventInformation(Serializable event, String nodeId, Agent source) {
		super();
		Preconditions.checkNotNull(event, "Event cannot be null");
		Preconditions.checkNotNull(source, "Source cannot be null");
		Preconditions.checkNotNull(nodeId, "Node id cannot be null");
		this.event = event;
		this.source = source;
		this.nodeId = nodeId;
	}
	
	public void setReceivedTime(long receivedTime) {
		this.receivedTime = receivedTime;
	}
	
	public boolean isOld(long currentTime) {
		return currentTime - this.receivedTime > 500;
	}
	
	public Serializable event() {
		return this.event;
	}

	public Agent source() {
		return this.source;
	}
	
	public String nodeId() {
		return this.nodeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result + (int) (receivedTime ^ (receivedTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventInformation other = (EventInformation) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (receivedTime != other.receivedTime)
			return false;
		return true;
	}
}
