package tv.floeze.bottleengine.common.networking.packets.exceptions;

import tv.floeze.bottleengine.common.networking.packets.Packet;

/**
 * A {@link Packet} was received but no handler exists that is compatible with
 * the version
 * 
 * @author Floeze
 *
 */
public class MissingPacketHandlerException extends Exception {

	private static final long serialVersionUID = 176446180212069136L;

	public MissingPacketHandlerException(Class<? extends Packet> packetClass, int version) {
		super("No handler for " + packetClass + " with version " + version);
	}

}
