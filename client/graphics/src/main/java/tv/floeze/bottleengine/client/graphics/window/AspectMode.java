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
	 * Does not modify the viewport size.
	 * 
	 * This will add borders around the content or draw the content outside the
	 * viewport if it does not match the camera output size
	 */
	public static final AspectMode NONE = //
			(viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth, cameraHeight) -> new Size(
					viewportX + (viewportWidth - cameraWidth) / 2, viewportY + (viewportHeight - cameraHeight) / 2,
					cameraWidth, cameraHeight, cameraWidth, cameraHeight);

	/**
	 * Stretches the content to fit the viewport. This will distort the content if
	 * the aspect ratio is not the same.
	 */
	public static final AspectMode STRETCH = Size::new;

	/**
	 * Scales the content to fill the viewport while keeping the aspect ratio. This
	 * will cut off parts of the content if the aspect ratio is not the same.
	 */
	public static final AspectMode SCALE = (viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth,
			cameraHeight) -> {
		double scale = Math.max(viewportWidth / (double) cameraWidth, viewportHeight / (double) cameraHeight);
		int width = (int) (scale * cameraWidth);
		int height = (int) (scale * cameraHeight);
		return new Size(viewportX + (viewportWidth - width) / 2, viewportY + (viewportHeight - height) / 2, width,
				height, cameraWidth, cameraHeight);
	};

	/**
	 * Scales the content to keep the whole content on the viewport while also
	 * keeping the aspect ratio of the content. This will put empty areas at the
	 * side of the viewport if the aspect ratio is not the same.
	 */
	public static final AspectMode KEEP = (viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth,
			cameraHeight) -> {
		double scale = Math.min(viewportWidth / (double) cameraWidth, viewportHeight / (double) cameraHeight);
		int width = (int) (scale * cameraWidth);
		int height = (int) (scale * cameraHeight);
		return new Size(viewportX + (viewportWidth - width) / 2, viewportY + (viewportHeight - height) / 2, width,
				height, cameraWidth, cameraHeight);
	};

	/**
	 * Scales the content to fit the width of the viewport while also keeping the
	 * aspect ratio of the content. This will put empty areas or cut off parts of
	 * the content at the top/bottom of the viewport if the aspect ratio is not the
	 * same.
	 */
	public static final AspectMode KEEP_WIDTH = (viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth,
			cameraHeight) -> {
		double scale = viewportWidth / (double) cameraWidth;
		int width = (int) (scale * cameraWidth);
		int height = (int) (scale * cameraHeight);
		return new Size(viewportX + (viewportWidth - width) / 2, viewportY + (viewportHeight - height) / 2, width,
				height, cameraWidth, cameraHeight);
	};

	/**
	 * Scales the content to fit the height of the viewport while also keeping the
	 * aspect ratio of the content. This will put empty areas or cut off parts of
	 * the content at the left/right of the viewport if the aspect ratio is not the
	 * same.
	 */
	public static final AspectMode KEEP_HEIGHT = (viewportX, viewportY, viewportWidth, viewportHeight, cameraWidth,
			cameraHeight) -> {
		double scale = viewportHeight / (double) cameraHeight;
		int width = (int) (scale * cameraWidth);
		int height = (int) (scale * cameraHeight);
		return new Size(viewportX + (viewportWidth - width) / 2, viewportY + (viewportHeight - height) / 2, width,
				height, cameraWidth, cameraHeight);
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
