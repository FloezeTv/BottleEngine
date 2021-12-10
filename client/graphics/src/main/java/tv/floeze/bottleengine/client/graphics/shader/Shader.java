package tv.floeze.bottleengine.client.graphics.shader;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.*;

import java.awt.Color;

import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

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

	public void set(String name, boolean b) {
		glUniform1i(getUniformLocation(name), b ? GL_TRUE : GL_FALSE);
	}

	public void set(String name, int i) {
		glUniform1i(getUniformLocation(name), i);
	}

	public void set(String name, float f) {
		glUniform1f(getUniformLocation(name), f);
	}

	public void set(String name, double d) {
		glUniform1d(getUniformLocation(name), d);
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

	/**
	 * Use no shader on the current thread
	 */
	public static void useNone() {
		glUseProgram(0);
	}

}
