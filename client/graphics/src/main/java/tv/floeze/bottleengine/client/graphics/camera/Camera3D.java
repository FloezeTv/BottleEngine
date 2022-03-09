package tv.floeze.bottleengine.client.graphics.camera;

/**
 * A camera to render a 3D view.
 * 
 * @author Floeze
 *
 */
public class Camera3D extends Camera {

	private final double fov, zNear, zFar;

	public Camera3D(int width, int height, double fov, double zNear, double zFar) {
		super(width, height);
		this.fov = fov;
		this.zNear = zNear;
		this.zFar = zFar;
	}

	@Override
	public void updateProjection(int width, int height) {
		projectionMatrix.setPerspective(fov, (double) width / height, zNear, zFar);
	}

}
