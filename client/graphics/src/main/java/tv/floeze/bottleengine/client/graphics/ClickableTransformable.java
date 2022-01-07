package tv.floeze.bottleengine.client.graphics;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import tv.floeze.bottleengine.client.graphics.Clickable.Ray;
import tv.floeze.bottleengine.client.graphics.camera.Camera;

/**
 * A {@link Clickable} that is also a {@link Transformable}.<br />
 * This will project the {@link Ray} into the local space of the
 * {@link Transformable}.
 * 
 * @author Floeze
 *
 */
public abstract class ClickableTransformable extends Transformable implements Clickable {

	/**
	 * Checks if this {@link Clickable} is intersected by the {@link Ray} and
	 * performs actions if clicked.<br />
	 * The {@link Ray} is projected into this {@link Transformable}s local space.
	 * 
	 * @param ray {@link Ray} projected from a {@link Camera} into the local space
	 *            of this {@link Transformable}
	 */
	public abstract void clickLocal(Ray ray);

	@Override
	public void click(Ray ray) {
		Matrix4d invertedTransform = getTransform().invert(new Matrix4d());
		Clickable.Ray localRay = new Clickable.Ray(ray.position.mulPosition(invertedTransform, new Vector3d()),
				ray.direction.mulDirection(invertedTransform, new Vector3d()).normalize());
		clickLocal(localRay);
	}

}
