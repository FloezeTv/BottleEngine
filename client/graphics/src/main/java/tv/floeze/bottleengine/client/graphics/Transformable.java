package tv.floeze.bottleengine.client.graphics;

import org.joml.AxisAngle4d;
import org.joml.Matrix4d;
import org.joml.Vector3d;

/**
 * An object that can be transformed (translated, scaled and rotated) in
 * 3d-space.
 * 
 * @author Floeze
 *
 */
public abstract class Transformable {

	private final Matrix4d model = new Matrix4d();
	private final Vector3d position = new Vector3d();
	private final Vector3d scale = new Vector3d(1);
	private final AxisAngle4d rotation = new AxisAngle4d();

	/**
	 * If you modify this transformation matrix manually, don't call
	 * {@link #updateTransform()}, as this will calculate the transformation matrix
	 * using position, rotation and scale.
	 * 
	 * @return The transform matrix of this object.
	 */
	public Matrix4d getTransform() {
		return model;
	}

	/**
	 * If you change the values of the position, call {@link #updateTransform()} to
	 * update the transformation matrix the changes.
	 * 
	 * @return The position of this object.
	 */
	public Vector3d getPosition() {
		return position;
	}

	/**
	 * If you change the values of the scale, call {@link #updateTransform()} to
	 * update the transformation matrix the changes.
	 * 
	 * @return The scale of this object.
	 */
	public Vector3d getScale() {
		return scale;
	}

	/**
	 * If you change the values of the rotation, call {@link #updateTransform()} to
	 * update the transformation matrix the changes.
	 * 
	 * @return The rotation of this object.
	 */
	public AxisAngle4d getRotation() {
		return rotation;
	}

	/**
	 * This recalculates the transformation for this object.
	 * 
	 * This method has to be called for the changes to take effect.
	 */
	public void updateTransform() {
		model.identity().translate(position).rotate(rotation).scale(scale);
	}

}
