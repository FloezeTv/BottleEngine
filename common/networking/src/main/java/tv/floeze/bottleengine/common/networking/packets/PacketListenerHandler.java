package tv.floeze.bottleengine.common.networking.packets;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import tv.floeze.bottleengine.common.networking.packets.exceptions.MissingPacketHandlerException;

/**
 * Handles packets and propogates them to {@link PacketListener}s.
 * 
 * @author Floeze
 *
 */
public class PacketListenerHandler extends SimpleChannelInboundHandler<Packet> {

	/**
	 * The handlers for the set versions
	 */
	private final Map<Class<? extends Packet>, PacketListenerList.Handler> handlers;

	/**
	 * The version of the packets
	 */
	private final int version;

	/**
	 * Creates a new {@link PacketListenerHandler}
	 * 
	 * @param packetListeners the {@link PacketListenerList} that has all
	 *                        {@link PacketListener}s registered
	 * @param version         the version of the packets to handle
	 */
	public PacketListenerHandler(PacketListenerList packetListeners, int version) {
		this.handlers = packetListeners.getHandlers(version);
		this.version = version;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		PacketListenerList.Handler handler = handlers.get(msg.getClass());
		if (handler != null)
			handler.handle(msg);
		else
			throw new MissingPacketHandlerException(msg.getClass(), version);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

}
