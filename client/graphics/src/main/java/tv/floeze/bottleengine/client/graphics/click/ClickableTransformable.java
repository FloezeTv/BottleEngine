package tv.floeze.bottleengine.client.graphics.click;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import tv.floeze.bottleengine.client.graphics.Transformable;
import tv.floeze.bottleengine.client.graphics.camera.Camera;
import tv.floeze.bottleengine.client.graphics.click.Clickable.Ray;

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
	 * @param button    index of the mouse button
	 * @param action    if the button was pressed or released
	 * @param modifiers if any modifier keys were pressed
	 * @param ray       {@link Ray} projected from a {@link Camera} into the local
	 *                  space of this {@link Transformable}
	 */
	public abstract void clickLocal(int button, int action, int modifiers, Ray ray);

	@Override
	public void click(int button, int action, int modifiers, Ray ray) {
		Matrix4d invertedTransform = getTransform().invert(new Matrix4d());
		Clickable.Ray localRay = new Clickable.Ray(ray.position.mulPosition(invertedTransform, new Vector3d()),
				ray.direction.mulDirection(invertedTransform, new Vector3d()).normalize());
		clickLocal(button, action, modifiers, localRay);
	}

}
