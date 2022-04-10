package tv.floeze.bottleengine.common.networking.packets;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation where info for a {@link Packet} is defined.
 * 
 * @author Floeze
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface PacketInfo {

	/**
	 * The identifying header of the packet (should be unique)
	 * 
	 * @return the header of the packet
	 */
	public int header();

	/**
	 * Versions the implementation of the packet is compatible with.
	 * 
	 * @return versions the implementations the packet is compatible with
	 */
	public int[] compatibleVersions();

}
