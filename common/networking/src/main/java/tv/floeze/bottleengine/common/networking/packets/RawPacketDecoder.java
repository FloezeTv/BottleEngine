package tv.floeze.bottleengine.common.networking.packets;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import tv.floeze.bottleengine.common.networking.packets.exceptions.IncompatibleVersionException;
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
	 * The versions this is compatible with
	 */
	private final int[] compatibleVersions;

	/**
	 * The constant length each packet has
	 */
	private final int constantLength;

	/**
	 * Creates a new {@link RawPacketDecoder}
	 * 
	 * @param beginPacket        the byte sequence used to determine a packet start
	 * @param compatibleVersions the versions of packets to accept
	 */
	public RawPacketDecoder(byte[] beginPacket, int... compatibleVersions) {
		this.beginPacket = beginPacket;
		this.compatibleVersions = compatibleVersions;
		Arrays.sort(this.compatibleVersions); // needed for binary search

		constantLength = //
				beginPacket.length * Byte.BYTES // beginPacket
						+ 1 * Integer.BYTES // version
						+ 1 * Integer.BYTES // length
						+ 1 * Integer.BYTES // packet header
						+ 1 * Integer.BYTES // packet version
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

		// version
		int version = in.readInt();
		if (!isCompatible(version))
			throw new IncompatibleVersionException(compatibleVersions, version);
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
		// packet version
		int packetVersion = in.readInt();
		// packet hash
		long hash = in.readLong();

		RawPacket raw = new RawPacket(header, packetVersion);
		// packet data
		raw.getData().ensureWritable(length - constantLength);
		in.readBytes(raw.getData(), length - constantLength);

		if (raw.computeHash() != hash)
			throw new MalformedDataException(hash, raw.computeHash());

		out.add(raw);
	}

	/**
	 * Checks if a version is compatible
	 * 
	 * @param version the version to check
	 * @return {@code true} if the version is compatible, {@code false} otherwise
	 */
	public boolean isCompatible(int version) {
		return Arrays.binarySearch(compatibleVersions, version) >= 0;
	}

}
