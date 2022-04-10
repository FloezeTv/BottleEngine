package tv.floeze.bottleengine.common.networking.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;
import nonapi.io.github.classgraph.scanspec.AcceptReject;
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
		Pattern packagePattern = AcceptReject.globToPattern(packageName, true);

		// scan all classes for Packets and save the closest compatible constructor
		try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).enableAnnotationInfo().scan()) {
			for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(PacketInfo.class)) {
				if (!classInfo.extendsSuperclass(Packet.class))
					continue;
				if (classInfo.isAbstract())
					continue;
				PackageInfo disabledPackage = getAutoscanDisabledPackage(classInfo.getPackageInfo());
				if (disabledPackage != null && !packagePattern.matcher(disabledPackage.getName()).find())
					continue;

				@SuppressWarnings("unchecked")
				Class<? extends Packet> packetClass = (Class<? extends Packet>) classInfo.loadClass();

				PacketInfo info = packetClass.getAnnotation(PacketInfo.class);

				Constructor<? extends Packet> constructor = getDecodeConstructor(packetClass);
				if (constructor == null)
					continue;

				Map<Integer, Constructor<? extends Packet>> constructors = packets.computeIfAbsent(info.header(),
						k -> new HashMap<>());

				for (int version : info.compatibleVersions())
					constructors.putIfAbsent(version, constructor);
			}
		}
	}

	/**
	 * Gets the first parent package with the {@link BlockPacketScan} annotation
	 * 
	 * @param packageInfo the package to begin searching at
	 * @return the first found parent package or {@code null} if none found
	 */
	private static PackageInfo getAutoscanDisabledPackage(PackageInfo packageInfo) {
		if (packageInfo == null)
			return null;
		if (packageInfo.hasAnnotation(BlockPacketScan.class))
			return packageInfo;
		return getAutoscanDisabledPackage(packageInfo.getParent());

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
	public Packet decode(RawPacket raw, int version)
			throws InvocationTargetException, MissingPacketException, IncompatibleVersionException {
		try {
			return get(raw.getHeader(), version).newInstance(raw);
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
