package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;

import tv.floeze.bottleengine.client.graphics.io.ImageLoader;
import tv.floeze.bottleengine.common.threads.Runner;

/**
 * A window that can display graphics.
 * 
 * @author Floeze
 *
 */
public class Window {

	/*
	 * Bugs:
	 * 
	 * - close callback can be too slow: Window#setCloseHandler(() -> false); and
	 * spam click close very fast
	 * 
	 * 
	 * Considerations:
	 * 
	 * - add a builder class
	 * 
	 * 
	 * TODOs:
	 * 
	 * - methods that have to be executed on the main thread should automatically do
	 * so or at least state that in the Javadoc
	 */

//	public static class Builder {
//
//		private int x, y, width = 800, height = 600;
//		private CharSequence title;
//		private Window share;
//		private ImageLoader icon;
//
//		public Builder() {
//		}
//
//		public Builder withWidth(int width) {
//			this.width = width;
//			return this;
//		}
//
//		public Builder withHeight(int height) {
//			this.height = height;
//			return this;
//		}
//
//		public Builder withSize(int width, int height) {
//			return withWidth(width).withHeight(height);
//		}
//
//		public Builder withTitle(CharSequence title) {
//			this.title = title;
//			return this;
//		}
//
//		public Builder withShare(Window share) {
//			this.share = share;
//			return this;
//		}
//
//		public Builder withIcon(ImageLoader icon) {
//			this.icon = icon;
//			return this;
//		}
//
//		public Builder withX(int x) {
//			this.x = x;
//			return this;
//		}
//
//		public Builder withY(int y) {
//			this.y = y;
//			return this;
//		}
//
//		public Builder withPosition(int x, int y) {
//			return withX(x).withY(y);
//		}
//
//		public Builder withWindowHint(int hint, int value) {
//			// TODO
//			return this;
//		}
//
//		public Window build() {
//			// TODO
//			return null;
//		}
//	}

	/**
	 * The number of currently open windows
	 */
	private static final AtomicInteger windowNumber = new AtomicInteger(0);

	/**
	 * A {@link Runnable} to run once the last window has been closed
	 */
	private static Runnable onLastWindowClosed = Runner.MAIN::stop;

	/**
	 * The handle of this window
	 */
	private final long handle;

	/**
	 * The thread this window renders in
	 */
	private final Runner runner;

	/**
	 * The viewports
	 */
	private final List<Viewport> viewports = new ArrayList<Viewport>();

	/**
	 * The size of the window
	 */
	private int width, height;

	/**
	 * The background color that is shown where nothing is rendered or {@code null}
	 * if background should not be cleared
	 * 
	 * @see #setClearColor(Color)
	 */
	private Color clearColor;

	/**
	 * Gets called when the window should close.
	 * 
	 * @see #setCloseHandler(Supplier)
	 */
	private Supplier<Boolean> closeHandler = () -> true;

	static {
		Runner.MAIN.repeat(GLFW::glfwPollEvents);
	}

