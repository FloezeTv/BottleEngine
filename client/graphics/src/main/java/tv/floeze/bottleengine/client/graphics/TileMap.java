package tv.floeze.bottleengine.client.graphics;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import org.joml.Vector2i;

import tv.floeze.bottleengine.client.graphics.io.Texture;
import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A {@link TileMap} to easily create levels based on tilesets
 * 
 * @author Floeze
 *
 */
public class TileMap extends Transformable implements Renderable, Disposable {

	/**
	 * A {@link Tileset} that is a single image with multiple tiles stichted
	 * together.
	 * 
	 * @author Floeze
	 *
	 */
	public static final class Tileset {
		private final Texture texture;
		private final Vector2i dimensions;

		/**
		 * @param texture The texture with all the tiles
		 * @param x       how many tiles there are in x-direction
		 * @param y       how many tiles there are in y-direction
		 */
		public Tileset(Texture texture, int x, int y) {
			this.texture = texture;
			this.dimensions = new Vector2i(x, y);
		}

		public Texture getTexture() {
			return texture;
		}

		public Vector2i getDimensions() {
			return dimensions;
		}

	}

	private static final RunnerLocalShader shaders = new RunnerLocalShader(
			new Shader.Part(GL_VERTEX_SHADER, "bottleengine/shaders/tilemap.vert"),
			new Shader.Part(GL_GEOMETRY_SHADER, "bottleengine/shaders/tilemap.geom"),
			new Shader.Part(GL_FRAGMENT_SHADER, "bottleengine/shaders/tilemap.frag"));

	/**
	 * The tile ids (indexed by x and y, but in a one dimensional array)
	 */
	private final int[] tiles;

	private final int vao, vbo;

	private final Tileset tileset;

	private final Shader shader;

	private float specular = 0.2f;

	/**
	 * The size of this {@link TileMap} (how many tiles in x- and y-direction)
	 */
	private final Vector2i size;

	/**
	 * Creates a new {@link TileMap}
	 * 
	 * @param width   the width of the {@link TileMap} in tiles
	 * @param height  the height of the {@link TileMap} in tiles
	 * @param tileset the tileset of this {@link TileMap}
	 */
	public TileMap(int width, int height, Tileset tileset) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Dimensions must be positive");

		tiles = new int[width * height];

		this.tileset = tileset;

		size = new Vector2i(width, height);

		shader = shaders.get(this);

		vao = glGenVertexArrays();
		vbo = glGenBuffers();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, tiles, GL_STATIC_DRAW); // maybe other usage

		glVertexAttribIPointer(0, 1, GL_INT, 1 * Integer.BYTES, 0 * Integer.BYTES);
		glEnableVertexAttribArray(0);
	}

	/**
	 * Sets a tile.<br />
	 * Tile ids are given in a Z-like fashion starting from the top and each row
	 * from left to right.<br />
	 * Call {@link #updateTiles()} to apply the changes.
	 * 
	 * @param x    x-coordinate of the tile to change
	 * @param y    y-coordinate of the tile to change
	 * @param tile the tile id to change to
	 * 
	 * @see #updateTiles()
	 */
	public void set(int x, int y, int tile) {
		tiles[x + y * size.x] = tile;
	}

	/**
	 * Updates the tiles. Call after changing a tile.
	 */
	public void updateTiles() {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, 0, tiles);
	}

	/**
	 * Sets the specular strength of this tilemap
	 * 
	 * @param specular the new specular strength of this tilemap
	 */
	public void setSpecular(float specular) {
		this.specular = specular;
	}

	/**
	 * Gets the specular strength of this tilemap
	 * 
	 * @return the specular shape of this tilemap
	 */
	public float getSpecular() {
		return specular;
	}

	@Override
	public void render() {
		shader.use();
		shader.set("tileMapSize", size);
		shader.set("tileSetSize", tileset.dimensions);
		shader.set("model", getTransform());
		shader.set("specular", specular);
		glBindVertexArray(vao);
		tileset.texture.bind();
		glDrawArrays(GL_POINTS, 0, tiles.length);
	}

	@Override
	public void dispose() {
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		shaders.abandon(this);
		tileset.texture.dispose();
	}

}
