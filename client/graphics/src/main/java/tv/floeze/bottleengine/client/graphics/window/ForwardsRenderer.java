package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import tv.floeze.bottleengine.client.graphics.Renderable;

/**
 * A {@link Renderer} that just renders the content to the screen.
 * 
 * @author Floeze
 *
 */
public class ForwardsRenderer extends Renderer {

	// Use Albedo
	private static final int BITFIELD = toBitfield(true);

	@Override
	protected void doInitialize() {
		// nothing to initialize
	}

	@Override
	protected void doDeinitialize() {
		// nothing to deinitialize
	}

	@Override
	protected void doUpdateSize(int width, int height) {
		// Nothing to update
	}

	@Override
	protected void doRender(Renderable content) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		setBitfield(BITFIELD);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		AspectMode.Size viewportSize = getParent().getViewportSize();

		glViewport(viewportSize.x, viewportSize.y, viewportSize.width, viewportSize.height);
		glScissor(getParent().getViewportX(), getParent().getViewportY(), getParent().getViewportWidth(),
				getParent().getViewportHeight());

		getParent().getCamera().setMatrices();

		content.render();
	}

}
