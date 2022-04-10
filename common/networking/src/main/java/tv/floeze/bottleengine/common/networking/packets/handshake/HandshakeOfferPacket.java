package tv.floeze.bottleengine.common.networking.packets.handshake;

import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketInfo;
import tv.floeze.bottleengine.common.networking.packets.RawPacket;

/**
 * The first package sent in the handshake.<br />
 * Contains the offered possible versions.
 * 
 * @author Floeze
 *
 */
@PacketInfo(header = 0, compatibleVersions = { 0 })
public class HandshakeOfferPacket extends Packet {

	/**
	 * The versions to offer
	 */
	private final int[] versions;

	/**
	 * Creates a new {@link HandshakeOfferPacket}
	 * 
	 * @param versions the versions to offer
	 */
	public HandshakeOfferPacket(int[] versions) {
		this.versions = versions;
	}

	/**
	 * Decodes a {@link HandshakeOfferPacket}
	 * 
	 * @param raw the raw packet to decode from
	 */
	public HandshakeOfferPacket(RawPacket raw) {
		this.versions = raw.readIntArray();
	}

	@Override
	protected void encode(RawPacket raw) {
		raw.writeIntArray(versions);
	}

	/**
	 * Gets the versions to offer
	 * 
	 * @return the versions to offer
	 */
	public int[] getVersions() {
		return versions;
	}

}
