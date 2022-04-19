package tv.floeze.bottleengine.common.networking.packets.handshake;

import java.util.function.IntFunction;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.exceptions.IncompatibleVersionException;

/**
 * Handles a handshake on new connections
 * 
 * @author Floeze
 *
 */
public class HandshakeHandler extends SimpleChannelInboundHandler<Packet> {

	/**
	 * Whether this handler should begin the handshake on new connections
	 */
	private final boolean beginHandshake;
	/**
	 * The versions to offer
	 */
	private final int[] compatibleVersions;
	/**
	 * The function to call when a handshake is successful that returns a handler to
	 * replace this handler
	 */
	private final IntFunction<ChannelHandler> versionCallback;

	/**
	 * Creates a new handshake handler
	 * 
	 * @param beginHandshake     {@code true} if this handler should begin the
	 *                           handshake on new connections, {@code false} if this
	 *                           handler should only reply to received handshakes
	 * @param compatibleVersions the versions this is compatible with to find the
	 *                           version to use
	 * @param versionCallback    the function to call when a handshake is successful
	 *                           that returns a handler to replace this handler
	 */
	public HandshakeHandler(boolean beginHandshake, int[] compatibleVersions,
			IntFunction<ChannelHandler> versionCallback) {
		this.beginHandshake = beginHandshake;
		this.compatibleVersions = compatibleVersions;
		this.versionCallback = versionCallback;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		if (beginHandshake)
			ctx.writeAndFlush(new HandshakeOfferPacket(compatibleVersions));
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		if (msg instanceof HandshakeOfferPacket) {
			HandshakeOfferPacket p = (HandshakeOfferPacket) msg;

			int version = getBestVersion(compatibleVersions, p.getVersions());

			handshakeCompleted(ctx, version);

			ctx.writeAndFlush(new HandshakeResultPacket(version));
		} else if (msg instanceof HandshakeResultPacket) {
			HandshakeResultPacket p = (HandshakeResultPacket) msg;

			if (!isVersionValid(p.getVersion()))
				throw new IncompatibleVersionException(compatibleVersions, p.getVersion());

			handshakeCompleted(ctx, p.getVersion());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.disconnect();
		super.exceptionCaught(ctx, cause);
	}

	/**
	 * The handshake was completed
	 * 
	 * @param ctx     the {@link ChannelHandlerContext} the handshake was completed
	 *                at
	 * @param version the version that resulted of the handshake
	 */
	private void handshakeCompleted(ChannelHandlerContext ctx, int version) {
		ChannelHandler replacementHandler = versionCallback.apply(version);
		ctx.pipeline().replace(this, null, replacementHandler);
	}

	/**
	 * Checks if a selected version is valid (in {@link #compatibleVersions})
	 * 
	 * @param version the version to check
	 * @return {@code true} if the version is valid, {@code false} otherwise
	 */
	private boolean isVersionValid(int version) {
		for (int v : compatibleVersions)
			if (v == version)
				return true;
		return false;
	}

	/**
	 * Gets the highest version both handlers are compatible with
	 * 
	 * @param a the versions of one handler
	 * @param b the versions of the other handler
	 * @return the highest version in both arrays
	 * @throws IncompatibleVersionException no version exists that both handlers are
	 *                                      compatible with (the two arrays are
	 *                                      disjoint)
	 */
	private static final int getBestVersion(int[] a, int[] b) throws IncompatibleVersionException {
		int val = Integer.MIN_VALUE;
		boolean found = false;

		for (int va : a) {
			for (int vb : b) {
				if (va == vb && va > val) {
					val = va;
					found = true;
					break;
				}
			}
		}

		if (!found)
			throw new IncompatibleVersionException(a, b);

		return val;
	}

}
