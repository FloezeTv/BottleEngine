package tv.floeze.bottleengine.client.graphics.shader;

import static org.lwjgl.opengl.GL20.*;

import tv.floeze.bottleengine.common.io.ResourceLoader;

/**
 * An OpenGL Shader-Program
 * 
 * @author Floeze
 *
 */
public class Shader {

	private final int program;

	/**
	 * Creates a new shader
	 * 
	 * @param vertex   path to vertex shader
	 * @param fragment path to fragment shader
	 */
	public Shader(String vertex, String fragment) {
		String vertexSource = ResourceLoader.loadTextFromResources(vertex);
		String fragmentSource = ResourceLoader.loadTextFromResources(fragment);

		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexSource);
		glCompileShader(vertexShader);
		if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile vertex shader " + vertex, glGetShaderInfoLog(vertexShader));

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentSource);
		glCompileShader(fragmentShader);
		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile fragment shader " + fragment,
					glGetShaderInfoLog(fragmentShader));

		program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to link shader", glGetProgramInfoLog(program));

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}

	/**
	 * Use this shader on the current thread
	 */
	public void use() {
		glUseProgram(program);
	}

	/**
	 * Use no shader on the current thread
	 */
	public static void useNone() {
		glUseProgram(0);
	}

}
