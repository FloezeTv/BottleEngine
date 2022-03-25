package tv.floeze.bottleengine.client.graphics.window;

import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.camera.Camera;
import tv.floeze.bottleengine.client.graphics.click.ClickListener;

/**
 * A viewport that renders to a part of the window.
 * 
 * @author Floeze
 *
 */
public class Viewport implements Renderable, ClickListener {

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
	 * The size of this viewport in pixels in the window
	 */
	private int viewportX, viewportY, viewportWidth, viewportHeight;

	/**
	 * If this viewport is currently visible
	 */
	private boolean visible = true;

	/**
	 * The way the content fits this viewport. <br />
	 * 
	 * Default is {@link AspectMode#NONE}.
	 */
	private AspectMode aspectMode = AspectMode.NONE;

	/**
	 * The {@link Camera} of this {@link Viewport}
	 */
	private Camera camera;

	/**
	 * The content to render
	 */
	private Renderable content;

	/**
	 * The {@link Renderer} of this {@link Viewport}
	 */
	private Renderer renderer;

	/**
	 * The parent of this {@link Viewport}
	 */
	private Window parent;

	/**
	 * Creates a new Viewport that fills the entire window
	 * 
	 * @param camera the {@link Camera} of this {@link Viewport}
	 */
	public Viewport(Camera camera) {
		this(0, 0, 1, 1, camera);
	}

	/**
	 * Creates a new Viewport that fills the entire window
	 * 
	 * @param camera  the {@link Camera} of this {@link Viewport}
	 * @param content the {@link Renderable} to render
	 */
	public Viewport(Camera camera, Renderable content) {
		this(0, 0, 1, 1, camera, content);
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
		this(xStart, yStart, xEnd, yEnd, camera, Renderable.NOTHING);
	}

	/**
	 * Creates a new viewport that fills a part of the window
	 * 
	 * @param xStart  percent of window width the {@link Viewport} starts at
	 * @param yStart  percent of window height the {@link Viewport} starts at
	 * @param xEnd    percent of window width the {@link Viewport} ends at
	 * @param yEnd    percent of window height the {@link Viewport} ends at
	 * @param camera  the {@link Camera} of this {@link Viewport}
	 * @param content the {@link Renderable} to render
	 */
	public Viewport(double xStart, double yStart, double xEnd, double yEnd, Camera camera, Renderable content) {
		this(xStart, yStart, xEnd, yEnd, camera, content, new ForwardsRenderer());
	}

