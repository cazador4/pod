package ar.edu.itba.pod.time;

import org.joda.time.Duration;

/**
 * Transforms a duration to an expected number of miliseconds.
 * <p>Note that the transofmration is arbitrary in the sense that it is expected that some
 * implementations provide a "fast-forward" transformation,
 */
public interface TimeMapper {

	public long toMillis(Duration duration);
}
