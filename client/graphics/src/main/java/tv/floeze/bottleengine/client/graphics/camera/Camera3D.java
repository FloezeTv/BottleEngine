package tv.floeze.bottleengine.client.graphics.camera;

import tv.floeze.bottleengine.client.graphics.window.Viewport;

/**
 * A camera to render a 3D view.
 * 
 * @author Floeze
 *
 */
public class Camera3D extends Camera {

	private double fov, zNear, zFar;

	/**
	 * Creates a new 3D camera
	 * 
	 * @param width  the width of the camera
	 * @param height the height of the camera
	 * @param fov    the fov of the camera
	 * @param zNear  the near plane of the camera
	 * @param zFar   the far plane of the camera
	 */
	public Camera3D(int width, int height, double fov, double zNear, double zFar) {
		super(width, height);
		this.fov = fov;
		this.zNear = zNear;
		this.zFar = zFar;
	}

	public final double getFov() {
		return fov;
	}

	/**
	 * Sets the fov of this camera.<br />
	 * Call {@link Viewport#updateProjection()} after.
	 * 
	 * @param fov the new fov
	 */
	public final void setFov(double fov) {
		this.fov = fov;
	}

	public final double getzNear() {
		return zNear;
	}

	/**
	 * Sets the near plane of this camera.<br />
	 * Call {@link Viewport#updateProjection()} after.
	 * 
	 * @param zNear the new near plane
	 */
	public final void setzNear(double zNear) {
		this.zNear = zNear;
	}

	public final double getzFar() {
		return zFar;
	}

	/**
	 * Sets the far plane of this camera.<br />
	 * Call {@link Viewport#updateProjection()} after.
	 * 
	 * @param zFar the new far plane
	 */
	public final void setzFar(double zFar) {
		this.zFar = zFar;
	}

	@Override
	public void updateProjection(int width, int height) {
		projectionMatrix.setPerspective(fov, (double) width / height, zNear, zFar);
	}

}