	/**
	 * Creates a new viewport that fills a part of the window
	 * 
	 * @param xStart   percent of window width the {@link Viewport} starts at
	 * @param yStart   percent of window height the {@link Viewport} starts at
	 * @param xEnd     percent of window width the {@link Viewport} ends at
	 * @param yEnd     percent of window height the {@link Viewport} ends at
	 * @param camera   the {@link Camera} of this {@link Viewport}
	 * @param content  the {@link Renderable} to render
	 * @param renderer the {@link Renderer} to use
	 */
	public Viewport(double xStart, double yStart, double xEnd, double yEnd, Camera camera, Renderable content,
			Renderer renderer) {
		setBounds(xStart, yStart, xEnd, yEnd);
		setCamera(camera);
		setContent(content);
		setRenderer(renderer);
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
	 * Changes the {@link Camera} of this {@link Viewport}
	 * 
	 * @param camera the new {@link Camera} of this {@link Viewport}
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * Gets the {@link Camera} of this {@link Viewport}
	 * 
	 * @return the {@link Camera} of this {@link Viewport}
	 */
	public Camera getCamera() {
		return camera;
	}

	public void setContent(Renderable renderable) {
		if (renderable == null)
			renderable = Renderable.NOTHING;
		content = renderable;
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
	 * Sets the {@link AspectMode} to use
	 * 
	 * @param aspectMode {@link AspectMode} to use
	 */
	public void setAspectMode(AspectMode aspectMode) {
		this.aspectMode = aspectMode;
	}

	/**
	 * Sets the new parent of this {@link Viewport}
	 * 
	 * @param parent the new parent of this {@link Viewport}
	 */
	protected void setParent(Window parent) {
		if (this.parent != null) {
			this.parent.execute(this::deinitializeRenderer);
			this.parent.removeViewport(this);
		}

		this.parent = parent;

		if (this.parent != null)
			this.parent.execute(this::initializeRenderer).join(); // must initialize before doing anything else
	}

	/**
	 * Deinitializes the {@link #renderer}
	 */
	private void deinitializeRenderer() {
		renderer.deinitialize();
	}

	/**
	 * Initializes the {@link #renderer}.
	 */
	private void initializeRenderer() {
		renderer.initialize();
		if (viewportSize == null)
			renderer.updateSize(1, 1);
		else
			renderer.updateSize(viewportSize.cameraWidth, viewportSize.cameraHeight);
	}

	/**
	 * Sets the {@link Renderer} of this {@link Viewport}
	 * 
	 * @param renderer the new {@link Renderer} of this {@link Viewport}
	 */
	public void setRenderer(Renderer renderer) {
		if (renderer == null)
			renderer = new ForwardsRenderer();
		if (this.renderer != null) {
			if (parent != null)
				parent.execute(this::deinitializeRenderer).join();
			this.renderer.setParent(null);
		}
		this.renderer = renderer;
		this.renderer.setParent(this);
		if (parent != null)
			parent.execute(this::initializeRenderer).join();
	}

	/**
	 * Updates the size of this viewport
	 * 
	 * @param width  width of the window to render in
	 * @param height height of the window to render in
	 */
	public void updateSize(int width, int height) {
		viewportX = (int) (width * xStart);
		viewportY = (int) (height * yStart);
		viewportWidth = (int) (width * xEnd - viewportX);
		viewportHeight = (int) (height * yEnd - viewportY);

		viewportSize = aspectMode.getSize(viewportX, viewportY, viewportWidth, viewportHeight, camera.getWidth(),
				camera.getHeight());

		updateProjection();

		parent.execute(() -> renderer.updateSize(viewportSize.cameraWidth, viewportSize.cameraHeight));
	}

	/**
	 * Gets the size of this {@link Viewport}
	 * 
	 * @return
	 */
	public AspectMode.Size getViewportSize() {
		return viewportSize;
	}

	/**
	 * Gets the x coordinate of this {@link Viewport} in the {@link Window}
	 * 
	 * @return the x coordinate of this {@link Viewport} in the {@link Window}
	 */
	public int getViewportX() {
		return viewportX;
	}

	/**
	 * Gets the y coordinate of this {@link Viewport} in the {@link Window}
	 * 
	 * @return the y coordinate of this {@link Viewport} in the {@link Window}
	 */
	public int getViewportY() {
		return viewportY;
	}

	/**
	 * Gets the width of this {@link Viewport} in the {@link Window}
	 * 
	 * @return the width of this {@link Viewport} in the {@link Window}
	 */
	public int getViewportWidth() {
		return viewportWidth;
	}

	/**
	 * Gets the height of this {@link Viewport} in the {@link Window}
	 * 
	 * @return the height of this {@link Viewport} in the {@link Window}
	 */
	public int getViewportHeight() {
		return viewportHeight;
	}

	/**
	 * Updates the projection matrix.<br />
	 * Only has to be called when the camera settings changed.
	 */
	public void updateProjection() {
		camera.updateProjection(viewportSize.cameraWidth, viewportSize.cameraHeight);
	}

	/**
	 * Renders this viewport
	 */
	@Override
	public void render() {
		if (!visible)
			return;

		renderer.render(content);
	}

	/**
	 * Passes the click event to the camera and converts the coordinates to camera
	 * coordinates
	 */
	@Override
	public void onClick(int button, int action, int modifiers, double x, double y) {
		x = (x - viewportSize.x) / viewportSize.width * viewportSize.cameraWidth;
		y = (y - viewportSize.y) / viewportSize.height * viewportSize.cameraHeight;

		if (x < 0 || x > viewportSize.cameraWidth || y < 0 || y > viewportSize.cameraHeight)
			return;

		camera.onClick(button, action, modifiers, x, y);
	}

}
