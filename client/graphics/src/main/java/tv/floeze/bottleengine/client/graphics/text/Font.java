package tv.floeze.bottleengine.client.graphics.text;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTTPackedchar.Buffer;
import org.lwjgl.system.MemoryStack;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.client.graphics.shader.RunnerLocalShader;
import tv.floeze.bottleengine.client.graphics.shader.Shader;

/**
 * A font that can be loaded from a local ttf file and render text to screen
 * 
 * @author Floeze
 *
 */
public class Font implements Disposable {

	/**
	 * The size of the textures to load the font into.
	 * 
	 * {@code 1024} seems to be widely supported and should be enough, but
	 * additional layers will be created if the texture is too small.
	 */
	private static final int TEXTURE_SIZE = 1024;

	/**
	 * How many chars will be loaded at first.
	 * 
	 * {@code 128} loads all ASCII-chars, which are probably used the most
	 */
	private static final int CHARDATA_FIRST_SIZE = 128;
	/**
	 * How many chars will be loaded around not loaded chars.
	 * 
	 * Smaller values will use the texture more efficiently and load faster, larger
	 * values might not need as much separate loads.
	 */
	private static final int CHARDATA_LOAD_SIZE = 16;

	/**
	 * How many vertices a char has (two triangles)
	 */
	private static final int VERTICES_PER_CHAR = 6;
	/**
	 * How many bytes a vertex has (position, texture coordinate, texture index)
	 */
	private static final int BYTES_PER_VERTEX = (2 + 2) * Float.BYTES + (1) * Integer.BYTES;
	/**
	 * How many bytes a char has
	 */
	private static final int BYTES_PER_CHAR = VERTICES_PER_CHAR * BYTES_PER_VERTEX;

	/**
	 * A class to store what parts of the font have been loaded
	 * 
	 * @author Floeze
	 *
	 */
	private static final class Cache {
		public final STBTTPackedchar.Buffer chardata;
		public final int textureIndex;

		public Cache(Buffer chardata, int textureIndex) {
			this.chardata = chardata;
			this.textureIndex = textureIndex;
		}

	}

	/**
	 * A class to store the relevant parts a Font-Texture has
	 * 
	 * @author Floeze
	 *
	 */
	private static final class Texture {
		public final STBTTPackContext context;
		public final ByteBuffer image;

		public Texture(STBTTPackContext context, ByteBuffer image) {
			this.context = context;
			this.image = image;
		}
	}

	/**
	 * The {@link Shader}s for {@link Font}s
	 */
	private static final RunnerLocalShader shaders = new RunnerLocalShader("bottleengine/shaders/font.vert",
			"bottleengine/shaders/font.frag");

	/**
	 * The shader for this {@link Font}
	 */
	private final Shader shader = shaders.get(this);
	/**
	 * OpenGL buffers
	 */
	private int vao, vbo;

	/**
	 * OpenGL texture id
	 */
	private final int textureId;

	/**
	 * A buffer that stores the vertices for rendering fonts directly.
	 * 
	 * This will automatically made bigger when longer text is rendered.
	 */
	private ByteBuffer vertices = BufferUtils.createByteBuffer(bytesNeeded(16));

	/**
	 * All the loaded textures for this {@link Font}
	 */
	private final List<Texture> textures = new ArrayList<>();

	/**
	 * All the loaded parts of the font for this {@link Font}
	 */
	private final Map<Integer, Cache> chardatas = new HashMap<>();

	/**
	 * the loaded raw font
	 */
	private final ByteBuffer fontdata;

	/**
	 * the size to load this font in
	 */
	private final int fontSize;

