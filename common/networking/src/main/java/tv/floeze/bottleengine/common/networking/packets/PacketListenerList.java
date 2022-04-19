package tv.floeze.bottleengine.common.networking.packets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tv.floeze.bottleengine.common.networking.packets.exceptions.MalformedHandlerException;

/**
 * A List of {@link PacketListener}s and their {@link PacketHandler}s to handle
 * packets
 * 
 * @author Floeze
 *
 */
public class PacketListenerList {

	/**
	 * The handler that calls the method in the {@link PacketListener}
	 * 
	 * @author Floeze
	 *
	 */
	@FunctionalInterface
	public static interface Handler {
		/**
		 * Handles a new Packet
		 * 
		 * @param packet the packet to handle
		 * @throws InvocationTargetException if the handler throws an exception
		 */
		public void handle(Packet packet) throws InvocationTargetException;
	}

	private final Map<Integer, Map<Class<? extends Packet>, Handler>> handlers = new HashMap<>();

	/**
	 * Adds a {@link PacketListener} to the list
	 * 
	 * @param listener the {@link PacketListener} to add
	 */
	@SuppressWarnings("unchecked")
	public void addListener(PacketListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			PacketHandler annotation = method.getAnnotation(PacketHandler.class);
			if (annotation == null)
				continue;

			Parameter[] params = method.getParameters();
			if (params.length != 1 || !Packet.class.isAssignableFrom(params[0].getType()))
				throw new MalformedHandlerException(method);

			method.setAccessible(true);
			if (!method.isAccessible())
				continue;

			// If no versions given, by default compatible with every version of the Packet
			int[] compatibleVersions = annotation.compatibleVersions();
			if (compatibleVersions.length == 0)
				compatibleVersions = params[0].getType().getAnnotation(PacketInfo.class).compatibleVersions();

			for (int version : compatibleVersions) {
				handlers.computeIfAbsent(version, k -> new HashMap<>())
						.put((Class<? extends Packet>) params[0].getType(), p -> {
							try {
								method.invoke(listener, p);
							} catch (// These exceptions should not be thrown:
									IllegalAccessException | // Inaccessible methods are set accessible or filtered out
							IllegalArgumentException // Methods are filtered to take the arguments provided
							e) {
								throw new IllegalStateException(
										"Calling a packet handler threw an exception that should not be possible in a valid state",
										e);
							}
						});
			}
		}
	}

	/**
	 * Gets the handlers of packets for the specified version.<br />
	 * The returned map is unmodifiable and updates automatically if a new
	 * {@link PacketListener} is added using {@link #addListener(PacketListener)}.
	 * 
	 * @param version The version to get the handlers for
	 * @return The handlers for the specified version
	 */
	public Map<Class<? extends Packet>, Handler> getHandlers(int version) {
		return Collections.unmodifiableMap(handlers.getOrDefault(version, new HashMap<>()));
	}

}
