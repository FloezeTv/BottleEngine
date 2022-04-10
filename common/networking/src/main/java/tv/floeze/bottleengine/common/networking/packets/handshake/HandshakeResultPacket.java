package tv.floeze.bottleengine.common.networking.packets.handshake;

import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketInfo;
import tv.floeze.bottleengine.common.networking.packets.RawPacket;

/**
 * The second package sent in the handshake.<br />
 * Contains the selected version.
 * 
 * @author Floeze
 *
 */
@PacketInfo(header = 1, compatibleVersions = { 0 })
public class HandshakeResultPacket extends Packet {

	/**
	 * The selected version
	 */
	private final int version;

	/**
	 * Creates a new {@link HandshakeResultPacket}
	 * 
	 * @param version the version to use
	 */
	public HandshakeResultPacket(int version) {
		this.version = version;
	}

	/**
	 * Decodes a {@link HandshakeResultPacket}
	 * 
	 * @param raw the raw packet to decode from
	 */
	public HandshakeResultPacket(RawPacket raw) {
		this.version = raw.getData().readInt();
	}

	@Override
	protected void encode(RawPacket raw) {
		raw.getData().writeInt(version);
	}

	/**
	 * Gets the selected version
	 * 
	 * @return the selected version
	 */
	public int getVersion() {
		return version;
	}

}
