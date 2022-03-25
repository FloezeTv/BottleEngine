package tv.floeze.bottleengine.client.graphics.deferred;

import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A {@link DeferredLight} that illuminates the whole world.
 * 
 * @author Floeze
 *
 */
public class AmbientDeferredLight extends DeferredLight {

	private static final RunnerLocalShader shaders = new RunnerLocalShader("bottleengine/shaders/light.vert",
			"bottleengine/shaders/ambientLight.frag");

	/**
	 * The strength of this light
	 */
	private float strength;

	/**
	 * Creates a new {@link AmbientDeferredLight} with a default strength of
	 * {@code 0.1}
	 */
	public AmbientDeferredLight() {
		this(0.1f);
	}

	/**
	 * Creates a new {@link AmbientDeferredLight} with a given strength
	 * 
	 * @param strength the strength of this {@link AmbientDeferredLight}
	 */
	public AmbientDeferredLight(float strength) {
		super(shaders);
		this.strength = strength;
	}

	/**
	 * Changes the strength of this {@link AmbientDeferredLight}
	 * 
	 * @param strength the new strength of this {@link AmbientDeferredLight}
	 */
	public void setStrength(float strength) {
		this.strength = strength;
	}

	@Override
	protected void setUniforms(Shader shader) {
		shader.set("strength", strength);
	}

}
