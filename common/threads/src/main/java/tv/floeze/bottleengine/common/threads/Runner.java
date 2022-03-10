package tv.floeze.bottleengine.common.threads;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A {@link Runner} that schedules tasks that can run once or repeat as fast as
 * possible.
 * 
 * @author Floeze
 *
 */
public final class Runner implements Runnable {

	/**
	 * A reserved {@link Runner} for the main Thread. <br />
	 * Call {@code Runner.MAIN.run()} as the last statement of your main method.
	 * This makes it possible to schedule tasks on the main thread. <br />
	 * Note however, that this will block until the {@link Runner} is stopped using
	 * {@code Runner.MAIN.stop()}. If you need to run code between that, schedule a
	 * task (using {@link Runner#run(Runnable)} or {@link Runner#repeat(Runnable)})
	 * on either the main {@link Runner} or a {@link Runner} in another
	 * {@link Thread} using {@link Runner#runInNewThread(String, boolean)}.
	 */
	public static final Runner MAIN = new Runner();

	private static final ThreadLocal<Runner> currentRunner = new ThreadLocal<>();

	private static final class Repeatable {

		public final Runnable runnable;
		public final long interval;
		public long lastTime;

		public Repeatable(Runnable runnable, long interval) {
			this.runnable = runnable;
			this.interval = interval;
		}

	}

	/**
	 * The possible states a {@link Runner} can have.
	 * 
	 * @see Runner#state
	 * 
	 * @author Floeze
	 *
	 */
	private enum State {
		IDLE, RUNNING, STOPPING;
	}

