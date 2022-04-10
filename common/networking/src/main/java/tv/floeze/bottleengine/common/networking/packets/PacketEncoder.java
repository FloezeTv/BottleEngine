package tv.floeze.bottleengine.common.networking.packets;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import tv.floeze.bottleengine.common.networking.packets.exceptions.MissingPacketInfoException;

/**
 * Encodes {@link Packet}s into {@link RawPacket}s
 * 
 * @author Floeze
 *
 */
public class PacketEncoder extends MessageToMessageEncoder<Packet> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
		PacketInfo packetInfo = msg.getClass().getAnnotation(PacketInfo.class);
		if (packetInfo == null)
			throw new MissingPacketInfoException(msg.getClass());

		RawPacket raw = new RawPacket(packetInfo.header());
		msg.encode(raw);
		out.add(raw);
	}

}
