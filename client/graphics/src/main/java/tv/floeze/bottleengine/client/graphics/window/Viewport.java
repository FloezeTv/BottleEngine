package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * A viewport that renders to a part of the window.
 * 
 * @author Floeze
 *
 */
public class Viewport {

	/**
	 * Percentage where the {@link Viewport} should start/end in percent of the
	 * window size (0.0 - 1.0)
	 */
	private double xStart, yStart, xEnd, yEnd;

	/**
	 * If this viewport is currently visible
	 */
	private boolean visible = true;

	/**
	 * Creates a new Viewport that fills the entire window
	 */
	public Viewport() {
		this(0, 0, 1, 1);
	}

	/**
	 * Creates a new viewport that fills a part of the window
	 * 
	 * @param xStart percent of window width the {@link Viewport} starts at
	 * @param yStart percent of window height the {@link Viewport} starts at
	 * @param xEnd   percent of window width the {@link Viewport} ends at
	 * @param yEnd   percent of window height the {@link Viewport} ends at
	 */
	public Viewport(double xStart, double yStart, double xEnd, double yEnd) {
		setBounds(xStart, yStart, xEnd, yEnd);
	}

	/**
	 * Changes the part of the window this viewport fills
	 * 
	 * 
	 * @param xStart percent of window width the {@link Viewport} starts at
	 * @param yStart percent of window height the {@link Viewport} starts at
	 * @param xEnd   percent of window width the {@link Viewport} ends at
	 * @param yEnd   percent of window height the {@link Viewport} ends at
	 */
	public void setBounds(double xStart, double yStart, double xEnd, double yEnd) {
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}

	/**
	 * Gets the percentage of the window width the {@link Viewport} starts at
	 * 
	 * @return the percentage of the window width the {@link Viewport} starts at
	 */
	public double getXStart() {
		return xStart;
	}

	/**
	 * Gets the percentage of the window height the {@link Viewport} starts at
	 * 
	 * @return the percentage of the window height the {@link Viewport} starts at
	 */
	public double getYStart() {
		return yStart;
	}

	/**
	 * Gets the percentage of the window width the {@link Viewport} ends at
	 * 
	 * @return the percentage of the window width the {@link Viewport} ends at
	 */
	public double getXEnd() {
		return xEnd;
	}

	/**
	 * Gets the percentage of the window height the {@link Viewport} ends at
	 * 
	 * @return the percentage of the window height the {@link Viewport} ends at
	 */
	public double getYEnd() {
		return yEnd;
	}

	/**
	 * Sets if this {@link Viewport} should be visible
	 * 
	 * @param visible true if this {@link Viewport} should be visible, false
	 *                otherwise
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Gets if this {@link Viewport} is currently visible
	 * 
	 * @return true if visible, false otherwise
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Renders this viewport
	 * 
	 * @param width  width of the window to render in
	 * @param height height of the window to render in
	 */
	public void render(int width, int height) {
		if (!visible)
			return;

		int x = (int) (width * xStart);
		int y = (int) (height * yStart);
		int w = (int) (width * xEnd - x);
		int h = (int) (height * yEnd - y);
		glViewport(x, y, w, h);

		// TODO: do some actual rendering
	}

}
