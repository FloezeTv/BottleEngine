package tv.floeze.bottleengine.client.graphics.shapes;

import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;

/**
 * A colored rectangle.
 * 
 * @author Floeze
 *
 */
public class ColoredRect extends ColoredShape {

	private static final RunnerLocalShader shaders = new RunnerLocalShader("bottleengine/shaders/rect.vert",
			"bottleengine/shaders/rect.frag");

	public ColoredRect() {
		setShader(shaders.get(this));
	}

	@Override
	protected void disposeShader() {
		shaders.abandon(this);
	}

}
