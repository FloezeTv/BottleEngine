package tv.floeze.bottleengine.client.graphics.shader;

import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * A shorthand for a {@link RunnerLocal} with a Shader.
 * 
 * @author Floeze
 *
 */
public class RunnerLocalShader extends RunnerLocal<Shader> {

	/**
	 * @param parts
	 * 
	 * @see Shader#Shader(Shader.Part...)
	 */
	public RunnerLocalShader(Shader.Part... parts) {
		super(() -> new Shader(parts), Shader::dispose);
	}

	/**
	 * @param vertex
	 * @param fragment
	 * 
	 * @see Shader#Shader(String, String)
	 */
	public RunnerLocalShader(String vertex, String fragment) {
		super(() -> new Shader(vertex, fragment), Shader::dispose);
	}

}
