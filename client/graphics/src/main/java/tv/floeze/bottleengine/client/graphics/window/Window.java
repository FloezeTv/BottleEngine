package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;

import tv.floeze.bottleengine.client.graphics.io.ImageLoader;

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
	 * - make a Viewport class that handles the rendering and adds the option to
	 * have multiple viewports
	 * 
	 * - add a ThreadManager class that handles the creation (and destruction) of
	 * threads and executing code on those threads including the main thread
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
	 * 
	 * @see #executeMain(Runnable)
	 * @see #executeMain(Callable)
	 */
	private static final AtomicInteger windowNumber = new AtomicInteger(0);

	/**
	 * A queue of {@link Runnable}s to run on the main thread
	 */
	private static final BlockingQueue<Runnable> mainRunnables = new LinkedBlockingQueue<Runnable>();

	/**
	 * The handle of this window
	 */
	private final long handle;

	/**
	 * The thread this window renders in
	 */
	private final Thread thread;

	/**
	 * A queue of {@link Runnable}s to run on the thread this window renders in
	 * ({@link #thread})
	 * 
	 * @see #execute(Runnable)
	 * @see #execute(Callable)
	 */
	private final BlockingQueue<Runnable> runnables = new LinkedBlockingQueue<Runnable>();

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

		// set callbacks
		glfwSetFramebufferSizeCallback(handle, (window, w, h) -> execute(() -> glViewport(0, 0, w, h)));
		glfwSetWindowCloseCallback(handle, window -> glfwSetWindowShouldClose(window, closeHandler.get()));

		thread = new Thread(() -> {
			glfwMakeContextCurrent(handle);
			// v-sync
			glfwSwapInterval(1);

			GL.createCapabilities();

			glViewport(0, 0, width, height);

			while (!glfwWindowShouldClose(handle)) {
				while (!runnables.isEmpty())
					runnables.poll().run();

				clear();

				render();

				glfwSwapBuffers(handle);
			}

			glfwMakeContextCurrent(NULL);
			executeMain(() -> {
				glfwDestroyWindow(handle);
				windowNumber.decrementAndGet();
			});

		}, "renderer-" + title.toString().replaceAll(" ", "_"));
		thread.start();

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
		// TODO: do some actual rendering
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
	 * @return a {@link Future} that is completed once the {@link Runnable} has
	 *         completed running
	 */
	public Future<Void> execute(Runnable runnable) {
		FutureTask<Void> futureTask = new FutureTask<Void>(runnable, null);
		runnables.add(futureTask);
		return futureTask;
	}

	/**
	 * Executes a {@link Callable} on the thread this window renders on
	 * 
	 * @param callable {@link Callable} to run
	 * @return a {@link Future} that is completed with the return value of the
	 *         {@link Callable} once the {@link Callable} has completed running
	 */
	public <T> Future<T> execute(Callable<T> callable) {
		FutureTask<T> futureTask = new FutureTask<T>(callable);
		runnables.add(futureTask);
		return futureTask;
	}

	/**
	 * Executes a {@link Runnable} on the main thread
	 * 
	 * @param runnable {@link Runnable} to run
	 * @return a {@link Future} that is completed once the {@link Runnable} has
	 *         completed running
	 */
	public Future<Void> executeMain(Runnable runnable) {
		FutureTask<Void> futureTask = new FutureTask<Void>(runnable, null);
		mainRunnables.add(futureTask);
		return futureTask;
	}

	/**
	 * Executes a {@link Callable} on the main thread
	 * 
	 * @param callable {@link Callable} to run
	 * @return a {@link Future} that is completed with the return value of the
	 *         {@link Callable} once the {@link Callable} has completed running
	 */
	public <T> Future<T> executeMain(Callable<T> callable) {
		FutureTask<T> futureTask = new FutureTask<T>(callable);
		mainRunnables.add(futureTask);
		return futureTask;
	}

	/**
	 * Polls events and allows to execute methods on the main thread.
	 * 
	 * This method blocks until all windows are closed.
	 * 
	 * Call this on the main thread only!
	 */
	public static synchronized void mainThread() {
		windowNumber.updateAndGet(i -> i < 0 ? 0 : i);
		while (windowNumber.get() > 0) {
			while (!mainRunnables.isEmpty())
				mainRunnables.poll().run();
			glfwPollEvents();
		}
	}

	/**
	 * @param application application code to run in separate Thread
	 * 
	 * @see #mainThread()
	 */
	public static synchronized void mainThread(Runnable application) {
		new Thread(application, "application").start();
		mainThread();
	}

}
