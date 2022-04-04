package tv.floeze.bottleengine.common.networking.packets.exceptions;

import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketInfo;

/**
 * A class extending {@link Packet} is not annotated with {@link PacketInfo}
 * 
 * @author Floeze
 *
 */
public class MissingPacketInfoException extends PacketException {

	private static final long serialVersionUID = 5581883788559543478L;

	public MissingPacketInfoException(Class<? extends Packet> packetClass) {
		super(packetClass.getCanonicalName() + " is missing the PacketInfo annotation");
	}

}
