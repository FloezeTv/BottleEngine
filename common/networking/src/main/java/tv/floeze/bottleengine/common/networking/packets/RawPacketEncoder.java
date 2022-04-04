package tv.floeze.bottleengine.common.networking.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes {@link RawPacket}s into bytes
 * 
 * @author Floeze
 *
 */
public class RawPacketEncoder extends MessageToByteEncoder<RawPacket> {

	/**
	 * The byte sequence used to determine a packet start
	 */
	private final byte[] beginPacket;
	/**
	 * The version to annotate packets with
	 */
	private final int version;

	/**
	 * The constant length each packet has
	 */
	private final int constantLength;

	/**
	 * Creates a new {@link RawPacketEncoder}
	 * 
	 * @param beginPacket the byte sequence used to determine a packet start
	 * @param version     the version to annotate packets with
	 */
	public RawPacketEncoder(byte[] beginPacket, int version) {
		this.beginPacket = beginPacket;
		this.version = version;

		constantLength = //
				beginPacket.length * Byte.BYTES // beginPacket
						+ 1 * Integer.BYTES // version
						+ 1 * Integer.BYTES // length
						+ 1 * Integer.BYTES // packet header
						+ 1 * Integer.BYTES // packet version
						+ 1 * Long.BYTES; // packet hash
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, RawPacket msg, ByteBuf out) throws Exception {
		// general information

		// start
		out.writeBytes(beginPacket);
		// version
		out.writeInt(version);
		// length
		out.writeInt(constantLength + msg.byteLength());

		// packet data

		// packet header
		out.writeInt(msg.getHeader());
		// packet version
		out.writeInt(msg.getVersion());
		// packet hash
		out.writeLong(msg.computeHash());
		// packet data
		out.writeBytes(msg.getData());
	}

}
