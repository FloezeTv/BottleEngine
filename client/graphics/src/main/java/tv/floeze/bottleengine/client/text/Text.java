package tv.floeze.bottleengine.client.text;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.awt.Color;
import java.nio.ByteBuffer;

import org.joml.Matrix4d;
import org.lwjgl.system.MemoryStack;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.Transformable;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A {@link Text} that can be rendered to the screen multiple times.
 * 
 * This is useful for drawing strings that don't change often, as the data
 * doesn't have to be calculated each frame.
 * 
 * @author Floeze
 *
 */
public class Text extends Transformable implements Renderable, Disposable {

	/**
	 * OpengL buffers
	 */
	private final int vao, vbo;
	/**
	 * Shader to use
	 */
	private final Shader shader;
	/**
	 * Color to draw in
	 */
	private Color color = Color.WHITE;
	/**
	 * Number of vertices
	 */
	private int size;
	/**
	 * Variables needed for only displaying part of the text
	 */
	private int cutLeft, cutRight, start, end;

	/**
	 * Creates a new {@link Text}
	 * 
	 * @param text text to render
	 * @param font {@link Font} to render in
	 */
	public Text(String text, Font font) {
		shader = font.getShader();
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		update(text, font);
		Font.setVertexAttributes();
	}

	/**
	 * Update the Text to display another text
	 * 
	 * @param text new text to display
	 * @param font new Font to render in
	 */
	public void update(String text, Font font) {
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer buffer = stack.malloc(Font.bytesNeeded(text));
			font.loadIntoByteBuffer(buffer, 0, 0, text);
			glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		}

		size = Font.vertexNumber(text);
		updateStartEnd();
	}

	/**
	 * Change the color of the text
	 * 
	 * @param color new color to render in
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Hide the specified number of chars on the left side
	 * 
	 * @param cutLeft how many chars to hide on the left side
	 */
	public void setCutLeft(int cutLeft) {
		if (cutLeft < 0)
			cutLeft = 0;
		this.cutLeft = cutLeft;
		updateStartEnd();
	}

	/**
	 * Hide the specified number of chars on the right side
	 * 
	 * @param cutRighthow many chars to hide on the right side
	 */
	public void setCutRight(int cutRight) {
		if (cutRight < 0)
			cutRight = 0;
		this.cutRight = cutRight;
		updateStartEnd();
	}

	/**
	 * Update the start and end of the text.
	 * 
	 * This should be called after modifying the cutoffs or the text
	 */
	private void updateStartEnd() {
		end = size - Font.vertexNumber(cutRight);
		if (end < 0)
			end = 0;
		start = Font.vertexNumber(cutLeft);
		if (start > end)
			start = end;
	}

	/**
	 * Draws this {@link Text} at the specified coordinates
	 * 
	 * @param x x-coordinate to draw at
	 * @param y y-coordinate to draw at
	 */
	public void draw(float x, float y) {
		draw(new Matrix4d().translate(x, y, 0));
	}

	/**
	 * Draws this {@link Text} transformed by the specified Matrix
	 * 
	 * @param modelToUse transformation matrix
	 */
	private void draw(Matrix4d modelToUse) {
		shader.use();
		shader.set("model", modelToUse);
		shader.set("color", color);
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, start, end);
	}

	@Override
	public void render() {
		draw(getTransform());
	}

	@Override
	public void dispose() {
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		// rest is handled by Font
	}

}
