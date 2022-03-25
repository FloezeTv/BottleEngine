package tv.floeze.bottleengine.client.graphics.deferred;

import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A {@link DeferredLight} that renders the contents of the deferred buffers to
 * the screen.
 * 
 * @author Floeze
 *
 */
public class DebugDeferredLight extends DeferredLight {

	private static final RunnerLocalShader shader = new RunnerLocalShader("bottleengine/shaders/light.vert",
			"bottleengine/shaders/debugLight.frag");

	public DebugDeferredLight() {
		super(shader);
	}

	@Override
	protected void setUniforms(Shader shader) {
		// No uniforms to set
	}

}
