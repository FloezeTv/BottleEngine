package tv.floeze.bottleengine.client.graphics.keys;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A {@link KeyListener} listens to key events using its
 * {@link #onKey(int, int, int, int)} method.
 * 
 * @author Floeze
 *
 */
public interface KeyListener {

	public static final int KEY_PRESS = GLFW_PRESS;
	public static final int KEY_RELEASE = GLFW_RELEASE;
	public static final int KEY_REPEAT = GLFW_REPEAT;

	public static int MODIFY_ALT = GLFW_MOD_ALT;
	public static int MODIFY_CAPS_LOCK = GLFW_MOD_CAPS_LOCK;
	public static int MODIFY_CONTROL = GLFW_MOD_CONTROL;
	public static int MODIFY_NUM_LOCK = GLFW_MOD_NUM_LOCK;
	public static int MODIFY_SHIFT = GLFW_MOD_SHIFT;
	public static int MODIFY_SUPER = GLFW_MOD_SUPER;

	/**
	 * Does nothing on key press
	 */
	public static final KeyListener NOTHING = (key, scancode, action, mods) -> {
	};

	/**
	 * Notifies this {@link KeyListener} that a key has been pressed/released
	 * 
	 * @param key      the key that was pressed/released
	 * @param scancode the scancode of the key (platform specific)
	 * @param action   if the key was pressed/released or is repeating
	 * @param mods     which modifier keys are held down
	 */
	public void onKey(int key, int scancode, int action, int mods);

}