	/**
	 * Creates a new {@link Font}
	 * 
	 * @param path     path to the font file
	 * @param fontSize size to load the font in pixels. Font can be scaled up when
	 *                 rendering text, but will not look as sharp.
	 * @throws IOException when loading the font file fails
	 */
	public Font(String path, int fontSize) throws IOException {
		this.fontSize = fontSize;

		fontdata = loadByteBuffer(path);

		textureId = glGenTextures();

		createTexture();
		Cache first = load(0, CHARDATA_FIRST_SIZE, 0);
		for (int i = 0; i < CHARDATA_FIRST_SIZE / CHARDATA_LOAD_SIZE; i++)
			chardatas.put(i, first);

		updateTextures();
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), GL_DYNAMIC_DRAW);
		setVertexAttributes();

		shader.use();
		shader.set("textures", 0);
	}

	/**
	 * Loads a region around a character
	 * 
	 * @param character character to load around
	 */
	private void load(int character) {
		int characterIndex = character / CHARDATA_LOAD_SIZE;
		character = characterIndex * CHARDATA_LOAD_SIZE;

		if (chardatas.containsKey(character))
			return;

		int currentTexture = currentTextureIndex();

		Cache cache = load(character, CHARDATA_LOAD_SIZE, currentTexture);

		if (cache == null) {
			stbtt_PackEnd(textures.get(currentTexture).context);

			createTexture();
			updateTextures();
			currentTexture = currentTextureIndex();

			cache = load(character, CHARDATA_LOAD_SIZE, currentTexture);
		}

		chardatas.put(characterIndex, cache);
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
		updateTexture(currentTexture);

	}

	/**
	 * Creates a new {@link Texture} and initializes the packing context
	 * 
	 * @return a new {@link Texture}
	 */
	private Texture createTexture() {
		Texture texture = new Texture(STBTTPackContext.create(),
				BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE));
		stbtt_PackBegin(texture.context, texture.image, TEXTURE_SIZE, TEXTURE_SIZE, 0, 1);
		stbtt_PackSetOversampling(texture.context, 2, 2);
		textures.add(texture);
		return texture;
	}

	/**
	 * Loads a range of characters and returns the {@link Cache}
	 * 
	 * @param from   first char to load
	 * @param length how many chars to load
	 * @param index  index of {@link Texture} in {@link #textures} to load into
	 * @return a {@link Cache} with the loaded data or {@code null} if no space in
	 *         texture
	 */
	private Cache load(int from, int length, int index) {
		Texture texture = textures.get(index);
		STBTTPackedchar.Buffer chardata = STBTTPackedchar.malloc(length);

		boolean success = stbtt_PackFontRange(texture.context, fontdata, 0, fontSize, from, chardata);

		if (success)
			return new Cache(chardata, index);

		return null;
	}

	/**
	 * @return the index of the {@link Texture} of {@link #textures} to currently
	 *         use and load into
	 */
	private int currentTextureIndex() {
		return textures.size() - 1;
	}

	/**
	 * Updates the textures on the GPU
	 */
	private void updateTextures() {
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
		glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RED, TEXTURE_SIZE, TEXTURE_SIZE, textures.size(), 0, GL_RED,
				GL_UNSIGNED_BYTE, 0);
		for (int i = 0; i < textures.size(); i++)
			updateTexture(i);
	}

	/**
	 * Update a single texture on the GPU
	 * 
	 * @param index index of a {@link Texture} in {@link #textures} to update
	 */
	private void updateTexture(int index) {
		glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, index, TEXTURE_SIZE, TEXTURE_SIZE, 1, GL_RED, GL_UNSIGNED_BYTE,
				textures.get(index).image);
	}

	/**
	 * Draws a white string to the screen
	 * 
	 * @param x    x-coordinate to start drawing from
	 * @param y    y-coordinate to start drawing from
	 * @param text text to draw
	 */
	public void drawString(float x, float y, String text) {
		drawString(x, y, text, Color.WHITE);
	}

	/**
	 * Draws a string to the screen
	 * 
	 * @param x     x-coordinate to start drawing from
	 * @param y     y-coordinate to start drawing from
	 * @param text  text to draw
	 * @param color color to draw text in
	 */
	public void drawString(float x, float y, String text, Color color) {
		drawString(x, y, text, color, 0.2f);
	}

	/**
	 * Draws a string to the screen
	 * 
	 * @param x        x-coordinate to start drawing from
	 * @param y        y-coordinate to start drawing from
	 * @param text     text to draw
	 * @param color    color to draw text in
	 * @param specular specular strength of the text
	 */
	public void drawString(float x, float y, String text, Color color, float specular) {
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);

		shader.use();
		shader.set("model", new Matrix4f());
		shader.set("color", color);
		shader.set("specular", specular);

		verticesMinLength(text.length());

		vertices.clear();
		loadIntoByteBuffer(vertices, x, y, text);

		glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
		glDrawArrays(GL_TRIANGLES, 0, text.length() * 6);
	}

	/**
	 * Loads the positions, texture coordinates and texture indices into the given
	 * {@link ByteBuffer}
	 * 
	 * @param buffer buffer to load into. Must be at least big enough (as given by
	 *               {@link #bytesNeeded(String)})
	 * @param x      x-position of first char
	 * @param y      y-position of first char
	 * @param text   text to load
	 * @return {@code buffer} filled with the data needed to render the text
	 */
	protected ByteBuffer loadIntoByteBuffer(ByteBuffer buffer, float x, float y, String text) {
		if (buffer.remaining() < bytesNeeded(text))
			throw new IllegalArgumentException(
					"Buffer is too small (" + buffer.remaining() + "/" + bytesNeeded(text) + ")");
		try (MemoryStack stack = MemoryStack.stackPush()) {
			STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
			FloatBuffer currentX = stack.floats(x);
			FloatBuffer currentY = stack.floats(y);

			for (int i = 0; i < text.length(); i++) {
				int c = text.codePointAt(i);

				Cache cache = chardatas.get(c / CHARDATA_LOAD_SIZE);
				if (cache == null) {
					load(c);
					cache = chardatas.get(c / CHARDATA_LOAD_SIZE);
				}

				if (c > CHARDATA_FIRST_SIZE)
					c %= CHARDATA_LOAD_SIZE;

				stbtt_GetPackedQuad(cache.chardata, TEXTURE_SIZE, TEXTURE_SIZE, c, currentX, currentY, quad, false);

				putVertices(buffer, quad, cache.textureIndex);
			}
		}
		buffer.flip();
		return buffer;
	}

	@Override
	public void dispose() {
		shaders.abandon(this);
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		glDeleteTextures(textureId);
	}

	/**
	 * Makes sure {@link #vertices} has at least the needed capacity
	 * 
	 * @param length length of text (number of chars)
	 */
	private void verticesMinLength(int length) {
		if (bytesNeeded(length) > vertices.capacity()) {
			vertices = BufferUtils.createByteBuffer(bytesNeeded(length));
			glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), GL_DYNAMIC_DRAW);
		}
	}

	/**
	 * Puts the vertices into a given {@link ByteBuffer}.
	 * 
	 * This assumes the buffer is big enough.
	 * 
	 * @param vertices     {@link ByteBuffer} to load into
	 * @param quad         bounds of the char
	 * @param textureIndex index of the texture the loaded font data is at
	 */
	private static void putVertices(ByteBuffer vertices, STBTTAlignedQuad quad, int textureIndex) {
		vertices.putFloat(quad.x0());
		vertices.putFloat(quad.y0());
		vertices.putFloat(quad.s0());
		vertices.putFloat(quad.t0());
		vertices.putInt(textureIndex);

		vertices.putFloat(quad.x1());
		vertices.putFloat(quad.y0());
		vertices.putFloat(quad.s1());
		vertices.putFloat(quad.t0());
		vertices.putInt(textureIndex);

		vertices.putFloat(quad.x1());
		vertices.putFloat(quad.y1());
		vertices.putFloat(quad.s1());
		vertices.putFloat(quad.t1());
		vertices.putInt(textureIndex);

		vertices.putFloat(quad.x0());
		vertices.putFloat(quad.y1());
		vertices.putFloat(quad.s0());
		vertices.putFloat(quad.t1());
		vertices.putInt(textureIndex);

		vertices.putFloat(quad.x0());
		vertices.putFloat(quad.y0());
		vertices.putFloat(quad.s0());
		vertices.putFloat(quad.t0());
		vertices.putInt(textureIndex);

		vertices.putFloat(quad.x1());
		vertices.putFloat(quad.y1());
		vertices.putFloat(quad.s1());
		vertices.putFloat(quad.t1());
		vertices.putInt(textureIndex);
	}

	/**
	 * Sets the OpenGL vertex Attributes to use the format used by {@link Font}
	 * 
	 * POS_X float<br />
	 * POS_Y float<br />
	 * TEX_X float<br />
	 * TEX_Y float<br />
	 * TEX_I int
	 */
	protected static void setVertexAttributes() {
		glVertexAttribPointer(0, 2, GL_FLOAT, false, BYTES_PER_VERTEX, 0 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTES_PER_VERTEX, 2 * Float.BYTES);
		glEnableVertexAttribArray(1);
		glVertexAttribIPointer(2, 1, GL_INT, BYTES_PER_VERTEX, 4 * Float.BYTES);
		glEnableVertexAttribArray(2);
	}

	/**
	 * Creates a new {@link ByteBuffer} and loads the bytes of a given file on the
	 * classpath into it.
	 * 
	 * @param path path of file
	 * @return the {@link ByteBuffer} with the bytes of the file
	 * @throws IOException if file could not be read
	 */
	private static ByteBuffer loadByteBuffer(String path) throws IOException {
		int size = 0;
		try (InputStream input = Font.class.getClassLoader().getResourceAsStream(path)) {
			while (input.read() >= 0)
				size++;
		}

		ByteBuffer ttf = BufferUtils.createByteBuffer(size);
		try (ReadableByteChannel channel = Channels.newChannel(Font.class.getClassLoader().getResourceAsStream(path))) {
			channel.read(ttf);
		}
		ttf.flip();
		return ttf;
	}

	/**
	 * Gets the shader of this {@link Font}
	 * 
	 * @return the shader used by this {@link Font}
	 */
	protected Shader getShader() {
		return shader;
	}

	/**
	 * How many bytes are needed to store the text in a {@link ByteBuffer}
	 * 
	 * @param text text to store
	 * @return bytes needed
	 */
	protected static int bytesNeeded(String text) {
		return bytesNeeded(text.length());
	}

	/**
	 * How many bytes are needed to store the number of chars in a
	 * {@link ByteBuffer}
	 * 
	 * @param length number of chars
	 * @return bytes needed
	 */
	protected static int bytesNeeded(int length) {
		return length * BYTES_PER_CHAR;
	}

	/**
	 * How many vertices are needed for the given text
	 * 
	 * @param text text to calculate for
	 * @return vertices needed
	 */
	protected static int vertexNumber(String text) {
		return vertexNumber(text.length());
	}

	/**
	 * How many vertices are needed for the given number of chars
	 * 
	 * @param length number of chars
	 * @return vertices needed
	 */
	protected static int vertexNumber(int length) {
		return length * VERTICES_PER_CHAR;
	}

}
