package tv.floeze.bottleengine.client.graphics.camera;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import tv.floeze.bottleengine.client.graphics.Transformable;
import tv.floeze.bottleengine.client.graphics.click.ClickListener;
import tv.floeze.bottleengine.client.graphics.click.Clickable;
import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.client.graphics.window.Viewport;

/**
 * A camera to render the scene from. <br />
 * 
 * {@link #onClick(int, int, int, double, double)} expects the coordinates in
 * the cameras coordinate system (0 - {@link #getWidth()}, 0 -
 * {@link #getHeight()}).<br />
 * 
 * This uses uniform buffer at index 0 for view and projection matrix:
 * 
 * <pre>
 * layout (std140) uniform Camera {
 *     mat4 view;
 *     mat4 projection;
 * };
 * </pre>
 * 
 * <br />
 *
 * It is however recommended to use the helper functions present when using the
 * {@link Shader}.
 * 
 * @author Floeze
 *
 */
public abstract class Camera extends Transformable implements ClickListener {

	private final int UBO;

	protected Matrix4d projectionMatrix = new Matrix4d();

	private int width, height;

	private final List<Clickable> clickables = new ArrayList<>();

	/**
	 * Matrix for storing the inverted transformation matrix
	 */
	private final Matrix4d viewMatrix = new Matrix4d();

	/**
	 * Creates a new camera with the specified width and height.
	 * 
	 * @param width  width of the camera
	 * @param height height of the camera
	 */
	public Camera(int width, int height) {
		this.width = width;
		this.height = height;

		UBO = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
		// allocate buffer for 2 matrices with 4 * 4 floats
		glBufferData(GL_UNIFORM_BUFFER, (2 * (4 * 4)) * Float.BYTES, GL_DYNAMIC_DRAW);

		updateProjection(width, height);
	}

	/**
	 * Gets the width of the camera.
	 * 
	 * @return the width of the camera
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height of the camera.
	 * 
	 * @return the height of the camera
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the matrices. This does not update the matrices.
	 */
	public void setMatrices() {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);

		glBufferSubData(GL_UNIFORM_BUFFER, (0 * (4 * 4)) * Float.BYTES, viewMatrix.get(new float[4 * 4]));
		glBufferSubData(GL_UNIFORM_BUFFER, (1 * (4 * 4)) * Float.BYTES, projectionMatrix.get(new float[4 * 4]));

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, UBO);
	}

	/**
	 * Updates the view matrix manually.<br />
	 * 
	 * This only needs to be called if the transformation was changed without
	 * calling {@link #updateTransform()}.
	 */
	public void updateView() {
		getTransform().invert(viewMatrix);
	}

	@Override
	public void updateTransform() {
		super.updateTransform();
		updateView();
	}

	/**
	 * Adds a {@link Clickable} to call when the {@link Viewport} of this
	 * {@link Camera} is called
	 * 
	 * @param clickable {@link Clickable} to call
	 */
	public void addClickable(Clickable clickable) {
		if (!clickables.contains(clickable))
			clickables.add(clickable);
	}

	/**
	 * Removes a previously added {@link Clickable}
	 * 
	 * @param clickable {@link Clickable} to remove
	 * 
	 * @see #addClickable(Clickable)
	 */
	public void removeClickable(Clickable clickable) {
		clickables.remove(clickable);
	}

	/**
	 * Maps a value from a range to another range
	 * 
	 * @param value   value to map
	 * @param fromMin input range lower bound
	 * @param fromMax input range upper bound
	 * @param toMin   output range lower bound
	 * @param toMax   output range upper bound
	 * @return the mapped value
	 */
	private static double mapToRange(double value, double fromMin, double fromMax, double toMin, double toMax) {
		return (value - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
	}

	protected abstract Vector3d getClickFrom(double x, double y);

	@Override
	public void onClick(int button, int action, int modifiers, double x, double y) {
		Matrix4d projInv = projectionMatrix.invert(new Matrix4d());
		Matrix4d viewInv = getTransform(); // view is inverted transform

		x = mapToRange(x, 0, getWidth(), -1, 1);
		y = mapToRange(y, 0, getHeight(), 1, -1); // need to map between screen (y-down) and OpenGL (y-up)

		Vector3d from = getClickFrom(x, y);
		Vector3d to = new Vector3d(x, y, 0);

		from.mulPosition(projInv);
		to.mulPosition(projInv);

		from.mulPosition(viewInv);
		to.mulPosition(viewInv);

		Clickable.Ray ray = new Clickable.Ray(from, to.sub(from));

		for (Clickable c : clickables)
			c.click(button, action, modifiers, ray);
	}

	/**
	 * Updates the projection matrix
	 * 
	 * @param width  width to use (see {@link #getWidth()})
	 * @param height height to use (see {@link #getHeight()})
	 */
	public abstract void updateProjection(int width, int height);

}
