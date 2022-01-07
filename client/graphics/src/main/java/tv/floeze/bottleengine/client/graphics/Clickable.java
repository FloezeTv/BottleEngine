package tv.floeze.bottleengine.client.graphics;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import tv.floeze.bottleengine.client.graphics.camera.Camera;

/**
 * A {@link Clickable} can check for being clicked by a ray projected from a
 * {@link Camera}
 * 
 * @author Floeze
 *
 */
public interface Clickable {

	/**
	 * Ray projected from {@link Camera}
	 * 
	 * @author Floeze
	 *
	 */
	public static class Ray {
		/**
		 * A point the ray goes through
		 */
		public final Vector3dc position;
		/**
		 * The direction of the ray
		 */
		public final Vector3dc direction;

		public Ray(Vector3d position, Vector3d direction) {
			this.position = position;
			this.direction = direction;
		}

		@Override
		public String toString() {
			return "Ray: " + position + " + t * " + direction + "; t in R";
		}
	}

	/**
	 * Checks if this {@link Clickable} is intersected by the {@link Ray} and
	 * performs actions if clicked
	 * 
	 * @param ray {@link Ray} projected from a {@link Camera}
	 */
	public void click(Ray ray);

}
