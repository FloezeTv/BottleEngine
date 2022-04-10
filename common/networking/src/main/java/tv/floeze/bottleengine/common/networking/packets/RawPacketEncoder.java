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
	 * The constant length each packet has
	 */
	private final int constantLength;

	/**
	 * Creates a new {@link RawPacketEncoder}
	 * 
	 * @param beginPacket the byte sequence used to determine a packet start
	 */
	public RawPacketEncoder(byte[] beginPacket) {
		this.beginPacket = beginPacket;

		constantLength = //
				beginPacket.length * Byte.BYTES // beginPacket
						+ 1 * Integer.BYTES // length
						+ 1 * Integer.BYTES // packet header
						+ 1 * Long.BYTES; // packet hash
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, RawPacket msg, ByteBuf out) throws Exception {
		// general information

		// start
		out.writeBytes(beginPacket);
		// length
		out.writeInt(constantLength + msg.byteLength());

		// packet data

		// packet header
		out.writeInt(msg.getHeader());
		// packet hash
		out.writeLong(msg.computeHash());
		// packet data
		out.writeBytes(msg.getData());
	}

}
