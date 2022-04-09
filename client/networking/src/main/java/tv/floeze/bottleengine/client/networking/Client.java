package tv.floeze.bottleengine.client.networking;

import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketChannelInitializer;

/**
 * A client that can connect to a server using TCP/IP.
 * 
 * @author Floeze
 *
 */
public class Client {

	/**
	 * The state of a {@link Client}
	 * 
	 * @author Floeze
	 *
	 */
	private enum State {
		/**
		 * Client is idling and not connected
		 */
		IDLE,
		/**
		 * Client is currently connecting to a server
		 */
		CONNECTING,
		/**
		 * Client is connected to a server and packets can be sent
		 */
		CONNECTED,
		/**
		 * Client is currently disconnecting from a server
		 */
		DISCONNECTING;
	}

	/**
	 * The host to connect to
	 */
	private final String host;
	/**
	 * The port to connect to
	 */
	private final int port;
	/**
	 * The bytes used to denote a packet start
	 */
	private final byte[] beginPacket;
	/**
	 * The version of this client
	 */
	private final int version;
	/**
	 * The versions this client is compatible with
	 */
	private final int[] compatibleVersions;

	/**
	 * The channel of this client to send messages to the server
	 */
	private Channel channel;
	private EventLoopGroup workerGroup;

	/**
	 * The {@link State} of this {@link Client}
	 */
	private State state = State.IDLE;

	/**
	 * Creates a new {@link Client}
	 * 
	 * @param host               the host to connect to
	 * @param port               the port to connect to
	 * @param beginPacket        the bytes used to denote a packet start
	 * @param version            the version of this client
	 * @param compatibleVersions the versions this client is compatible with
	 */
	public Client(String host, int port, byte[] beginPacket, int version, int... compatibleVersions) {
		this.host = host;
		this.port = port;
		this.beginPacket = beginPacket;
		this.version = version;
		this.compatibleVersions = compatibleVersions;
	}

	/**
	 * Connects this client to the specified server
	 * 
	 * @return a {@link CompletableFuture} that completes as soon as the client is
	 *         connected
	 */
	public synchronized CompletableFuture<Void> connect() {
		if (state != State.IDLE)
			throw new IllegalStateException("Client already connected");
		state = State.CONNECTING;

		workerGroup = new NioEventLoopGroup();

		Bootstrap bootstrap = new Bootstrap().group(workerGroup).channel(NioSocketChannel.class)
				.handler(new PacketChannelInitializer(beginPacket, version, compatibleVersions));

		CompletableFuture<Void> future = new CompletableFuture<>();
		channel = bootstrap.connect(host, port).addListener(v -> state = State.CONNECTED)
				.addListener(v -> future.complete(null)).channel();
		channel.closeFuture().addListener(v -> state = State.IDLE);
		return future;
	}

	/**
	 * Disconnects this client from the server
	 * 
	 * @return a {@link CompletableFuture} that completes as soon as the client is
	 *         disconnected
	 */
	public synchronized CompletableFuture<Void> disconnect() {
		if (state != State.CONNECTED)
			throw new IllegalStateException("Client not connected");
		state = State.DISCONNECTING;

		CompletableFuture<Void> channelFuture = new CompletableFuture<>();
		CompletableFuture<Void> workerGroupFuture = new CompletableFuture<>();
		channel.disconnect().addListener(v -> channelFuture.complete(null));
		workerGroup.shutdownGracefully().addListener(v -> workerGroupFuture.complete(null));
		channel = null;
		workerGroup = null;
		return CompletableFuture.allOf(channelFuture, workerGroupFuture).thenAccept(v -> state = State.IDLE);
	}

	/**
	 * Sends a packet to the server
	 * 
	 * @param packet the packet to send
	 */
	public void send(Packet packet) {
		if (state != State.CONNECTED)
			throw new IllegalStateException("Client not connected");
		channel.writeAndFlush(packet);
	}

	/**
	 * @return {@code true} if the client is currently idle, {@code false} otherwise
	 */
	public boolean isIdle() {
		return state == State.IDLE;
	}

	/**
	 * @return {@code true} if the client is currently connecting to the server,
	 *         {@code false} otherwise
	 */
	public boolean isConnecting() {
		return state == State.CONNECTING;
	}

	/**
	 * @return {@code true} if the client is currently connected to the server,
	 *         {@code false} otherwise
	 */
	public boolean isConnected() {
		return state == State.CONNECTED;
	}

	/**
	 * @return {@code true} if the client is currently disconnecting from the
	 *         server, {@code false} otherwise
	 */
	public boolean isDisconnecting() {
		return state == State.DISCONNECTING;
	}

}
