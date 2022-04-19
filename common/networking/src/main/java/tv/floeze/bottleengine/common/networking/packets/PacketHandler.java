package tv.floeze.bottleengine.common.networking.packets;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation for methods that handle {@link Packet}s in a
 * {@link PacketListener}
 * 
 * @author Floeze
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface PacketHandler {

	/**
	 * The versions that the annotated handler is compatible with.<br />
	 * By default the handler is compatible with every version the {@link Packet}
	 * that is handled is compatible with.
	 * 
	 * @return an array of versions the annotated handler is compatible with
	 */
	public int[] compatibleVersions() default {};

}
