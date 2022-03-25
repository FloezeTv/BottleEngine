package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import tv.floeze.bottleengine.client.graphics.Renderable;

/**
 * A Renderer is responsible for rendering a {@link Renderable}
 * 
 * @author Floeze
 *
 */
public abstract class Renderer {

	/**
	 * The uniform buffer that holds the bitfield
	 */
	private int ubo;

	/**
	 * The parent of this Renderer
	 */
	private Viewport parent;

	/**
	 * Initializes this {@link Renderer}
	 */
	protected final void initialize() {
		ubo = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, ubo);
		// allocate space for one int (bitfield)
		glBufferData(GL_UNIFORM_BUFFER, 1 * Integer.BYTES, GL_DYNAMIC_DRAW);
		doInitialize();
	}

	/**
	 * Deinitializes this {@link Renderer}
	 */
	protected final void deinitialize() {
		doDeinitialize();
		glDeleteBuffers(ubo);
	}

	/**
	 * Updates the size this {@link Renderer} has to render in
	 * 
	 * @param width  new width to render
	 * @param height new height to render
	 */
	protected final void updateSize(int width, int height) {
		doUpdateSize(width, height);
	}

	/**
	 * Renders the content
	 * 
	 * @param content the {@link Renderable} to render
	 */
	protected final void render(Renderable content) {
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, ubo);
		doRender(content);
	}

	/**
	 * This {@link Renderer}s specific code that renders a {@link Renderable}
	 * 
	 * @param content the {@link Renderable} to render
	 */
	protected abstract void doRender(Renderable content);

	/**
	 * This {@link Renderer}s specific code to initialize
	 */
	protected abstract void doInitialize();

	/**
	 * This {@link Renderer}s specific code to deinitialize
	 */
	protected abstract void doDeinitialize();

	/**
	 * This {@link Renderer}s specific code to update the size
	 * 
	 * @param width  new width to render
	 * @param height new height to render
	 */
	protected abstract void doUpdateSize(int width, int height);

	/**
	 * Sets the parent.<br />
	 * This does neither initialize nor deinitialize this {@link Renderer}; it only
	 * updates the reference to the parent.
	 * 
	 * @param parent The new parent
	 */
	protected final void setParent(Viewport parent) {
		this.parent = parent;
	}

	/**
	 * Gets the current parent of this {@link Renderable}
	 * 
	 * @return the current parent
	 */
	protected final Viewport getParent() {
		return parent;
	}

	/**
	 * Sets the bitfield of this {@link Renderer}
	 * 
	 * @param values the boolean values to set, as converted by
	 *               {@link #toBitfield(boolean...)}
	 * 
	 * @see #toBitfield(boolean...)
	 */
	protected final void setBitfield(boolean... values) {
		setBitfield(toBitfield(values));
	}

	/**
	 * Sets the bitfield of this {@link Renderer}
	 * 
	 * @param value the raw bitfield to set
	 */
	protected final void setBitfield(int value) {
		glBindBuffer(GL_UNIFORM_BUFFER, ubo);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer buffer = stack.ints(1);
			buffer.put(value);
			buffer.flip();
			glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);
		}
	}

	/**
	 * Converts the given booleans to a bitfield. The first value is the least
	 * significant bit, the next values will be written in the direction towards the
	 * most significant bit. Unset bits will be treated as {@code false}.
	 * 
	 * @param values The values (at most 32) to convert to a bitfield. {@code true}
	 *               is {@code 1}, {@code false} is {@code 0}.
	 * @return The converted bitfield as a 32-bit int
	 */
	protected static final int toBitfield(boolean... values) {
		if (values.length > Integer.BYTES * 8)
			throw new IllegalArgumentException("Bitfield is too long to fit into 32 bit integer");
		int bitfield = 0;
		for (boolean value : values) {
			bitfield >>>= 1;
			if (value)
				bitfield |= 1 << Integer.BYTES * 8 - 1;
		}
		bitfield >>>= 32 - values.length;
		return bitfield;
	}

}
