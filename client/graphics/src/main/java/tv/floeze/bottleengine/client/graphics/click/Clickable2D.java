package tv.floeze.bottleengine.client.graphics.click;

/**
 * A {@link Clickable2D} listens for a click on a 2D surface
 * 
 * @author Floeze
 *
 */
@FunctionalInterface
public interface Clickable2D {

	/**
	 * Registers a click on the 2D surface.
	 * 
	 * @param button    index of the mouse button
	 * @param action    if the button was pressed or released
	 * @param modifiers if any modifier keys were pressed
	 * @param x         normalized x-coordinate of the click ({@code 0.0} -
	 *                  {@code 1.0})
	 * @param y         normalized y-coordinate of the click ({@code 0.0} -
	 *                  {@code 1.0})
	 */
	public void onClick(int button, int action, int modifiers, double x, double y);

}
