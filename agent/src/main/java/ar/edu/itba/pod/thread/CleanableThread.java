package ar.edu.itba.pod.thread;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread capable of performing a clean shutdown
 */
public abstract class CleanableThread extends Thread {
	private final AtomicBoolean finish = new AtomicBoolean();

	public CleanableThread(String name) {
		super(name);
		checkNotNull(name, "Name cannot be null");
	}
	
	/**
	 * Check if the this thread should finish
	 * @return true if this thread should finish
	 */
	protected final boolean shouldFinish() {
		return this.finish.get();
	}

	/**
	 * Notify the agent that it should finish and start a clean shutdown
	 */
	public void finish() {
		checkState(this.finish.compareAndSet(false, true), "Thread already finished!!");
		interrupt();
	}
	
}
