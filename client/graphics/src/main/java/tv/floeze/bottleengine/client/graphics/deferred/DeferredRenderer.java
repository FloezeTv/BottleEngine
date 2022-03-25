package tv.floeze.bottleengine.client.graphics.deferred;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

import tv.floeze.bottleengine.client.graphics.Disposable;
import tv.floeze.bottleengine.client.graphics.Renderable;
import tv.floeze.bottleengine.client.graphics.window.AspectMode;
import tv.floeze.bottleengine.client.graphics.window.Renderer;

/**
 * A {@link Renderer} that renders the content using deferred. This allows the
 * {@link Renderer} to have {@link DeferredLight}s which illuminate the scene.
 * <br />
 * By default only a single {@link AmbientDeferredLight} is used to illuminate
 * the content, which can be removed using {@link #clearLights()}.
 * 
 * @author Floeze
 *
 */
public class DeferredRenderer extends Renderer {

	// FIXME: problems with specular and transparency
	// FIXME: general problems with 2D things (objects get illuminated from 'behind'
	// and one side stays dark)

	// Use Albedo, Position, Normal, Spec
	private static final int BITFIELD = toBitfield(true, true, true, true);

	/**
	 * The framebuffer to render to
	 */
	private int framebuffer;

	/**
	 * The textures that are rendered to
	 */
	private int position, normal, albedo, specular;

	/**
	 * The lights to render
	 */
	private final List<DeferredLight> lights = new ArrayList<>();

	@Override
	protected void doInitialize() {

		framebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

		// color
		albedo = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, albedo);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, albedo, 0);

		// position
		position = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, position);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, position, 0);

		// normal
		normal = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, normal);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, normal, 0);

		// specular
		specular = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, specular);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT3, GL_TEXTURE_2D, specular, 0);

		glDrawBuffers(
				new int[] { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3 });

		lights.add(new AmbientDeferredLight());
	}

	/**
	 * Adds a light
	 * 
	 * @param light light to add
	 */
	public final void addLight(DeferredLight light) {
		lights.add(light);
	}

	/**
	 * Removes a light
	 * 
	 * @param light light to remove
	 */
	public final void removeLight(DeferredLight light) {
		lights.remove(light);
	}

	/**
	 * Removes all the lights from this {@link DeferredRenderer}
	 */
	public final void clearLights() {
		lights.clear();
	}

	@Override
	protected void doDeinitialize() {
		glDeleteFramebuffers(framebuffer);
		glDeleteTextures(new int[] { position, normal, albedo, specular });
		lights.forEach(Disposable::dispose);
		lights.clear();
	}

	@Override
	protected void doUpdateSize(int width, int height) {
		// This creates a new texture every time the screen is resized, but this does
		// not seem to be an issue
		// Theoretically some logic can be added to only double/half the texture,
		// because glScissors is used

		glBindTexture(GL_TEXTURE_2D, albedo);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, 0);

		glBindTexture(GL_TEXTURE_2D, position);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, 0);

		glBindTexture(GL_TEXTURE_2D, normal);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, 0);

		glBindTexture(GL_TEXTURE_2D, specular);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_FLOAT, 0);
	}

	@Override
	protected void doRender(Renderable content) {
		// Setup

		AspectMode.Size viewportSize = getParent().getViewportSize();

		// Geometry pass

		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		setBitfield(BITFIELD);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glViewport(0, 0, viewportSize.cameraWidth, viewportSize.cameraHeight);
		glScissor(0, 0, viewportSize.cameraWidth, viewportSize.cameraHeight);

		glClearColor(0, 0, 0, 0);
		glClear(GL_COLOR_BUFFER_BIT);

		getParent().getCamera().setMatrices();

		content.render();

		// Lighting pass

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		glViewport(viewportSize.x, viewportSize.y, viewportSize.width, viewportSize.height);
		glScissor(getParent().getViewportX(), getParent().getViewportY(), getParent().getViewportWidth(),
				getParent().getViewportHeight());

		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT);

		glBlendFunc(GL_ONE, GL_ONE);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, albedo);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, position);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, normal);
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, specular);

		lights.forEach(Renderable::render);

	}

}
