package tv.floeze.bottleengine.common.networking.packets;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Decodes {@link RawPacket}s into {@link Packet}s
 * 
 * @author Floeze
 *
 */
public class PacketDecoder extends MessageToMessageDecoder<RawPacket> {

	/**
	 * The list of all packets
	 */
	private final PacketList packets;
	/**
	 * The version to decode packets with
	 */
	private final int version;

	/**
	 * Creates a new {@link PacketDecoder}.<br />
	 * Searches for packets in all packages.
	 * 
	 * @param version the version to decode packets with
	 */
	public PacketDecoder(int version) {
		this("", version);
	}

	/**
	 * Creates a new {@link PacketDecoder}.<br />
	 * Searched for packets in the provided package.
	 * 
	 * @param packageName package to search in
	 * @param version     the version to decode packets with
	 */
	public PacketDecoder(String packageName, int version) {
		packets = PacketList.get(packageName);
		this.version = version;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, RawPacket msg, List<Object> out) throws Exception {
		out.add(packets.decode(msg, version));
	}

}
