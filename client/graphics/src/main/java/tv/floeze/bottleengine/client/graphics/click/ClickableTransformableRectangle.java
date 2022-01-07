package tv.floeze.bottleengine.client.graphics.click;

import java.util.List;

import java.util.ArrayList;

import org.joml.Vector3d;

/**
 * A {@link ClickableTransformable} that checks if the {@link Ray} intersects
 * the {@link Rectangle} bounds of this object.
 * 
 * @author Floeze
 *
 */
public abstract class ClickableTransformableRectangle extends ClickableTransformable {

	private final List<Clickable2D> clickables = new ArrayList<>();

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
	 * Adds a listener to call when this object is clicked.
	 * 
	 * @param clickable A {@link Clickable2D} to call
	 */
	public void addClickable(Clickable2D clickable) {
		if (!clickables.contains(clickable))
			clickables.add(clickable);
	}

	public void removeClickable(Clickable2D clickable) {
		clickables.remove(clickable);
	}

	@Override
	public void clickLocal(Ray ray) {
		if (clickables.isEmpty()) // no listener
			return;

		if (Math.abs(ray.direction.z()) <= 1E-20) { // parallel
			if (Math.abs(ray.position.z()) <= 1E-20) // infinite intersection
				callClickables(0.5, 0.5);
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
			callClickables(position.x + 0.5, position.y + 0.5);
		}
	}

	private void callClickables(double x, double y) {
		for (Clickable2D clickable : clickables)
			clickable.onClick(x, y);
	}
}
