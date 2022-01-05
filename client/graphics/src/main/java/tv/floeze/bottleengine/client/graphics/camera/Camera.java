package tv.floeze.bottleengine.client.graphics.camera;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import org.joml.Matrix4d;
import org.joml.Matrix4f;

import tv.floeze.bottleengine.client.graphics.ClickListener;
import tv.floeze.bottleengine.client.graphics.Transformable;

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
 * 
 * @author Floeze
 *
 */
public abstract class Camera extends Transformable implements ClickListener {

	private final int UBO;

	protected Matrix4f projectionMatrix = new Matrix4f();

	private int width, height;

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
	 * Updates the projection matrix
	 * 
	 * @param width  width to use (see {@link #getWidth()})
	 * @param height height to use (see {@link #getHeight()})
	 */
	public abstract void updateProjection(int width, int height);

}
