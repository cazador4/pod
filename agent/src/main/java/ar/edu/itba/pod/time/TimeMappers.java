package ar.edu.itba.pod.time;

import static com.google.common.base.Preconditions.checkArgument;

import org.joda.time.Duration;

/**
 * Factory for {@link TimeMapper} implementations
 */
public class TimeMappers {
	private static final TimeMapper REALTIME = new ScaledMapper(1L, 1L);
	
	public static TimeMapper realtime() {
		return TimeMappers.REALTIME;
	}
	
	public static TimeMapper oneSecondEach(Duration amount) {
		long milis = amount.getMillis();
		checkArgument(milis > 0, "Clock too fast!");
		long gcd = gcd(milis, 1000);
		return new ScaledMapper(milis / gcd, 1000 / gcd);
	}
	
   private static long gcd(long x, long y) {
       	x = Math.abs(x);
       	y = Math.abs(y);
        return (y == 0) ? x : gcd(y, x % y);
    }	

	private static class ScaledMapper implements TimeMapper {
		private final long numerator;
		private final long denominator;
		
		public ScaledMapper(long numerator, long denominator) {
			super();
			checkArgument(denominator > 0, "invalid denominator");
			this.numerator = numerator;
			this.denominator = denominator;
		}

		@Override
		public long toMillis(Duration duration) {
			return duration.getMillis() * denominator / numerator;
		}
	}
}