	/**
	 * Creates a new window.
	 * 
	 * Creates a new thread that renders the graphics to the window.
	 * 
	 * A window must only be created on the main thread (see
	 * {@link GLFW#glfwCreateWindow(int, int, CharSequence, long, long)}).
	 * 
	 * @param width   the width of the window
	 * @param height  the height of the window
	 * @param title   the title of the window
	 * @param monitor a monitor to use for fullscreen or {@code 0} if windowed
	 * @param share   another {@link Window} to share resources with its context or
	 *                {@code null} if no resources should be shared
	 * 
	 * @see GLFW#glfwCreateWindow(int, int, CharSequence, long, long)
	 */
	public Window(int width, int height, CharSequence title, long monitor, Window share) {
		if (!glfwInit())
			throw new RuntimeException("Couldn't initialize GLWF");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		// for Mac OS X
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

		handle = glfwCreateWindow(width, height, title, monitor > 0 ? monitor : NULL,
				share != null ? share.handle : NULL);
		this.width = width;
		this.height = height;

		// create Thread
		runner = new Runner().runInNewThread("renderer-" + System.identityHashCode(this), false);

		// set callbacks
		glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
			this.width = w;
			this.height = h;
		});
		glfwSetWindowCloseCallback(handle, window -> glfwSetWindowShouldClose(window, closeHandler.get()));

		runner.run(() -> {
			glfwMakeContextCurrent(handle);
			// v-sync
			glfwSwapInterval(1);

			GL.createCapabilities();

			glViewport(0, 0, width, height);
		}).thenRun(() -> runner.repeat(() -> {
			if (!glfwWindowShouldClose(handle)) {
				clear();

				render();

				glfwSwapBuffers(handle);
			} else {
				runner.stop();
				glfwMakeContextCurrent(NULL);
				closeWindow(handle);
			}
		}));

		windowNumber.incrementAndGet();
	}

	/**
	 * Sets the icon of this window
	 * 
	 * @param icon the new icon
	 * 
	 * @see #setIcon(ImageLoader)
	 */
	public void setIcon(GLFWImage.Buffer icon) {
		glfwSetWindowIcon(handle, icon);
	}

	/**
	 * Sets the icon of this window
	 * 
	 * @param icon the new icon
	 */
	public void setIcon(ImageLoader icon) {
		GLFWImage.Buffer buffer = icon.asGLFWImageBuffer();
		setIcon(buffer);
		buffer.free();
	}

	/**
	 * Sets the position of the window
	 * 
	 * @param x the new x position
	 * @param y the new y position
	 */
	public void setPosition(int x, int y) {
		glfwSetWindowPos(handle, x, y);
	}

	/**
	 * Sets the size of the window
	 * 
	 * @param width  the new width of the window
	 * @param height the new height of the window
	 */
	public void setSize(int width, int height) {
		glfwSetWindowSize(handle, width, height);
	}

	/**
	 * The background color that is shown where nothing is rendered or {@code null}
	 * if background should not be cleared
	 * 
	 * @param clearColor
	 */
	public void setClearColor(Color clearColor) {
		this.clearColor = clearColor;
	}

	/**
	 * Sets a listener to be executed when this window should close
	 * 
	 * @param closeHandler a listener that gets executed when this window should
	 *                     close. <br />
	 *                     Return
	 *                     <ul>
	 *                     <li>{@code true} if the window should still close</li>
	 *                     <li>{@code false} if the window should not close</li>
	 *                     </ul>
	 */
	public void setCloseHandler(Supplier<Boolean> closeHandler) {
		this.closeHandler = closeHandler;
	}

	/**
	 * Clears the screen
	 */
	private void clear() {
		if (clearColor != null) {
			glClearColor(clearColor.getRed() / 255f, clearColor.getGreen() / 255f, clearColor.getBlue() / 255f,
					clearColor.getBlue() / 255f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_DEPTH_BUFFER_BIT);
		}
	}

	/**
	 * Renders to the screen
	 */
	private void render() {
		for (Viewport viewport : viewports)
			viewport.render(width, height);
	}

	/**
	 * Adds a {@link Viewport} to this {@link Window}
	 * 
	 * @param viewport {@link Viewport} to add
	 * @return true if the {@link Viewport} has been added, false if this
	 *         {@link Window} already contains the {@link Viewport}
	 */
	public boolean addViewport(Viewport viewport) {
		if (!viewports.contains(viewport)) {
			viewports.add(viewport);
			return true;
		}
		return false;
	}

	/**
	 * Adds a {@link Viewport} at the specified index
	 * 
	 * @param index    index to add {@link Viewport} at
	 * @param viewport {@link Viewport} to add
	 * @return true if the {@link Viewport} has been added, false if this
	 *         {@link Window} already contains the {@link Viewport}
	 */
	public boolean addViewport(int index, Viewport viewport) {
		if (!viewports.contains(viewport)) {
			viewports.add(Math.max(Math.min(index, viewports.size()), 0), viewport);
			return true;
		}
		return false;
	}

	/**
	 * Adds a {@link Viewport} underneath another {@link Viewport}
	 * 
	 * @param check    {@link Viewport} that already exists
	 * @param viewport {@link Viewport} to add underneath {@code check}
	 * @return true if the {@link Viewport} has been added, false if this
	 *         {@link Window} already contains the {@link Viewport} or does not
	 *         contain the {@code check} {@link Viewport}
	 */
	public boolean addViewportUnder(Viewport check, Viewport viewport) {
		if (!viewports.contains(viewport) && viewports.contains(check)) {
			viewports.add(viewports.indexOf(check), viewport);
			return true;
		}
		return false;
	}

	/**
	 * Adds a {@link Viewport} above another {@link Viewport}
	 * 
	 * @param check    {@link Viewport} that already exists
	 * @param viewport {@link Viewport} to add above {@code check}
	 * @return true if the {@link Viewport} has been added, false if this
	 *         {@link Window} already contains the {@link Viewport} or does not
	 *         contain the {@code check} {@link Viewport}
	 */
	public boolean addViewportOver(Viewport check, Viewport viewport) {
		if (!viewports.contains(viewport) && viewports.contains(check)) {
			viewports.add(viewports.indexOf(check) + 1, viewport);
			return true;
		}
		return false;
	}

	/**
	 * Removes a {@link Viewport} from this {@link Window}
	 * 
	 * @param viewport {@link Viewport} to remove
	 * @return true if {@link Window} contained the {@link Viewport}, false
	 *         otherwise
	 */
	public boolean removeViewport(Viewport viewport) {
		return viewports.remove(viewport);
	}

	/**
	 * Sets if the window should close.
	 * 
	 * @param close true if the window should close, false otherwise
	 */
	public void setShouldClose(boolean close) {
		glfwSetWindowShouldClose(handle, close);
	}

	/**
	 * Executes a {@link Runnable} on the thread this window renders on
	 * 
	 * @param runnable {@link Runnable} to run
	 * @return a {@link CompletableFuture} that is completed once the
	 *         {@link Runnable} has completed running
	 */
	public CompletableFuture<Void> execute(Runnable runnable) {
		return runner.run(runnable);
	}

	/**
	 * Executes a {@link Callable} on the thread this window renders on
	 * 
	 * @param callable {@link Callable} to run
	 * @return a {@link CompletableFuture} that is completed with the return value
	 *         of the {@link Callable} once the {@link Callable} has completed
	 *         running
	 */
	public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
		return runner.run(supplier);
	}

	/**
	 * Call this, when the window is closed.<br />
	 * This closes the window with the given handle, decrements
	 * {@link #windowNumber} and runs {@link #onLastWindowClosed} if
	 * applicaple.<br />
	 * This will all be executed in the main Thread.
	 */
	private static void closeWindow(long handle) {
		Runner.MAIN.run(() -> {
			glfwDestroyWindow(handle);
			if (windowNumber.decrementAndGet() <= 0)
				onLastWindowClosed.run();
		});
	}

	/**
	 * Runs a {@link Runnable} once the last {@link Window} has been closed.<br />
	 * By default, this will stop {@link Runner#MAIN}.
	 * 
	 * @param onLastWindowClosed a new {@link Runnable} to run once all
	 *                           {@link Window}s are closed or {@code null} if
	 *                           nothing should be done
	 */
	public static void setLastWindowClosedHandler(Runnable onLastWindowClosed) {
		if (onLastWindowClosed == null)
			Window.onLastWindowClosed = () -> {
			};
		else
			Window.onLastWindowClosed = onLastWindowClosed;
	}

}
