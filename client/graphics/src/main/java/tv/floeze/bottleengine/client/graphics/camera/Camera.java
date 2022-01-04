package tv.floeze.bottleengine.client.graphics.camera;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import tv.floeze.bottleengine.client.graphics.ClickListener;

/**
 * A camera to render the scene from. <br />
 * 
 * {@link #onClick(int, int, int, double, double)} expects the coordinates in
 * the cameras coordinate system (0 - {@link #getWidth()}, 0 -
 * {@link #getHeight()}).
 * 
 * @author Floeze
 *
 */
public abstract class Camera implements ClickListener {

	private final int UBO;

	protected Matrix4f projectionMatrix = new Matrix4f();
	protected Matrix4f viewMatrix = new Matrix4f();

	private Vector3f position = new Vector3f();

	private int width, height;

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
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBufferRange(GL_UNIFORM_BUFFER, 0, UBO, 0, (2 * (4 * 4)) * Float.BYTES);

		updateView();
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
	 * Sets the position of the camera and updates the matrix.
	 * 
	 * @param position the new position of the camera
	 */
	public void setPosition(Vector3f position) {
		this.position = position;
		updateView();
	}

	/**
	 * Gets the current position of the camera as a read-only {@link Vector3fc}.
	 * 
	 * @return the read-only position of the camera
	 */
	public Vector3fc getPosition() {
		return position;
	}

	/**
	 * Sets the matrices. This does not update the matrices.
	 */
	public void setMatrices() {
		glBindBuffer(GL_UNIFORM_BUFFER, UBO);

		glBufferSubData(GL_UNIFORM_BUFFER, (0 * (4 * 4)) * Float.BYTES, viewMatrix.get(new float[4 * 4]));
		glBufferSubData(GL_UNIFORM_BUFFER, (1 * (4 * 4)) * Float.BYTES, projectionMatrix.get(new float[4 * 4]));

		glBindBuffer(GL_UNIFORM_BUFFER, UBO);
	}

	/**
	 * Updates the view matrix
	 */
	public void updateView() {
		viewMatrix = new Matrix4f().translate(position);
	}

	/**
	 * Updates the projection matrix
	 * 
	 * @param width  width to use (see {@link #getWidth()})
	 * @param height height to use (see {@link #getHeight()})
	 */
	public abstract void updateProjection(int width, int height);

}
