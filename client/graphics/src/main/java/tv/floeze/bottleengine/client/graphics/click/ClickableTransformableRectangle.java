package tv.floeze.bottleengine.client.graphics.click;

import org.joml.Vector3d;

/**
 * A {@link ClickableTransformable} that checks if the {@link Ray} intersects
 * the {@link Rectangle} bounds of this object.
 * 
 * @author Floeze
 *
 */
public abstract class ClickableTransformableRectangle extends ClickableTransformable {

	private Clickable2D clickable;

	/**
	 * A {@link Rectangle} that represents the bounds of an object.
	 * 
	 * @author Floeze
	 *
	 */
	public static class Rectangle {
		public final double x, y, width, height;

		public Rectangle(double x, double y, double width, double height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

	}

	/**
	 * Gets the bound of this object.
	 * 
	 * @return
	 */
	protected abstract Rectangle getBounds();

	/**
	 * Sets the listener to call when this object is clicked.
	 * 
	 * @param clickable The {@link Clickable2D} to call
	 */
	public void setClickable(Clickable2D clickable) {
		this.clickable = clickable;
	}

	@Override
	public void clickLocal(Ray ray) {
		if (clickable == null) // no listener
			return;

		if (Math.abs(ray.direction.z()) <= 1E-20) { // parallel
			if (Math.abs(ray.position.z()) <= 1E-20) // infinite intersection
				clickable.onClick(0.5, 0.5);
			// no intersection
			return;
		}

		// R = ray.pos + lambda * ray.direction
		// R.z != 0

		double lambda = -ray.position.z() / ray.direction.z();

		Vector3d position = ray.position.add(ray.direction.mul(lambda, new Vector3d()), new Vector3d());

		Rectangle bounds = getBounds();

		// check if intersection
		if (position.x >= bounds.x && position.x <= bounds.x + bounds.width //
				&& position.y >= bounds.y && position.y <= bounds.y + bounds.height) {
			clickable.onClick(position.x + 0.5, position.y + 0.5);
		}
	}
}
