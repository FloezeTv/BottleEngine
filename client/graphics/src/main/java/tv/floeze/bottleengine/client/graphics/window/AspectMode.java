package tv.floeze.bottleengine.client.graphics.window;

import tv.floeze.bottleengine.client.graphics.camera.Camera;

/**
 * An {@link AspectMode} calculates the size of the {@link Viewport} and
 * {@link Camera} to follow certain rules.
 * 
 * @author Floeze
 *
 */
@FunctionalInterface
public interface AspectMode {

	/**
	 * Scales the content to fit the viewport stretching the content if necessary
	 */
	public static final AspectMode SCALE = //
			(viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth, cameraHeight) -> {
				return new Size(viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth, cameraHeight);
			};

	/**
	 * Size returned by {@link AspectMode#getSize(int, int, int, int, int, int)}
	 * 
	 * @author Floeze
	 *
	 */
	public static final class Size {
		/**
		 * Viewport size
		 */
		public int x, y, width, height;
		/**
		 * Camera size
		 */
		public int cameraWidth, cameraHeight;

		public Size(int x, int y, int width, int height, int cameraWidth, int cameraHeight) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.cameraWidth = cameraWidth;
			this.cameraHeight = cameraHeight;
		}

		public Size() {
		}

		@Override
		public String toString() {
			return "Size [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", cameraWidth="
					+ cameraWidth + ", cameraHeight=" + cameraHeight + "]";
		}

	}

	/**
	 * Calculates the size of the {@link Viewport} and {@link Camera}
	 * 
	 * @param viewportX      x coordinate of the {@link Viewport}
	 * @param viewportY      y coordinate of the {@link Viewport}
	 * @param viewportWidth  width of the {@link Viewport}
	 * @param viewportHeight height of the {@link Viewport}
	 * @param cameraWidth    width of the {@link Camera}
	 * @param cameraHeight   height of the {@link Camera}
	 * @return the {@link Size} for {@link Viewport} and {@link Camera}
	 */
	public Size getSize(int viewportX, int viewportY, int viewportWidth, int viewportHeight, int cameraWidth,
			int cameraHeight);

}
