package tv.floeze.bottleengine.client.graphics;

/**
 * Meters the number of times {@link #render()} is called per second to
 * calculate a FPS value.
 * 
 * @author Floeze
 *
 */
public class FPSMeter implements Renderable {

	private static final double NANOS = 1E+9;

	/**
	 * Circular buffer
	 */
	private final long[] buffer;
	/**
	 * buffer index
	 */
	private int index;

	public FPSMeter(int samples) {
		buffer = new long[samples];
	}

	/**
	 * Gets the exact value of frames per second. <br />
	 * Most of the time it is enough to call {@link #getFps()}, which gives a
	 * rounded value.
	 * 
	 * @return the exact value of frames per second
	 * 
	 * @see #getFps()
	 */
	public double getFpsDouble() {
		long sum = 0;
		for (int i = 1; i < buffer.length; i++) {
			int current = (index + i) % buffer.length;
			int last = (index + i - 1) % buffer.length;
			sum += buffer[current] - buffer[last];
		}
		return sum != 0 ? buffer.length / (sum / NANOS) : 0;
	}

	/**
	 * Gets the whole number of frames per second.
	 * 
	 * @return the number of frames per second rounded to the closest integer
	 */
	public int getFps() {
		return (int) Math.round(getFpsDouble());
	}

	@Override
	public void render() {
		buffer[index] = System.nanoTime();
		index = (index + 1) % buffer.length;
	}

}
