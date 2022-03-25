package tv.floeze.bottleengine.client.graphics.deferred;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * A light for use with a {@link DeferredRenderer}
 * 
 * @author Floeze
 *
 */
public abstract class DeferredLight implements Renderable, Disposable {

	/**
	 * Vertices for full screen rect to draw light content to
	 */
	private static final float[] VERTICES = { //
			-1f, -1f, 0, 0, 0, // bottom left
			1f, -1f, 0, 1, 0, // bottom right
			1f, 1f, 0, 1, 1, // top right
			-1f, 1f, 0, 0, 1, // top left
	};

	/**
	 * Indices for {@link #VERTICES}
	 */
	private static final int[] INDICES = { //
			0, 1, 2, // bottom right triangle
			2, 3, 0, // to left triangle
	};

	/**
	 * The buffers the light renders to
	 */
	private static final RunnerLocal<int[]> buffers = new RunnerLocal<>(() -> {
		int vao = glGenVertexArrays();
		int vbo = glGenBuffers();
		int ebo = glGenBuffers();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0 * Float.BYTES);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

		return new int[] { vao, vbo, ebo };
	}, b -> {
		glDeleteVertexArrays(b[0]);
		glDeleteBuffers(b[1]);
		glDeleteBuffers(b[2]);
		return true;
	});

	/**
	 * The {@link Shader} this light uses
	 */
	private final Shader shader;

	/**
	 * The {@link RunnerLocalShader} that provides {@link Shader}s for this
	 * {@link DeferredLight}
	 */
	private final RunnerLocalShader runnerLocalShader;

	private final int vao;

	/**
	 * Creates a new {@link DeferredLight}
	 * 
	 * @param shader the {@link RunnerLocalShader} that provides {@link Shader}s for
	 *               this {@link DeferredLight}
	 */
	protected DeferredLight(RunnerLocalShader shader) {
		vao = buffers.get(this)[0];
		this.runnerLocalShader = shader;
		this.shader = this.runnerLocalShader.get(this);
	}

	/**
	 * Sets the uniforms for this {@link DeferredLight} before rendering the light
	 * pass. <br />
	 * The textures of the geometry pass are automatically bound to the following
	 * sampler2Ds:
	 * <ul>
	 * <li>albedo</li>
	 * <li>position</li>
	 * <li>normal</li>
	 * <li>specular</li>
	 * </ul>
	 * 
	 * @param shader Shader to set uniforms for
	 */
	protected abstract void setUniforms(Shader shader);

	@Override
	public final void render() {
		shader.use();
		setUniforms(shader);
		shader.set("albedo", 0);
		shader.set("position", 1);
		shader.set("normal", 2);
		shader.set("specular", 3);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
	}

	@Override
	public final void dispose() {
		buffers.abandon(this);
		runnerLocalShader.abandon(this);
	}

}
