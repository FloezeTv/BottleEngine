package tv.floeze.bottleengine.client.graphics.shapes;

import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;

/**
 * A colored circle. <br />
 * Does not have to be a circle; it can be made into an ellipse using
 * {@link #getScale()} to scale the x-direction independent of the y-direction.
 * 
 * @author Floeze
 *
 */
public class ColoredCircle extends ColoredShape {

	private static final RunnerLocalShader shaders = new RunnerLocalShader("bottleengine/shaders/circle.vert",
			"bottleengine/shaders/circle.frag");

	public ColoredCircle() {
		setShader(shaders.get(this));
	}

	@Override
	protected void disposeShader() {
		shaders.abandon(this);
	}

}
