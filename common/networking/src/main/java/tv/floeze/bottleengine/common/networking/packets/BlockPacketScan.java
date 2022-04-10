package tv.floeze.bottleengine.common.networking.packets;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Blocks the recursive scanning for {@link Packet}s in a package and its
 * subpackages.
 * 
 * @author Floeze
 *
 */
@Retention(RUNTIME)
@Target(PACKAGE)
public @interface BlockPacketScan {

}
