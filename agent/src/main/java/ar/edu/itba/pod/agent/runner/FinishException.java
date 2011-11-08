package ar.edu.itba.pod.agent.runner;

/**
 * Exception thrown when an agent is waiting and it should finish
 */
public class FinishException extends RuntimeException {

	public FinishException() {
		super();
	}

	public FinishException(String message) {
		super(message);
	}
}
