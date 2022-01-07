package tv.floeze.bottleengine.client.graphics.click;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A {@link ClickListener} listens to click events using its
 * {@link #onClick(double, double)} method.
 * 
 * @author Floeze
 *
 */
@FunctionalInterface
public interface ClickListener {

	public static final int MOUSE_PRESSED = GLFW_PRESS;
	public static final int MOUSE_RELEASED = GLFW_RELEASE;

	public static final int MOUSE_LEFT = GLFW_MOUSE_BUTTON_LEFT;
	public static final int MOUSE_MIDDLE = GLFW_MOUSE_BUTTON_MIDDLE;
	public static final int MOUSE_RIGHT = GLFW_MOUSE_BUTTON_RIGHT;

	public static final int MODIFY_SHIFT = GLFW_MOD_SHIFT;
	public static final int MODIFY_CONTROL = GLFW_MOD_CONTROL;
	public static final int MODIFY_ALT = GLFW_MOD_ALT;
	public static final int MODIFY_SUPER = GLFW_MOD_SUPER;
	public static final int MODIFY_CAPS = GLFW_MOD_CAPS_LOCK;
	public static final int MODIFY_NUM = GLFW_MOD_NUM_LOCK;

	/**
	 * Does nothing on click
	 */
	public static final ClickListener NOTHING = (button, action, modifiers, x, y) -> {
	};

	/**
	 * Notifies this {@link ClickListener} that a click has happened at the
	 * specified coordinates.
	 * 
	 * @param button    index of the mouse button
	 * @param action    if the button was pressed or released
	 * @param modifiers if any modifier keys were pressed
	 * @param x         x-coordinate of the click
	 * @param y         y-coordinate of the click
	 */
	public void onClick(int button, int action, int modifiers, double x, double y);

}
