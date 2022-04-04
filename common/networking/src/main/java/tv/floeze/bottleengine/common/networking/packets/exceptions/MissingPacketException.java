package tv.floeze.bottleengine.common.networking.packets.exceptions;

/**
 * No packet found for a given header that is compatible with the given version
 * 
 * @author Floeze
 *
 */
public class MissingPacketException extends PacketException {

	private static final long serialVersionUID = -1740095546731588177L;

	public MissingPacketException(int header) {
		super("No Packet found to decode packet with header " + header);
	}

	public MissingPacketException(int header, int version) {
		super("No Packet found to decode packet with header " + header + " for version " + version);
	}

}
