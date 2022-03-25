package tv.floeze.bottleengine.client.graphics.shapes;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.awt.Color;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.Transformable;
import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * The common code for rendering colored shapes. <br />
 * All shapes will be rendered in a square of {@code (-0.5, -0.5)} to
 * {@code (0.5, 0.5)} model space. <br />
 * The model matrix will be automatically set as a uniform to the {@link Shader}
 * with the name {@code model}, but the shader has to use it. For useful
 * methods, see the helper-methods of {@link Shader}.<br />
 * The color will also be set automatically as a uniform to the {@link Shader}
 * with the name {@code color}, but the shader has to use it.
 * 
 * @author Floeze
 *
 */
public abstract class ColoredShape extends Transformable implements Renderable, Disposable {

	/**
	 * Vertices of the Shape (creating a simple square)
	 */
	private static final float[] VERTICES = { //
			-0.5f, -0.5f, 0, // bottom left
			0.5f, -0.5f, 0, // bottom right
			0.5f, 0.5f, 0, // top right
			-0.5f, 0.5f, 0, // top left
	};

	/**
	 * Indices of the Shape (creating a simple square)
	 */
	private static final int[] INDICES = { //
			0, 1, 2, // bottom right triangle
			2, 3, 0, // to left triangle
	};

	private static final RunnerLocal<int[]> buffers = new RunnerLocal<>(() -> {
		int vao = glGenVertexArrays();
		int vbo = glGenBuffers();
		int ebo = glGenBuffers();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 * Float.BYTES);
		glEnableVertexAttribArray(0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

		return new int[] { vao, vbo, ebo };
	}, b -> {
		glDeleteVertexArrays(b[0]);
		glDeleteBuffers(b[1]);
		glDeleteBuffers(b[2]);
		return true;
	});

	private Shader shader;

	private final int vao;

	private Color color = Color.BLACK;

	private float specular = 0.2f;

	/**
	 * Creates a new colored shape.
	 * 
	 * Set the {@link Shader} with {@link #setShader(Shader)} in constructor!
	 */
	protected ColoredShape() {
		vao = buffers.get(this)[0];
	}

	/**
	 * Sets the shader of this shape.
	 * 
	 * Call this in the constructor
	 * 
	 * @param shader {@link Shader} to use
	 */
	protected final void setShader(Shader shader) {
		this.shader = shader;
	}

	/**
	 * Sets the color of this shape.
	 * 
	 * @param color new color to use
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Gets the color of this shape.
	 * 
	 * @return the color of this shape
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the specular strength of this shape.
	 * 
	 * @param specular the new specular strength of this shape
	 */
	public void setSpecular(float specular) {
		this.specular = specular;
	}

	/**
	 * Gets the specular strength of this shape.
	 * 
	 * @return the specular shape of this shape
	 */
	public float getSpecular() {
		return specular;
	}

	@Override
	public void render() {
		shader.use();
		shader.set("model", getTransform());
		shader.setWithAlpha("color", color);
		shader.set("specular", specular);
		setShaderUnforms(shader);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
	}

	@Override
	public void dispose() {
		disposeShader();
		buffers.abandon(this);
	}

	/**
	 * Override in subclass if shader uniforms are needed.
	 * 
	 * A variable named {@code model} will automatically set to this shapes
	 * transformation matrix.
	 * 
	 * @param shader the shader this shape uses
	 */
	protected void setShaderUnforms(Shader shader) {

	}

	/**
	 * Code to dispose shader
	 */
	protected abstract void disposeShader();

}
