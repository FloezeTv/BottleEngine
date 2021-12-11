package tv.floeze.bottleengine.client.graphics.window;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

/**
 * A Monitor on the users setup.
 * 
 * @author Floeze
 *
 */
public final class Monitor {

	/**
	 * The GLFW pointer
	 */
	private final long handle;
	/**
	 * The video mode
	 */
	private final GLFWVidMode mode;

	/**
	 * Creates a new {@link Monitor}
	 * 
	 * @param handle GLFW pointer
	 */
	private Monitor(long handle) {
		this.handle = handle;
		mode = glfwGetVideoMode(handle);
	}

	public long getHandle() {
		return handle;
	}

	public String getName() {
		return glfwGetMonitorName(handle);
	}

	public int getRefreshRate() {
		return mode.refreshRate();
	}

	public int getWidth() {
		return mode.width();
	}

	public int getHeight() {
		return mode.height();
	}

	public int getRedBits() {
		return mode.redBits();
	}

	public int getGreenBits() {
		return mode.greenBits();
	}

	public int getBlueBits() {
		return mode.blueBits();
	}

	/**
	 * Gets the users primary {@link Monitor}
	 * 
	 * @return the users primary {@link Monitor}
	 */
	public static Monitor getPrimaryMonitor() {
		Window.initGlfw();
		return new Monitor(glfwGetPrimaryMonitor());
	}

	/**
	 * Gets all the users {@link Monitor}s
	 * 
	 * @return an array of all the users {@link Monitor}s
	 */
	public static Monitor[] getMonitors() {
		Window.initGlfw();
		PointerBuffer buffer = glfwGetMonitors();
		Monitor[] monitors = new Monitor[buffer.remaining()];
		for (int m = 0; m < monitors.length; m++)
			monitors[m] = new Monitor(buffer.get(m));
		return monitors;
	}

}
