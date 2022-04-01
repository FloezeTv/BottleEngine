package tv.floeze.bottleengine.common.networking.packets;

/**
 * A packet that can be sent between server and client. <br />
 * Use the {@link PacketInfo} annotation to define the packet and add a
 * constructor that takes a single {@link RawPacket} to decode the packet.
 * 
 * @author Floeze
 *
 */
public abstract class Packet {

	/**
	 * Encodes this {@link Packet} into bytes in the given {@link RawPacket}
	 * 
	 * @param raw {@link RawPacket} to decode this {@link Packet} into
	 */
	protected abstract void encode(RawPacket raw);

}
