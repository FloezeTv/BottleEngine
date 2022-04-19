package tv.floeze.bottleengine.client.networking;

import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketChannelInitializer;
import tv.floeze.bottleengine.common.networking.packets.PacketListener;
import tv.floeze.bottleengine.common.networking.packets.PacketListenerList;

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
		 * Client is performing handshake with server
		 */
		HANDSHAKING,
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
	 * The versions this client is compatible with
	 */
	private final int[] compatibleVersions;

	/**
	 * The list of the {@link PacketListener}s
	 */
	private final PacketListenerList packetListeners = new PacketListenerList();

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
	 * The version negotiated between server and client
	 */
	private int version;

	/**
	 * Creates a new {@link Client}
	 * 
	 * @param host               the host to connect to
	 * @param port               the port to connect to
	 * @param beginPacket        the bytes used to denote a packet start
	 * @param compatibleVersions the versions this client is compatible with
	 */
	public Client(String host, int port, byte[] beginPacket, int... compatibleVersions) {
		this.host = host;
		this.port = port;
		this.beginPacket = beginPacket;
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

		CompletableFuture<Void> future = new CompletableFuture<>();

		Bootstrap bootstrap = new Bootstrap().group(workerGroup).channel(NioSocketChannel.class)
				.handler(new PacketChannelInitializer(beginPacket, packetListeners, false, v -> {
					state = State.CONNECTED;
					version = v;
					future.complete(null);
				}, compatibleVersions));

		channel = bootstrap.connect(host, port).addListener(v -> state = State.HANDSHAKING).channel();
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
	 * Adds a new {@link PacketListener} to this client that listens for received
	 * {@link Packet}s
	 * 
	 * @param listener the {@link PacketListener} to add
	 */
	public void addListener(PacketListener listener) {
		packetListeners.addListener(listener);
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
	 * Gets the version that has been negotiated between the client and the server.
	 * Only returns a useful number if the client is connected
	 * ({@link #isConnected()}).
	 * 
	 * @return the version this client uses
	 */
	public int getVersion() {
		return version;
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
