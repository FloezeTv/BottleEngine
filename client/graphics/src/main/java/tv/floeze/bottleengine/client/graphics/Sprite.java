package tv.floeze.bottleengine.client.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import tv.floeze.bottleengine.client.graphics.io.Texture;
import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.common.threads.RunnerLocal;

/**
 * A Sprite is a 2D-Image in the game
 * 
 * @author Floeze
 *
 */
public class Sprite extends Transformable implements Renderable, Disposable {

	/**
	 * Vertices of the Sprite (for creating {@link #VAOs} in {@link #getVAO()})
	 */
	private static final float[] VERTICES = { //
			-0.5f, -0.5f, 0, 0, 1, // bottom left
			0.5f, -0.5f, 0, 1, 1, // bottom right
			0.5f, 0.5f, 0, 1, 0, // top right
			-0.5f, 0.5f, 0, 0, 0, // top left
	};

	/**
	 * Indices of the Sprite (for creating {@link #VAOs} in {@link #getVAO()})
	 */
	private static final int[] INDICES = { //
			0, 1, 2, // bottom right triangle
			2, 3, 0, // to left triangle
	};

	private static final RunnerLocal<Shader> shaders = new RunnerLocal<>(
			() -> new Shader("bottleengine/shaders/sprite.vert", "bottleengine/shaders/sprite.frag"), Shader::dispose);

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
	 * Texture of the sprite
	 */
	private Texture texture;

	private final int vao;
	private final Shader shader;

	/**
	 * Creates a new sprite
	 * 
	 * @param texture texture of the sprite
	 */
	public Sprite(Texture texture) {
		this.texture = texture;
		vao = buffers.get(this)[0];
		shader = shaders.get(this);
	}

	@Override
	public void render() {
		texture.bind(GL_TEXTURE0);

		shader.use();
		shader.set("model", getTransform());
		shader.set("texture", 0);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
	}

	public Texture getTexture() {
		return texture;
	}

	public Sprite setTexture(Texture texture) {
		this.texture = texture;
		return this;
	}

	@Override
	public void dispose() {
		shaders.abandon(this);
		buffers.abandon(this);
		texture.dispose();
	}

}
