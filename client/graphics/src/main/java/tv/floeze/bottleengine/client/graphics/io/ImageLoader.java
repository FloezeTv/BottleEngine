package tv.floeze.bottleengine.client.graphics.io;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFWImage;

/**
 * A ImageLoader that loads an input image into memory and can convert that
 * image into different formats for use in the engine.
 * 
 * @author Floeze
 *
 */
public class ImageLoader {

	/**
	 * The image
	 */
	private final BufferedImage data;

	/**
	 * Creates a new {@link ImageLoader} from an image at the given path in the
	 * classpath
	 * 
	 * @param path path in the classpath where the image is located
	 * @throws IOException if an error occurred while loading the image
	 */
	public ImageLoader(String path) throws IOException {
		this(ImageLoader.class.getClassLoader().getResource(path));
	}

	/**
	 * Creates a new {@link ImageLoader} from an image at the given {@link URL}
	 * 
	 * @param url url where the image is located
	 * @throws IOException if an error occurred while loading the image
	 */
	public ImageLoader(URL url) throws IOException {
		data = ImageIO.read(url);
	}

	/**
	 * Creates a new {@link ImageLoader} from an image using an {@link InputStream}
	 * 
	 * @param stream input stream that contains the data of the image
	 * @throws IOException if an error occurred while loading the image
	 */
	public ImageLoader(InputStream stream) throws IOException {
		data = ImageIO.read(stream);
	}

	/**
	 * Creates a new {@link ImageLoader} from an image at the given {@link File}
	 * 
	 * @param file {@link File} where the image is at
	 * @throws IOException if an error occurred while loading the image
	 */
	public ImageLoader(File file) throws IOException {
		data = ImageIO.read(file);
	}

	/**
	 * Creates a new {@link ImageLoader} from the given image. The image will be
	 * copied.
	 * 
	 * @param image The image to load
	 */
	public ImageLoader(Image image) {
		data = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = data.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}

	/**
	 * Converts the loaded image to a {@link BufferedImage} with the type
	 * {@link BufferedImage#TYPE_INT_ARGB}.
	 * 
	 * @return a {@link BufferedImage} with the loaded Image data
	 * @see #asBufferedImage(int)
	 */
	public BufferedImage asBufferedImage() {
		return asBufferedImage(BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Converts the loaded image to a {@link BufferedImage} with the given type
	 * 
	 * @param type type of the image format (e.g.
	 *             {@link BufferedImage#TYPE_INT_ARGB}
	 * @return a {@link BufferedImage} with the loaded image data
	 */
	public BufferedImage asBufferedImage(int type) {
		BufferedImage image = new BufferedImage(data.getWidth(null), data.getHeight(null), type);
		Graphics g = image.getGraphics();
		g.drawImage(data, 0, 0, null);
		g.dispose();
		return image;
	}

	/**
	 * Converts the loaded image to a {@link GLFWImage}
	 * 
	 * @return a {@link GLFWImage} with the loaded image data
	 */
	public GLFWImage asGLFWImage() {
		int[] rgb = data.getRGB(0, 0, data.getWidth(), data.getHeight(), null, 0, data.getWidth());
		ByteBuffer bytes = ByteBuffer.allocateDirect(rgb.length * 4);
		for (int pixel : rgb) {
			// AA RR GG BB -> RR GG BB AA
			bytes.put((byte) ((pixel >> 16) & 0xFF));
			bytes.put((byte) ((pixel >> 8) & 0xFF));
			bytes.put((byte) ((pixel >> 0) & 0xFF));
			bytes.put((byte) ((pixel >> 24) & 0xFF));
		}
		bytes.flip();

		GLFWImage glfwImage = GLFWImage.malloc();
		glfwImage.set(data.getWidth(), data.getHeight(), bytes);

		return glfwImage;
	}

	/**
	 * Converts the loaded image to a {@link GLFWImage.Buffer}
	 * 
	 * @return a {@link GLFWImage.Buffer} with the loaded image data
	 */
	public GLFWImage.Buffer asGLFWImageBuffer() {
		GLFWImage.Buffer buffer = GLFWImage.malloc(1);
		GLFWImage img = asGLFWImage();
		buffer.put(0, img);
		img.free();
		return buffer;
	}

}
