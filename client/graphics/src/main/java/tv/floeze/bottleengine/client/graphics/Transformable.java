package tv.floeze.bottleengine.client.graphics;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * An object that can be transformed (translated, scaled and rotated) in
 * 3d-space.
 * 
 * @author Floeze
 *
 */
public abstract class Transformable {

	/**
	 * Two model matrices:<br />
	 * {@code model[currentModelIndex]} is the model to read.<br />
	 * {@code model[getUnusedModelIndex()]} is the model to write to.<br />
	 * After writing, set {@link #currentModelIndex} to
	 * {@link #getUnusedModelIndex()}
	 */
	private final Matrix4d[] model = { new Matrix4d(), new Matrix4d() };
	private int currentModelIndex = 0;
	private final Vector3d position = new Vector3d();
	private final Vector3d scale = new Vector3d(1);
	private final Quaterniond rotation = new Quaterniond();

	/**
	 * If you modify this transformation matrix manually, don't call
	 * {@link #updateTransform()}, as this will calculate the transformation matrix
	 * using position, rotation and scale.
	 * 
	 * @return The transform matrix of this object.
	 */
	public Matrix4d getTransform() {
		return model[currentModelIndex];
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
	 * update the transformation matrix the changes. <br />
	 * Do <b>not</b> use to scale, as the scale will not be applied.
	 * 
	 * @return The rotation of this object.
	 */
	public Quaterniond getRotation() {
		return rotation;
	}

	/**
	 * This recalculates the transformation for this object.
	 * 
	 * This method has to be called for the changes to take effect.
	 */
	public void updateTransform() {
		int edit = getUnusedModelIndex();
		model[edit].identity().translate(position).rotate(rotation.normalize()).scale(scale);
		currentModelIndex = edit;
	}

	/**
	 * Gets the index of the currently unused model
	 * 
	 * @return a index for {@link #model} that is not {@link #currentModelIndex}
	 */
	private int getUnusedModelIndex() {
		return (currentModelIndex + 1) % model.length;
	}

}
