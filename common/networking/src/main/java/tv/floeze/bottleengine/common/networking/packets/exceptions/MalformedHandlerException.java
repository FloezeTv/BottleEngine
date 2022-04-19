package tv.floeze.bottleengine.common.networking.packets.exceptions;

import java.lang.reflect.Method;

import tv.floeze.bottleengine.common.networking.packets.Packet;
import tv.floeze.bottleengine.common.networking.packets.PacketHandler;
import tv.floeze.bottleengine.common.networking.packets.PacketListener;

/**
 * A method in a {@link PacketListener} is annotated with {@link PacketHandler}
 * but does not take exactly one {@link Packet} (or a class extending
 * {@link Packet}) as arguments.
 * 
 * @author Floeze
 *
 */
public class MalformedHandlerException extends RuntimeException {

	private static final long serialVersionUID = 7738261970545944804L;

	public MalformedHandlerException(Method method) {
		super(method.toString() + " is a PacketHandler but does not take exactly one Packet as arguments");
	}

}
