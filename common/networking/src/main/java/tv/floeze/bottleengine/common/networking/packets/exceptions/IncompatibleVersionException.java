package tv.floeze.bottleengine.common.networking.packets.exceptions;

import java.util.Arrays;

/**
 * A version is not compatible
 * 
 * @author Floeze
 *
 */
public class IncompatibleVersionException extends PacketException {

	private static final long serialVersionUID = 4972796819156818832L;

	public IncompatibleVersionException(int[] compatibleVersions, int version) {
		super("Version " + version + " is incompatible with versions " + Arrays.toString(compatibleVersions));
	}

	public IncompatibleVersionException(int[] a, int[] b) {
		super("No compatible version exists between " + Arrays.toString(a) + " and " + Arrays.toString(b));
	}

}
