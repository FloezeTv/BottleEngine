package tv.floeze.bottleengine.client.graphics.camera;

import org.joml.Matrix4f;

/**
 * A camera to render a 2D view.
 * 
 * @author Floeze
 *
 */
public class Camera2D extends Camera {

	public Camera2D(int width, int height) {
		super(width, height);
	}

	@Override
	public void updateProjection(int width, int height) {
		projectionMatrix = new Matrix4f().ortho2D(0, width, height, 0);
	}

	@Override
	public void onClick(int button, int action, int modifiers, double x, double y) {
		// TODO: handle the clicks
		System.out.println("Camera clicked on (" + x + ", " + y + ")");
	}

}
