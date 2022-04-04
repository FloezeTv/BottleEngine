package tv.floeze.bottleengine.common.networking.packets.exceptions;

/**
 * An {@link Exception} that has to do with packets
 * 
 * @author Floeze
 *
 */
public class PacketException extends Exception {

	private static final long serialVersionUID = 3226047378464855323L;

	public PacketException() {
		super();
	}

	public PacketException(String message) {
		super(message);
	}

	public PacketException(Throwable throwable) {
		super(throwable);
	}

	public PacketException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
