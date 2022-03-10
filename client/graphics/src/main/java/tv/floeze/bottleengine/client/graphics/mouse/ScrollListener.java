package tv.floeze.bottleengine.client.graphics.mouse;

/**
 * A {@link ScrollListener} listens to key events using its
 * {@link #onScroll(double, double)} method.
 * 
 * @author Floeze
 *
 */
public interface ScrollListener {

	/**
	 * Does nothing on scroll
	 */
	public static final ScrollListener NOTHING = (x, y) -> {
	};

	/**
	 * Notifies this {@link ScrollListener} that the user has scrolled
	 * 
	 * @param x amount of scrolling in x-direction
	 * @param y amount of scrolling in y-direction
	 */
	public void onScroll(double x, double y);

}
