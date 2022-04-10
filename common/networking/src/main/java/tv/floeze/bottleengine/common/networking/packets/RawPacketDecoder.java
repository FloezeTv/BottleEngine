package tv.floeze.bottleengine.common.networking.packets;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import tv.floeze.bottleengine.common.networking.packets.exceptions.MalformedDataException;

/**
 * Decodes bytes into {@link RawPacket}s
 * 
 * @author Floeze
 *
 */
public class RawPacketDecoder extends ByteToMessageDecoder {

	/**
	 * The byte sequence used to determine a packet start
	 */
	private final byte[] beginPacket;
	/**
	 * The constant length each packet has
	 */
	private final int constantLength;

	/**
	 * Creates a new {@link RawPacketDecoder}
	 * 
	 * @param beginPacket the byte sequence used to determine a packet start
	 */
	public RawPacketDecoder(byte[] beginPacket) {
		this.beginPacket = beginPacket;

		constantLength = //
				beginPacket.length * Byte.BYTES // beginPacket
						+ 1 * Integer.BYTES // length
						+ 1 * Integer.BYTES // packet header
						+ 1 * Long.BYTES; // packet hash
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// general information

		// discard bytes until start found
		boolean headerCorrect = false;
		while (!headerCorrect) {
			if (in.readableBytes() < beginPacket.length)
				return;

			headerCorrect = true;
			for (int i = 0; i < beginPacket.length; i++) {
				if (in.getByte(i) != beginPacket[i]) { // wrong byte
					in.skipBytes(1);
					headerCorrect = false;
					break;
				}
			}
		}

		// Check if at least header was received
		if (in.readableBytes() < constantLength)
			return;

		in.markReaderIndex();
		int bufferSize = in.readableBytes();

		// beginPacket
		in.skipBytes(beginPacket.length);

		// length
		int length = in.readInt();
		// check if full packet was received
		if (bufferSize < length) {
			in.resetReaderIndex();
			return;
		}

		// packet data

		// packet header
		int header = in.readInt();
		// packet hash
		long hash = in.readLong();

		RawPacket raw = new RawPacket(header);
		// packet data
		raw.getData().ensureWritable(length - constantLength);
		in.readBytes(raw.getData(), length - constantLength);

		if (raw.computeHash() != hash)
			throw new MalformedDataException(hash, raw.computeHash());

		out.add(raw);
	}

}
