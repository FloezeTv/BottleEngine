package tv.floeze.bottleengine.server.networking;

import java.util.concurrent.CompletableFuture;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tv.floeze.bottleengine.common.networking.packets.PacketChannelInitializer;

/**
 * A server that clients can connect to using TCP/IP.
 * 
 * @author Floeze
 *
 */
public class Server {

	/**
	 * The state of a {@link Server}
	 * 
	 * @author Floeze
	 *
	 */
	private enum State {
		/**
		 * Server is idling and not running
		 */
		IDLE,
		/**
		 * Server is currently starting
		 */
		STARTING,
		/**
		 * Server is currently running and can accept connections
		 */
		RUNNING,
		/**
		 * The server is currently stopping
		 */
		STOPPING;
	}

	/**
	 * The port this server listens on
	 */
	private final int port;
	/**
	 * The bytes used to denote a packet start
	 */
	private final byte[] beginPacket;
	/**
	 * The version of this server
	 */
	private final int version;
	/**
	 * The versions this server is compatible with
	 */
	private final int[] compatibleVersions;

	private EventLoopGroup bossGroup, workerGroup;

	/**
	 * The {@link State} of this {@link Server}
	 */
	private State state = State.IDLE;

	/**
	 * Creates a new {@link Server}
	 * 
	 * @param port               the port to listen on
	 * @param beginPacket        the bytes used to denote a packet start
	 * @param version            the version of this server
	 * @param compatibleVersions the versions this server is compatible with
	 */
	public Server(int port, byte[] beginPacket, int version, int... compatibleVersions) {
		this.port = port;
		this.beginPacket = beginPacket;
		this.version = version;
		this.compatibleVersions = compatibleVersions;
	}

	/**
	 * Starts this server to listen on the specified port
	 * 
	 * @return a {@link CompletableFuture} that completes as soon as the server is
	 *         running
	 */
	public synchronized CompletableFuture<Void> start() {
		if (state != State.IDLE)
			throw new IllegalStateException("Server already started");
		state = State.STARTING;

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();

		ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new PacketChannelInitializer(beginPacket, version, compatibleVersions));

		CompletableFuture<Void> future = new CompletableFuture<>();
		bootstrap.bind(port).addListener(v -> state = State.RUNNING).addListener(v -> future.complete(null));
		return future;
	}

	/**
	 * Stops this server
	 * 
	 * @return a {@link CompletableFuture} that completes as soon as the server is
	 *         stopped
	 */
	public synchronized CompletableFuture<Void> stop() {
		if (state != State.RUNNING)
			throw new IllegalStateException("Server not started");
		state = State.STOPPING;

		CompletableFuture<Void> workerGroupFuture = new CompletableFuture<>();
		CompletableFuture<Void> bossGroupFuture = new CompletableFuture<>();
		workerGroup.shutdownGracefully().addListener(v -> workerGroupFuture.complete(null));
		bossGroup.shutdownGracefully().addListener(v -> bossGroupFuture.complete(null));
		workerGroup = null;
		bossGroup = null;

		return CompletableFuture.allOf(workerGroupFuture, bossGroupFuture).thenAccept(v -> state = State.IDLE);
	}

	/**
	 * @return {@code true} if the server is currently idle, {@code false} otherwise
	 */
	public boolean isIdle() {
		return state == State.IDLE;
	}

	/**
	 * @return {@code true} if the server is currently starting, {@code false}
	 *         otherwise
	 */
	public boolean isStarting() {
		return state == State.STARTING;
	}

	/**
	 * @return {@code true} if the server is currently running and accepting new
	 *         connections, {@code false} otherwise
	 */
	public boolean isRunning() {
		return state == State.RUNNING;
	}

	/**
	 * @return {@code true} if the server is currently stopping, {@code false}
	 *         otherwise
	 */
	public boolean isStopping() {
		return state == State.STOPPING;
	}

}
