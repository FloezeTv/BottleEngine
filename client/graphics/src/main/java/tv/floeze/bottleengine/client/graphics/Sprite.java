package tv.floeze.bottleengine.client.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import org.joml.AxisAngle4d;
import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;

import tv.floeze.bottleengine.client.graphics.io.Texture;
import tv.floeze.bottleengine.client.graphics.shader.Shader;
import tv.floeze.bottleengine.common.threads.Runner;

/**
 * A Sprite is a 2D-Image in the game
 * 
 * @author Floeze
 *
 */
public class Sprite implements Renderable {

	/**
	 * VAOs for the different Runners
	 */
	private static final Map<Runner, Integer> VAOs = new HashMap<>();

	/**
	 * Shaders for the different runners
	 */
	private static final Map<Runner, Shader> shaders = new HashMap<>();

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

	/**
	 * Texture of the sprite
	 */
	private Texture texture;

	/**
	 * Mode matrix
	 */
	private Matrix4d model;

	private Vector3d position = new Vector3d();
	private Vector2d scale = new Vector2d(1);
	private AxisAngle4d rotation = new AxisAngle4d();

	private final int vao;
	private final Shader shader;

	/**
	 * Creates a new sprite
	 * 
	 * @param texture texture of the sprite
	 */
	public Sprite(Texture texture) {
		this.texture = texture;
		model = new Matrix4d();
		updateMatrix();
		vao = getVAO();
		shader = getShader();
	}

	@Override
	public void render() {
		texture.bind(GL_TEXTURE0);

		shader.use();
		shader.set("model", model);
		shader.set("texture", 0);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
	}

	/**
	 * Updates the matrix. Call this after modifying the vectors. <br />
	 * Using the setters calls this automatically.
	 */
	public void updateMatrix() {
		model = new Matrix4d().translate(position).rotate(rotation).scale(scale.x(), scale.y(), 0);
	}

	public Vector3d getPosition() {
		return position;
	}

	public Sprite setPosition(Vector3d position) {
		this.position = position;
		updateMatrix();
		return this;
	}

	public Vector2d getScale() {
		return scale;
	}

	public Sprite setScale(Vector2d scale) {
		this.scale = scale;
		updateMatrix();
		return this;
	}

	public AxisAngle4d getRotation() {
		return rotation;
	}

	public Sprite setRotation(AxisAngle4d rotation) {
		this.rotation = rotation;
		updateMatrix();
		return this;
	}

	public Texture getTexture() {
		return texture;
	}

	public Sprite setTexture(Texture texture) {
		this.texture = texture;
		return this;
	}

	/**
	 * Gets the vao for the current {@link Runner} or creates a new one if none
	 * exists.
	 * 
	 * @return the vao to use in the current {@link Runner}
	 */
	private static int getVAO() {
		return VAOs.computeIfAbsent(Runner.getCurrentRunner(), k -> {
			int VAO = glGenVertexArrays();
			int VBO = glGenBuffers();
			int EBO = glGenBuffers();

			glBindVertexArray(VAO);

			glBindBuffer(GL_ARRAY_BUFFER, VBO);
			glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

			glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0 * Float.BYTES);
			glEnableVertexAttribArray(0);

			glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
			glEnableVertexAttribArray(1);

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

			return VAO;
		});
	}

	/**
	 * Gets the shader for the current {@link Runner} or creates a new one if none
	 * exists.
	 * 
	 * @return the shader to use in the current {@link Runner}
	 */
	private static Shader getShader() {
		return shaders.computeIfAbsent(Runner.getCurrentRunner(),
				k -> new Shader("bottleengine/shaders/sprite.vert", "bottleengine/shaders/sprite.frag"));
	}

}
