package tv.floeze.bottleengine.client.graphics.io;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import tv.floeze.bottleengine.client.graphics.Disposable;

/**
 * A Texture.<br />
 * You can get a {@link Texture} via {@link ImageLoader#asTexture()}.
 * 
 * @author Floeze
 *
 */
public class Texture implements Disposable {

	private final int handle;

	protected Texture(int handle) {
		this.handle = handle;
	}

	/**
	 * Binds this {@link Texture} to the specified slot
	 * 
	 * @param slot slot to bind to
	 */
	public void bind(int slot) {
		glActiveTexture(slot);
		bind();
	}

	/**
	 * Binds this {@link Texture} to the active slot
	 */
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, handle);
	}

	@Override
	public void dispose() {
		glDeleteTextures(handle);
	}

}
