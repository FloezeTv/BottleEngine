package tv.floeze.bottleengine.client.graphics;

/**
 * An interface for all classes that have native memory that needs to be
 * disposed
 * 
 * @author Floeze
 *
 */
public interface Disposable {

	/**
	 * Disposes of this object.
	 * 
	 * (Frees native memory and buffers, but will still have to be garbage
	 * collected)
	 */
	public void dispose();

}
