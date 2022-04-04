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
	 * Creates a new {@link PacketDecoder}.<br />
	 * Searches for packets in all packages.
	 */
	public PacketDecoder() {
		this("");
	}

	/**
	 * Creates a new {@link PacketDecoder}.<br />
	 * Searched for packets in the provided package.
	 * 
	 * @param packageName package to search in
	 */
	public PacketDecoder(String packageName) {
		packets = PacketList.get(packageName);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, RawPacket msg, List<Object> out) throws Exception {
		out.add(packets.decode(msg));
	}

}
