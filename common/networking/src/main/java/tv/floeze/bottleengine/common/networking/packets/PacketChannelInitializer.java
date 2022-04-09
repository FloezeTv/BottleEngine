package tv.floeze.bottleengine.common.networking.packets;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * Initializes a new Channel that can handle {@link Packet}s
 * 
 * @author Floeze
 *
 */
public class PacketChannelInitializer extends ChannelInitializer<Channel> {

	/**
	 * the bytes used to denote the start of a packet
	 */
	private final byte[] beginPacket;
	/**
	 * the version
	 */
	private final int version;
	/**
	 * the versions that are compatible
	 */
	private final int[] compatibleVersions;

	/**
	 * Creates a new {@link PacketChannelInitializer}
	 * 
	 * @param beginPacket        the bytes used to denote the start of a packet
	 * @param version            the version
	 * @param compatibleVersions the versions that are compatible
	 */
	public PacketChannelInitializer(byte[] beginPacket, int version, int... compatibleVersions) {
		this.beginPacket = beginPacket;
		this.version = version;
		this.compatibleVersions = compatibleVersions;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		// encoding
		ch.pipeline().addLast(new RawPacketEncoder(beginPacket, version));
		ch.pipeline().addLast(new PacketEncoder());

		// decoding
		ch.pipeline().addLast(new RawPacketDecoder(beginPacket, compatibleVersions));
		ch.pipeline().addLast(new PacketDecoder());

		// handler
		ch.pipeline().addLast(new PacketHandler());
	}

}
