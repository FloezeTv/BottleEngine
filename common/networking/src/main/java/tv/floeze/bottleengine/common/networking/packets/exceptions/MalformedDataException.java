package tv.floeze.bottleengine.common.networking.packets.exceptions;

/**
 * The data of a received packet is malformed
 * 
 * @author Floeze
 *
 */
public class MalformedDataException extends PacketException {

	private static final long serialVersionUID = 4685446545696500576L;

	public MalformedDataException(long expectedHash, long actualHash) {
		super("Hash was " + actualHash + " but expected " + expectedHash);
	}

}
