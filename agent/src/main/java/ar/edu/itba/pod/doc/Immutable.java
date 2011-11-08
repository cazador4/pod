package ar.edu.itba.pod.doc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a class as immutable. As such, instances may be freely returned and used in code that spawns 
 * different threads.
 * <p>This annotation doesn't enforce immutability, but documents it</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
@ThreadSafe
public @interface Immutable {
	// no body
}
