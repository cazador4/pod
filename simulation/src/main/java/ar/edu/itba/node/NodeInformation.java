package ar.edu.itba.node;

import java.io.Serializable;

import com.google.common.base.Preconditions;

/**
 * Represents the node information
 * @author POD
 *
 */
public class NodeInformation implements Serializable {

	private static final long serialVersionUID = 6273033757511151541L;
	
	private final String host;
	private final int port;
	private final String id;
	/**
	 * @param host node host
	 * @param port node port
	 * @param id node id
	 */
	public NodeInformation(String host, int port, String id) {
		super();
		//Preconditions.checkNotNull(host, "Host cannot be null");
		//Preconditions.checkNotNull(id, "Id cannot be null");
		this.id = id;
		this.host = host;
		this.port = port;
	}

	public String host() {
		return this.host;
	}
	
	public int port() {
		return this.port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
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
		NodeInformation other = (NodeInformation) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.host + ":" + this.port;
	}

	public String id() {
		return id;
	}

}