	/**
	 * the current state this {@link Runner} is in
	 */
	private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);

	/**
	 * All the tasks that should be run once
	 */
	private final Queue<Runnable> runnables = new LinkedList<>();

	/**
	 * The lock for {@link #runnables}
	 */
	private final Lock runnablesLock = new ReentrantLock();

	/**
	 * All the tasks that should be repeated
	 */
	private final List<Repeatable> repeatables = new ArrayList<>();

	/**
	 * The lock for {@link #repeatables}
	 */
	private final ReadWriteLock repeatablesLock = new ReentrantReadWriteLock();

	/**
	 * The stop handler
	 * 
	 * @see #onStop(Runnable)
	 */
	private Runnable onStop = () -> {
	};

	/**
	 * The finish handler
	 * 
	 * @see #onFinish(Runnable)
	 */
	private Runnable onFinish = () -> {
	};

	/**
	 * The start handler
	 * 
	 * @see #onStart(Runnable)
	 */
	private Runnable onStart = () -> {
	};

	/**
	 * Wait on this object to sleep; notify when adding new runnable/repeatable
	 */
	private final Object sleeper = new Object();

	/**
	 * Creates a new {@link Runner}
	 */
	public Runner() {
		// Nothing to initialize
	}

	/**
	 * Runs the {@link Runnable} once as soon as possible
	 * 
	 * @param runnable {@link Runnable} to run
	 * @return a {@link CompletableFuture} that completes with {@code null} as soon
	 *         as the {@link Runnable} has been run
	 */
	public CompletableFuture<Void> run(Runnable runnable) {
		// if called from current runner, run immediately
		if (getCurrentRunner() == this) {
			runnable.run();
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> future = new CompletableFuture<>();
		runnablesLock.lock();
		runnables.add(() -> {
			runnable.run();
			future.complete(null);
		});
		runnablesLock.unlock();
		synchronized (sleeper) {
			sleeper.notifyAll();
		}
		return future;
	}

	/**
	 * Runs the {@link Supplier} once as soon as possible and completes the returned
	 * {@link CompletableFuture} with the result.
	 * 
	 * @param supplier {@link Supplier} to run
	 * @return a {@link CompletableFuture} that completes with the result of the
	 *         {@link Supplier} as soon as the {@link Supplier} has been run
	 */
	public <T> CompletableFuture<T> run(Supplier<T> supplier) {
		// if called from current runner, run immediately
		if (getCurrentRunner() == this) {
			return CompletableFuture.completedFuture(supplier.get());
		}

		CompletableFuture<T> future = new CompletableFuture<>();
		runnablesLock.lock();
		runnables.add(() -> {
			T result = supplier.get();
			future.complete(result);
		});
		runnablesLock.unlock();
		synchronized (sleeper) {
			sleeper.notifyAll();
		}
		return future;
	}

	/**
	 * Repeats the {@link Runnable} as fast as possible
	 * 
	 * @param runnable {@link Runnable} to repeat
	 */
	public void repeat(Runnable runnable) {
		repeat(runnable, -1);
	}

	/**
	 * Repeats the {@link Runnable} while waiting at least the specified interval
	 * between executions.<br />
	 * <br />
	 * The time between may be longer, if different tasks block the thread while
	 * waiting, but will never be shorter than the specified interval.
	 * 
	 * @param runnable {@link Runnable} to repeat
	 * @param interval minimum interval to wait in ms
	 */
	public void repeat(Runnable runnable, long interval) {
		repeatablesLock.writeLock().lock();
		repeatables.add(new Repeatable(runnable, interval));
		repeatablesLock.writeLock().unlock();
		synchronized (sleeper) {
			sleeper.notifyAll();
		}
	}

	/**
	 * Executes this runner. <br />
	 * This will change the state of this Runner to running an will block until the
	 * Thread is stopped again. <br />
	 * This can only be run on one {@link Thread} at a time.
	 */
	@Override
	public synchronized void run() {
		currentRunner.set(this);

		state.set(State.RUNNING);
		onStart.run();

		// repeat while running
		while (state.get().equals(State.RUNNING)) {
			// Stop when interrupted
			if (Thread.interrupted())
				break;

			// wait until possible to read repeatables
			repeatablesLock.readLock().lock();

			// the minimum time at which a repeatable has to be run
			long minNextRun = Long.MAX_VALUE;

			// for every repeatable
			for (Repeatable repeatable : repeatables) {
				long currentTime = System.currentTimeMillis();

				// when this repeatable has to be run next
				long nextRun = repeatable.lastTime + repeatable.interval;

				if (nextRun < minNextRun)
					minNextRun = nextRun;

				// check if at least interval has passed
				if (nextRun > currentTime)
					continue;

				minNextRun = -1;

				// set that repeatable has ran
				repeatable.lastTime = currentTime;

				// run that repeatable
				repeatable.runnable.run();

				// run runnables as soon as possible (in between repeatables)
				runRunnables();
			}
			// unlock repeatables lock to allow scheduling again
			repeatablesLock.readLock().unlock();

			long waitTime = minNextRun - System.currentTimeMillis();

			// sleep if no repeatables run
			if (waitTime > 1) { // short times can be busy sleep, to prevent waiting too long
				try {
					synchronized (sleeper) {
						sleeper.wait(waitTime * 3 / 4); // only wait 3/4 of time, to prevent waiting too long
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			// run runnables even if no repeatables exist
			runRunnables();
		}

		state.set(State.IDLE);
		onFinish.run();

		currentRunner.remove();
	}

	private void runRunnables() {
		// try to lock repeatables to not block
		if (runnablesLock.tryLock()) {
			// run every repeatable
			while (!runnables.isEmpty())
				runnables.poll().run();
			// unlock repeatables again
			runnablesLock.unlock();
		}
	}

	/**
	 * Gets if the {@link Runner} is currently running
	 * 
	 * @return true if running, false otherwise
	 */
	public boolean isRunning() {
		return state.get().equals(State.RUNNING);
	}

	/**
	 * Gets if the {@link Runner} is currently idle
	 * 
	 * @return true if idle, false otherwise
	 */
	public boolean isIdle() {
		return state.get().equals(State.IDLE);
	}

	/**
	 * Gets if the {@link Runner} is currently stopping
	 * 
	 * @return true if stopping, false otherwise
	 */
	public boolean isStopping() {
		return state.get().equals(State.STOPPING);
	}

	/**
	 * If the {@link Runner} is running, this method stops the {@link Runner}, and
	 * then executes and the removes the stop handler.
	 * 
	 * If not, nothing happens.
	 */
	public void stop() {
		boolean running = state.compareAndSet(State.RUNNING, State.STOPPING);
		if (running) {
			onStop.run();
			onStop = () -> {
			};
		}
	}

	/**
	 * Executes this runner in a new {@link Thread}
	 * 
	 * @param name   name of the {@link Thread}
	 * @param daemon see {@link Thread#setDaemon(boolean)}
	 * @return this Runner
	 */
	public Runner runInNewThread(String name, boolean daemon) {
		Thread thread = new Thread(this, name);
		thread.setDaemon(daemon);
		thread.start();
		return this;
	}

	/**
	 * Sets the stop handler, but only if this Runner is currently idle. <br />
	 * This gets executed when the {@link Runner} is running and {@link #stop()} is
	 * called.
	 * 
	 * @param onStop the new stop handler
	 */
	public Runner onStop(Runnable onStop) {
		if (state.get().equals(State.IDLE))
			this.onStop = onStop;
		return this;
	}

	/**
	 * Sets the finish handler, but only if this Runner is currently idle. <br />
	 * This gets executed when the {@link Runner} is running and {@link #stop()} is
	 * called, but only after the last Tasks have finished executing.
	 * 
	 * @param onFinish the new finish handler
	 */
	public Runner onFinish(Runnable onFinish) {
		if (state.get().equals(State.IDLE))
			this.onFinish = onFinish;
		return this;
	}

	/**
	 * Sets the stop handler, but only if this Runner is currently idle. <br />
	 * This gets executed when the {@link Runner} starts ({@link #run()} is called).
	 * 
	 * @param onStart the new start handler
	 */
	public Runner onStart(Runnable onStart) {
		if (state.get().equals(State.IDLE))
			this.onStart = onStart;
		return this;
	}

	public static Runner getCurrentRunner() {
		return currentRunner.get();
	}

}