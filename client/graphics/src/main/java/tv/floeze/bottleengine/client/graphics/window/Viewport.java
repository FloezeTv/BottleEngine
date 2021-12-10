package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.opengl.GL11.glViewport;

import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.camera.Camera;

/**
 * A viewport that renders to a part of the window.
 * 
 * @author Floeze
 *
 */
public class Viewport implements Renderable {

	/**
	 * Percentage where the {@link Viewport} should start/end in percent of the
	 * window size (0.0 - 1.0)
	 */
	private double xStart, yStart, xEnd, yEnd;

	/**
	 * The size of this viewport
	 */
	private AspectMode.Size viewportSize;

	/**
	 * If this viewport is currently visible
	 */
	private boolean visible = true;

	/**
	 * The way the content fits this viewport
	 */
	private AspectMode aspectMode = AspectMode.SCALE;

	/**
	 * The {@link Camera} of this {@link Viewport}
	 */
	private Camera camera;

	/**
	 * Creates a new Viewport that fills the entire window
	 * 
	 * @param camera the {@link Camera} of this {@link Viewport}
	 */
	public Viewport(Camera camera) {
		this(0, 0, 1, 1, camera);
	}

	/**
	 * Creates a new viewport that fills a part of the window
	 * 
	 * @param xStart percent of window width the {@link Viewport} starts at
	 * @param yStart percent of window height the {@link Viewport} starts at
	 * @param xEnd   percent of window width the {@link Viewport} ends at
	 * @param yEnd   percent of window height the {@link Viewport} ends at
	 * @param camera the {@link Camera} of this {@link Viewport}
	 */
	public Viewport(double xStart, double yStart, double xEnd, double yEnd, Camera camera) {
		setBounds(xStart, yStart, xEnd, yEnd);
		setCamera(camera);
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
	 * Changes the {@link Camera} of this viewport
	 * 
	 * @param camera the new {@link Camera} of this viewport
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
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
	 * Updates the size of this viewport
	 * 
	 * @param width  width of the window to render in
	 * @param height height of the window to render in
	 */
	public void updateSize(int width, int height) {
		int x = (int) (width * xStart);
		int y = (int) (height * yStart);
		int w = (int) (width * xEnd - x);
		int h = (int) (height * yEnd - y);

		viewportSize = aspectMode.getSize(x, y, w, h, camera.getWidth(), camera.getHeight());

		camera.updateProjection(viewportSize.cameraWidth, viewportSize.cameraHeight);
	}

	/**
	 * Renders this viewport
	 */
	public void render() {
		if (!visible)
			return;

		glViewport(viewportSize.x, viewportSize.y, viewportSize.width, viewportSize.height);

		camera.setMatrices();

		// TODO: do some actual rendering
	}

}
