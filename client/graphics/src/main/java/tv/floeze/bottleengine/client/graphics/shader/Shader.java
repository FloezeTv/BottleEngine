package tv.floeze.bottleengine.client.graphics.shader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.joml.*;
import org.lwjgl.opengl.GL20;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.common.io.ResourceLoader;
import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * An OpenGL Shader-Program.<br />
 * 
 * By default, this includes the following helper-methods for every shader:
 * 
 * <ul>
 * <li>Camera
 * <ul>
 * <li>{@code void set_position(mat4 model, vec3 position);} Sets the
 * gl_Position to the result of calculate_position</li>
 * <li>{@code vec4 calculate_position(mat4 model, vec3 position);} Calculates
 * the vec4 for OpenGL including the camera</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Floeze
 *
 */
public class Shader implements Disposable {

	/**
	 * Parts of a {@link Shader}
	 * 
	 * @author Floeze
	 * 
	 * @see Shader#Shader(Part...)
	 *
	 */
	public static final class Part {
		private final int type;
		private final String path;

		public Part(int type, String path) {
			this.type = type;
			this.path = path;
		}

	}

	/**
	 * The path of the common shader for camera operations
	 */
	private static final String CAMERA_COMMON_PATH = "bottleengine/shaders/camera.common.vert";

	/**
	 * The common shader for camera operations
	 */
	private static final RunnerLocal<Integer> cameraCommon = new RunnerLocal<>(() -> {
		int shader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(shader, ResourceLoader.loadTextFromResources(CAMERA_COMMON_PATH));
		glCompileShader(shader);

		if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile common camera shader", glGetShaderInfoLog(shader));

		return shader;
	}, GL20::glDeleteShader);

	private final int program;

	/**
	 * Creates a new shader from multiple parts
	 * 
	 * @param parts parts of shader
	 * @see Part
	 */
	public Shader(Part... parts) {
		program = glCreateProgram();

		// list of created shaders for deletion
		List<Integer> glShaders = new ArrayList<>();

		try {
			glAttachShader(program, cameraCommon.get(this));

			for (Part part : parts) {
				int shader = glCreateShader(part.type);
				glShaders.add(shader);

				glShaderSource(shader, ResourceLoader.loadTextFromResources(part.path));
				glCompileShader(shader);
				glAttachShader(program, shader);

				if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
					throw new ShaderException("Failed to compile shader " + part.path + " (" + part.type + ")",
							glGetShaderInfoLog(shader));
			}

			glLinkProgram(program);
			if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE)
				throw new ShaderException("Failed to link shader", glGetProgramInfoLog(program));

		} finally {
			for (int shader : glShaders)
				glDeleteShader(shader);
		}
	}

	/**
	 * Creates a new shader
	 * 
	 * @param vertex   path to vertex shader
	 * @param fragment path to fragment shader
	 */
	public Shader(String vertex, String fragment) {
		this(new Part(GL_VERTEX_SHADER, vertex), new Part(GL_FRAGMENT_SHADER, fragment));
	}

	/**
	 * Use this shader on the current thread
	 */
	public void use() {
		glUseProgram(program);
	}

	public void set(String name, boolean b) {
		glUniform1i(getUniformLocation(name), b ? GL_TRUE : GL_FALSE);
	}

	public void set(String name, int i) {
		glUniform1i(getUniformLocation(name), i);
	}

	public void set(String name, int[] i) {
		glUniform1iv(getUniformLocation(name), i);
	}

	public void set(String name, float f) {
		glUniform1f(getUniformLocation(name), f);
	}

	public void set(String name, float[] f) {
		glUniform1fv(getUniformLocation(name), f);
	}

	public void set(String name, double d) {
		glUniform1d(getUniformLocation(name), d);
	}

	public void set(String name, double[] d) {
		glUniform1dv(getUniformLocation(name), d);
	}

	public void set(String name, Vector2fc vector) {
		glUniform2f(getUniformLocation(name), vector.x(), vector.y());
	}

	public void set(String name, Vector2dc vector) {
		glUniform2f(getUniformLocation(name), (float) vector.x(), (float) vector.y());
	}

	public void setDouble(String name, Vector2dc vector) {
		glUniform2d(getUniformLocation(name), vector.x(), vector.y());
	}

	public void set(String name, Vector3fc vector) {
		glUniform3f(getUniformLocation(name), vector.x(), vector.y(), vector.z());
	}

	public void set(String name, Vector3dc vector) {
		glUniform3f(getUniformLocation(name), (float) vector.x(), (float) vector.y(), (float) vector.z());
	}

	public void setDouble(String name, Vector3dc vector) {
		glUniform3d(getUniformLocation(name), vector.x(), vector.y(), vector.z());
	}

	public void set(String name, Color color) {
		glUniform3f(getUniformLocation(name), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
	}

	public void set(String name, Matrix4fc matrix) {
		glUniformMatrix4fv(getUniformLocation(name), false, matrix.get(new float[4 * 4]));
	}

	public void set(String name, Matrix4dc matrix) {
		glUniformMatrix4fv(getUniformLocation(name), false, matrix.get(new float[4 * 4]));
	}

	public void setDouble(String name, Matrix4dc matrix) {
		glUniformMatrix4dv(getUniformLocation(name), false, matrix.get(new double[4 * 4]));
	}

	private int getUniformLocation(String name) {
		return glGetUniformLocation(program, name);
	}

	@Override
	public void dispose() {
		glDeleteProgram(program);
	}

	/**
	 * Use no shader on the current thread
	 */
	public static void useNone() {
		glUseProgram(0);
	}

}
