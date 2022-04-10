package tv.floeze.bottleengine.common.networking.packets;

import java.util.function.IntConsumer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import tv.floeze.bottleengine.common.networking.packets.handshake.HandshakeHandler;

/**
 * Initializes a new Channel that can handle {@link Packet}s.<br />
 * Starts with a {@link HandshakeHandler} for the handshake.
 * 
 * @author Floeze
 *
 */
public class PacketChannelInitializer extends ChannelInitializer<Channel> {

	/**
	 * The package where the handshake packets are located
	 */
	private static final String HANDSHAKE_PACKAGE = HandshakeHandler.class.getPackage().getName();

	/**
	 * the bytes used to denote the start of a packet
	 */
	private final byte[] beginPacket;

	/**
	 * Whether the initialized channel should begin the handshake on new connections
	 */
	private final boolean beginHandshake;

	/**
	 * The function to call when a handshake is successful
	 */
	private final IntConsumer handshakeCompleted;

	/**
	 * The versions this is compatible with
	 */
	private final int[] compatibleVersions;

	/**
	 * Creates a new {@link PacketChannelInitializer}
	 * 
	 * @param beginPacket        the bytes used to denote the start of a packet
	 * @param beginHandshake     {@code true} if the initialized channel should
	 *                           begin the handshake on new connections,
	 *                           {@code false} if the initialized channel should
	 *                           only reply to received handshakes
	 * @param handshakeCompleted the function to call when a handshake is successful
	 * @param compatibleVersions the versions this is compatible with
	 */
	public PacketChannelInitializer(byte[] beginPacket, boolean beginHandshake, IntConsumer handshakeCompleted,
			int... compatibleVersions) {
		this.beginPacket = beginPacket;
		this.beginHandshake = beginHandshake;
		this.handshakeCompleted = handshakeCompleted;
		this.compatibleVersions = compatibleVersions;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		// encoding
		ch.pipeline().addLast(new RawPacketEncoder(beginPacket));
		ch.pipeline().addLast(new PacketEncoder());

		// decoding
		ch.pipeline().addLast(new RawPacketDecoder(beginPacket));
		PacketDecoder packetDecoder = new PacketDecoder(HANDSHAKE_PACKAGE, 0);
		ch.pipeline().addLast(packetDecoder);

		// handler
		ch.pipeline().addLast(new HandshakeHandler(beginHandshake, compatibleVersions, new PacketHandler(), version -> {
			ch.pipeline().replace(packetDecoder, null, new PacketDecoder("", version));
			handshakeCompleted.accept(version);
		}));
	}

}
