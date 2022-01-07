package tv.floeze.bottleengine.client.graphics.camera;

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
		projectionMatrix.setOrtho2D(-width / 2f, width / 2f, height / 2f, -height / 2f);
	}

}
