package tv.floeze.bottleengine.client.graphics;

/**
 * A {@link Renderable} can be rendered using its {@link #render()} method.
 * 
 * @author Floeze
 *
 */
@FunctionalInterface
public interface Renderable {

	/**
	 * Renders nothing
	 */
	public static final Renderable NOTHING = () -> {
	};

	/**
	 * Renders this {@link Renderable}
	 */
	public void render();

	/**
	 * Creates a {@link Renderable} that renders all passed {@link Renderable}s
	 * 
	 * @param renderables {@link Renderable}s to render
	 * @return a {@link Renderable} that renders all the passed {@link Renderable}s
	 */
	public static Renderable all(Renderable... renderables) {
		return () -> {
			for (Renderable r : renderables)
				r.render();
		};
	}

}
