package ar.edu.itba.pod.doc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a class as threadsafe. As such, instances may be used concurrently in several threads without any 
 * need for external syncronization.
 * <p>This annotation doesn't enforce thread-safety, but documents it</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ThreadSafe {
	// no body
}
