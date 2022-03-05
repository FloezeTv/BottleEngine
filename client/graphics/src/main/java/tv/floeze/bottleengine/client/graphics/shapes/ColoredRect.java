package tv.floeze.bottleengine.client.graphics.shapes;

import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * A colored rectangle.
 * 
 * @author Floeze
 *
 */
public class ColoredRect extends ColoredShape {

	private static final RunnerLocal<Shader> shaders = new RunnerLocal<>(
			() -> new Shader("bottleengine/shaders/rect.vert", "bottleengine/shaders/rect.frag"), Shader::dispose);

	public ColoredRect() {
		setShader(shaders.get(this));
	}

	@Override
	protected void disposeShader() {
		shaders.abandon(this);
	}

}
