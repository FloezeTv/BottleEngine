package tv.floeze.bottleengine.client.graphics.window;

/**
 * Thrown when a system failed to initialize
 * 
 * @author Floeze
 *
 */
public class InitException extends RuntimeException {

	private static final long serialVersionUID = 4398194415453885077L;

	/**
	 * @param system name of system/component that failed to initialize
	 */
	public InitException(String system) {
		super("Couldn't initialize " + system);
	}

}
