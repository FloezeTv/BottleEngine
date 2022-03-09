package tv.floeze.bottleengine.client.graphics.shader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
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
	 * The path of the common vertex shader for camera operations
	 */
	private static final String CAMERA_COMMON_VERTEX_PATH = "bottleengine/shaders/camera.common.vert";
	/**
	 * The path of the common geometry shader for camera operations
	 */
	private static final String CAMERA_COMMON_GEOMETRY_PATH = "bottleengine/shaders/camera.common.geom";
	/**
	 * The path of the common fragment shader for camera operations
	 */
	private static final String CAMERA_COMMON_FRAGMENT_PATH = "bottleengine/shaders/camera.common.frag";

	/**
	 * The common vertex shader for camera operations
	 */
	private static final RunnerLocal<Integer> cameraCommonVertex = new RunnerLocal<>(() -> {
		int shader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(shader, ResourceLoader.loadTextFromResources(CAMERA_COMMON_VERTEX_PATH));
		glCompileShader(shader);

		if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile common camera vertex shader", glGetShaderInfoLog(shader));

		return shader;
	}, GL20::glDeleteShader);
	/**
	 * The common geometry shader for camera operations
	 */
	private static final RunnerLocal<Integer> cameraCommonGeometry = new RunnerLocal<>(() -> {
		int shader = glCreateShader(GL_GEOMETRY_SHADER);
		glShaderSource(shader, ResourceLoader.loadTextFromResources(CAMERA_COMMON_GEOMETRY_PATH));
		glCompileShader(shader);

		if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile common camera geometry shader", glGetShaderInfoLog(shader));

		return shader;
	}, GL20::glDeleteShader);
	/**
	 * The common fragment shader for camera operations
	 */
	private static final RunnerLocal<Integer> cameraCommonFragment = new RunnerLocal<>(() -> {
		int shader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(shader, ResourceLoader.loadTextFromResources(CAMERA_COMMON_FRAGMENT_PATH));
		glCompileShader(shader);

		if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
			throw new ShaderException("Failed to compile common camera fragment shader", glGetShaderInfoLog(shader));

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
			// if the common shaders have been added
			boolean addedCommonVertex = false;
			boolean addedCommonGeometry = false;
			boolean addedCommonFragment = false;

			for (Part part : parts) {
				// add the common shaders if needed but not already added
				if (!addedCommonVertex && part.type == GL_VERTEX_SHADER) {
					glAttachShader(program, cameraCommonVertex.get(this));
					addedCommonVertex = true;
				}
				if (!addedCommonGeometry && part.type == GL_GEOMETRY_SHADER) {
					glAttachShader(program, cameraCommonGeometry.get(this));
					addedCommonGeometry = true;
				}
				if (!addedCommonFragment && part.type == GL_FRAGMENT_SHADER) {
					glAttachShader(program, cameraCommonFragment.get(this));
					addedCommonFragment = true;
				}

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

	public void set(String name, Vector2ic vector) {
		glUniform2i(getUniformLocation(name), vector.x(), vector.y());
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

	public void set(String name, Vector3ic vector) {
		glUniform3i(getUniformLocation(name), vector.x(), vector.y(), vector.z());
	}

	public void set(String name, Vector4fc vector) {
		glUniform4f(getUniformLocation(name), vector.x(), vector.y(), vector.z(), vector.w());
	}

	public void set(String name, Vector4dc vector) {
		glUniform4f(getUniformLocation(name), (float) vector.x(), (float) vector.y(), (float) vector.z(),
				(float) vector.w());
	}

	public void setDouble(String name, Vector4dc vector) {
		glUniform4d(getUniformLocation(name), vector.x(), vector.y(), vector.z(), vector.w());
	}

	public void set(String name, Vector4ic vector) {
		glUniform4i(getUniformLocation(name), vector.x(), vector.y(), vector.z(), vector.w());
	}

	public void set(String name, Color color) {
		glUniform3f(getUniformLocation(name), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
	}

	public void setWithAlpha(String name, Color color) {
		glUniform4f(getUniformLocation(name), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
				color.getAlpha() / 255f);
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
		cameraCommonVertex.abandon(this);
		cameraCommonGeometry.abandon(this);
		cameraCommonFragment.abandon(this);
	}

	/**
	 * Use no shader on the current thread
	 */
	public static void useNone() {
		glUseProgram(0);
	}

}
