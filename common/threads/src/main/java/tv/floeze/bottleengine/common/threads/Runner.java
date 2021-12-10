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

	/**
	 * The possible states a {@link Runner} can have.
	 * 
	 * @see Runner#state
	 * 
	 * @author Floeze
	 *
	 */
	private static enum State {
		IDLE, RUNNING, STOPPING;
	}

	/**
	 * the current state this {@link Runner} is in
	 */
	private final AtomicReference<State> state = new AtomicReference<State>(State.IDLE);

	/**
	 * All the tasks that should be run once
	 */
	private final Queue<Runnable> runnables = new LinkedList<Runnable>();

	/**
	 * The lock for {@link #runnables}
	 */
	private final Lock runnablesLock = new ReentrantLock();

	/**
	 * All the tasks that should be repeated
	 */
	private final List<Runnable> repeatables = new ArrayList<Runnable>();

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
	 * Creates a new {@link Runner}
	 */
	public Runner() {
	}

//	/**
//	 * Creates a new {@link Runner}
//	 * 
//	 * @param onStop stop handler to execute when this thread is stopped
//	 */
//	protected Runner(Runnable onStop) {
//		this.onStop = onStop;
//	}

	/**
	 * Runs the {@link Runnable} once as soon as possible
	 * 
	 * @param runnable {@link Runnable} to run
	 * @return a {@link CompletableFuture} that completes with {@code null} as soon
	 *         as the {@link Runnable} has been run
	 */
	public CompletableFuture<Void> run(Runnable runnable) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		runnablesLock.lock();
		runnables.add(() -> {
			runnable.run();
			future.complete(null);
		});
		runnablesLock.unlock();
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
		CompletableFuture<T> future = new CompletableFuture<T>();
		runnablesLock.lock();
		runnables.add(() -> {
			T result = supplier.get();
			future.complete(result);
		});
		runnablesLock.unlock();
		return future;
	}

	/**
	 * Repeats the {@link Runnable} as fast as possible
	 * 
	 * @param runnable {@link Runnable} to repeat
	 */
	public void repeat(Runnable runnable) {
		repeatablesLock.writeLock().lock();
		repeatables.add(runnable);
		repeatablesLock.writeLock().unlock();
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
			// wait until possible to read repeatables
			repeatablesLock.readLock().lock();
			// for every repeatable
			for (Runnable repeatable : repeatables) {
				// run that repeatable
				repeatable.run();

				// run runnables as soon as possible (in between repeatables)
				runRunnables();
			}
			// unlock repeatables lock to allow scheduling again
			repeatablesLock.readLock().unlock();

			// run runnables even if no repeatables exist
			runRunnables();
		}

		state.set(State.IDLE);
		onFinish.run();

		currentRunner.set(null);
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

//	/**
//	 * Sets the stop handler, but only if this Runner is currently idle.
//	 * 
//	 * @param onStop the new stop handler
//	 */
//	public void setStopHandler(Runnable onStop) {
//		if (state.get().equals(State.IDLE))
//			this.onStop = onStop;
//	}

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