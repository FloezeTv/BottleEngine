package tv.floeze.bottleengine.client.graphics.deferred;

import java.awt.Color;

import org.joml.Vector3d;

import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A {@link DeferredLight} that illuminates the world from a single point.
 * 
 * @author Floeze
 *
 */
public class PointDeferredLight extends DeferredLight {

	private static final RunnerLocalShader shaders = new RunnerLocalShader("bottleengine/shaders/light.vert",
			"bottleengine/shaders/pointLight.frag");

	/**
	 * The position of this {@link PointDeferredLight}
	 */
	private final Vector3d position;

	/**
	 * The color of this {@link PointDeferredLight}
	 */
	private Color color;

	/**
	 * The strength of this {@link PointDeferredLight}
	 */
	private float strength = 1;
	/**
	 * The linear attenuation of this {@link PointDeferredLight}
	 */
	private float linearAttenuation = 0;
	/**
	 * The quadratic attenuation of this {@link PointDeferredLight}
	 */
	private float quadraticAttenuation = 0;

	/**
	 * Creates a new white {@link PointDeferredLight} at {@code (0, 0, 0)}
	 */
	public PointDeferredLight() {
		this(new Vector3d(0, 0, 00), Color.WHITE);
	}

	/**
	 * Creates a new {@link PointDeferredLight}
	 * 
	 * @param position the position of this {@link PointDeferredLight}
	 * @param color    the color of this {@link PointDeferredLight}
	 */
	public PointDeferredLight(Vector3d position, Color color) {
		super(shaders);
		this.position = position;
		this.color = color;
	}

	/**
	 * Gets the position of this {@link PointDeferredLight}
	 * 
	 * @return the position of this {@link PointDeferredLight}
	 */
	public Vector3d getPosition() {
		return position;
	}

	/**
	 * Sets the color of this {@link PointDeferredLight}
	 * 
	 * @param color the new color of this {@link PointDeferredLight}
	 */
	public void setColor(Color color) {
		if (color == null)
			color = Color.BLACK;
		this.color = color;
	}

	/**
	 * Sets the strength of this {@link PointDeferredLight}
	 * 
	 * @param strength the new strength of this {@link PointDeferredLight}
	 */
	public void setStrength(float strength) {
		this.strength = strength;
	}

	/**
	 * Sets the linear attenuation of this {@link PointDeferredLight}
	 * 
	 * @param linearAttenuation the new linear attenuation of this
	 *                          {@link PointDeferredLight}
	 */
	public void setLinearAttenuation(float linearAttenuation) {
		this.linearAttenuation = linearAttenuation;
	}

	/**
	 * Sets the quadratic attenuation of this {@link PointDeferredLight}
	 * 
	 * @param quadraticAttenuation the new quadratic attenuation of this
	 *                             {@link PointDeferredLight}
	 */
	public void setQuadraticAttenuation(float quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	@Override
	protected void setUniforms(Shader shader) {
		shader.set("lightPosition", position);
		shader.set("lightColor", color);
		shader.set("strength", strength);
		shader.set("linearAttenuation", linearAttenuation);
		shader.set("quadraticAttenuation", quadraticAttenuation);
	}

}
