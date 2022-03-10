package tv.floeze.bottleengine.client.graphics.mouse;

/**
 * A {@link HoverListener} listens to hover events using its
 * {@link #onHover(double, double)} method.
 * 
 * @author Floeze
 *
 */
public interface HoverListener {

	/**
	 * Does nothing on hover
	 */
	public static final HoverListener NOTHING = (x, y) -> {
	};

	/**
	 * Notifies this {@link HoverListener} that the mouse has been hovered
	 * 
	 * @param x new x-position of the cursor
	 * @param y new y-position of the cursor
	 */
	public void onHover(double x, double y);

}
