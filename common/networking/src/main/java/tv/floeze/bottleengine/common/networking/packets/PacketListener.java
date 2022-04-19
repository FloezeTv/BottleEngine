package tv.floeze.bottleengine.common.networking.packets;

/**
 * An interface for a listener that listens to received {@link Packet}s.<br />
 * Handler methods should take exactly one {@link Packet} (or a class extending
 * {@link Packet}) as arguments and be annotated with the {@link PacketHandler}
 * exception.
 * 
 * @author Floeze
 *
 */
public interface PacketListener {

}
