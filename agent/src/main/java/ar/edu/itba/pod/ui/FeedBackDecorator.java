package ar.edu.itba.pod.ui;

import static com.google.common.base.Preconditions.checkNotNull;


public abstract class FeedBackDecorator<T> {
	private final FeedbackCallback callback;
	private final T delegate;

	public FeedBackDecorator(FeedbackCallback callback, T delegate) {
		super();
		checkNotNull(callback, "Callback cannot be null");
		checkNotNull(delegate, "Delegate cannot be null");
		this.callback = callback;
		this.delegate = delegate;
	}
	
	protected void feedback(String format, Object... params) {
		checkNotNull(format, "Format cannot be null");
		checkNotNull(params, "Parameters cannot be null");
		this.callback.print(format, params);
	}
	
	protected FeedbackCallback callback() {
		return this.callback;
	}
	
	protected T delegate() {
		return this.delegate;
	}
}
