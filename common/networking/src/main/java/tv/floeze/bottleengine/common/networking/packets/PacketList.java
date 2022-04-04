package tv.floeze.bottleengine.common.networking.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import tv.floeze.bottleengine.common.networking.packets.exceptions.IncompatibleVersionException;
import tv.floeze.bottleengine.common.networking.packets.exceptions.MissingPacketException;

/**
 * A list of all {@link Packet}s that can be decoded.
 * 
 * @author Floeze
 *
 */
public class PacketList {

	private static final Map<String, PacketList> INSTANCES = new HashMap<>();

	/**
	 * header -> version -> packet decode constructor
	 */
	private final Map<Integer, Map<Integer, Constructor<? extends Packet>>> packets = new HashMap<>();

	/**
	 * Creates a new {@link PacketList}.<br />
	 * Should not be called directly. Instead, use {@link #get(String)}.
	 * 
	 * @param packageName the name of the package to scan for packets
	 */
	private PacketList(String packageName) {
		// header -> version -> difference packageVersion and version
		Map<Integer, Map<Integer, Integer>> packetVersionDifferences = new HashMap<>();

		// scan all classes for Packets and save the closest compatible constructor
		try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).enableAnnotationInfo().scan()) {
			for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(PacketInfo.class)) {
				if (!classInfo.extendsSuperclass(Packet.class))
					continue;
				if (classInfo.isAbstract())
					continue;
				@SuppressWarnings("unchecked")
				Class<? extends Packet> packetClass = (Class<? extends Packet>) classInfo.loadClass();

				PacketInfo info = packetClass.getAnnotation(PacketInfo.class);

				Constructor<? extends Packet> constructor = getDecodeConstructor(packetClass);
				if (constructor == null)
					continue;

				Map<Integer, Constructor<? extends Packet>> constructors = packets.computeIfAbsent(info.header(),
						k -> new HashMap<>());
				Map<Integer, Integer> versionDifferences = packetVersionDifferences.computeIfAbsent(info.header(),
						k -> new HashMap<>());

				tryAddConstructors(info, constructor, constructors, versionDifferences);
			}
		}
	}

	/**
	 * Gets the decode constructor of the given {@link Packet} class that takes a
	 * single {@link RawPacket}
	 * 
	 * @param packetClass the class to get the constructor for
	 * @return the constructor of {@code null} if no such constructor was found
	 */
	private static Constructor<? extends Packet> getDecodeConstructor(Class<? extends Packet> packetClass) {
		try {
			Constructor<? extends Packet> constructor = packetClass.getDeclaredConstructor(RawPacket.class);
			constructor.setAccessible(true);
			if (!constructor.isAccessible())
				return null;
			return constructor;
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	/**
	 * Tries to add the given constructor to the passed map of constructors and
	 * updates the passed map of version differences.<br />
	 * Will only add the constructor if no other constructor is in the list or the
	 * other constructor has a bigger version difference.
	 * 
	 * @param info               The info of the packet to add the constructor for
	 * @param constructor        The constructor to add
	 * @param constructors       The list of constructors to add into (version ->
	 *                           constructor)
	 * @param versionDifferences The list of version differences to update (version
	 *                           -> difference)
	 */
	private static void tryAddConstructors(PacketInfo info, Constructor<? extends Packet> constructor,
			Map<Integer, Constructor<? extends Packet>> constructors, Map<Integer, Integer> versionDifferences) {
		// a packet is always compatible with its own version
		tryAddConstructor(info, constructor, info.version(), constructors, versionDifferences);
		for (int version : info.compatibleVersions())
			tryAddConstructor(info, constructor, version, constructors, versionDifferences);
	}

	/**
	 * Tries to add the given constructor to the passed map of constructors and
	 * updates the passed map of version differenced.<br />
	 * Will only add the constructor if no other constructor is in the list or the
	 * other constructor has a bigger version difference.
	 * 
	 * @param info               The info of the packet to add the constructor for
	 * @param constructor        The constructor to add
	 * @param version            The version to add the constructor for
	 * @param constructors       The list of constructors to add into (version ->
	 *                           constructor)
	 * @param versionDifferences The list of version differences to update (version
	 *                           -> difference)
	 */
	private static void tryAddConstructor(PacketInfo info, Constructor<? extends Packet> constructor, int version,
			Map<Integer, Constructor<? extends Packet>> constructors, Map<Integer, Integer> versionDifferences) {
		int versionDifference = Math.abs(info.version() - version);
		if (!constructors.containsKey(version)
				|| versionDifference < versionDifferences.getOrDefault(version, Integer.MAX_VALUE)) {
			constructors.put(version, constructor);
			versionDifferences.put(version, versionDifference);
		}
	}

	/**
	 * Gets the constructor that decodes a {@link RawPacket} into a {@link Packet}
	 * for the given header and version
	 * 
	 * @param header  the header of the {@link Packet}
	 * @param version the version of the {@link Packet}
	 * @return the decode constructor that takes a single {@link RawPacket}
	 * @throws MissingPacketException No packet with the given header and compatible
	 *                                with the version found
	 */
	public Constructor<? extends Packet> get(int header, int version) throws MissingPacketException {
		Map<Integer, Constructor<? extends Packet>> a = packets.get(header);
		if (a == null)
			throw new MissingPacketException(header);
		Constructor<? extends Packet> constructor = a.get(version);
		if (constructor == null)
			throw new MissingPacketException(header, version);
		return constructor;
	}

	/**
	 * Tries to decode a {@link RawPacket} into a {@link Packet} by calling its
	 * decode constructor
	 * 
	 * @param raw the {@link RawPacket}
	 * @return the decoded {@link Packet}
	 * @throws InvocationTargetException    The decoding constructor threw an
	 *                                      exception
	 * @throws MissingPacketException       No packet with the given header found
	 * @throws IncompatibleVersionException No packet compatible with the given
	 *                                      version found
	 */
	public Packet decode(RawPacket raw)
			throws InvocationTargetException, MissingPacketException, IncompatibleVersionException {
		try {
			return get(raw.getHeader(), raw.getVersion()).newInstance(raw);
		} catch (// These exceptions should not be thrown:
				InstantiationException | // Abstract classes are filtered out
				IllegalAccessException | // Inaccessible constructors are set accessible or filtered out
				IllegalArgumentException // Constructors are filtered to take the arguments provided
		e) {
			throw new IllegalStateException(
					"Decoding a packet threw an exception that should not be possible in a valid state", e);
		}
	}

	/**
	 * Gets or creates a {@link PacketList} that contains all the packets in the
	 * specified package
	 * 
	 * @param packageName the package to search in
	 * @return a {@link PacketList} that contains the found packets
	 */
	public static PacketList get(String packageName) {
		return INSTANCES.computeIfAbsent(packageName, PacketList::new);
	}

}
