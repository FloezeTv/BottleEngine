package tv.floeze.bottleengine.common.threads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Reference that is local to the current {@link Runner} <br />
 * <br />
 * 
 * Example: <br />
 * Setup <br />
 * {@code
 * private static final RunnerLocal<Object> objects;
 * } <br />
 * 
 * Getting a value <br />
 * {@code
 * private final Object value;
 * 
 *\// in some method
 * value = objects.get(this);
 * } <br />
 * 
 * Stopping using a value <br />
 * {@code
 * objects.abandon(this);
 * }
 * 
 * 
 * @author Floeze
 *
 * @param <T> type of the value
 */
public class RunnerLocal<T> {

	private static final class Reference {

		private final Object value;

		public Reference(Object value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (value == obj)
				return true;
			if (!(obj instanceof Reference))
				return false;
			Reference other = (Reference) obj;
			return value == other.value;
		}

		@Override
		public String toString() {
			return "Reference{ " + value + " }";
		}

	}

	private final Map<Runner, T> values = new HashMap<>();

	private final Map<Runner, Set<Reference>> users = new HashMap<>();

	private final Supplier<T> creator;

	private final Function<T, Boolean> destroyer;

	/**
	 * Creates a new reference that is local to the current {@link Runner}.
	 * 
	 * @param value value for all runners
	 */
	public RunnerLocal(T value) {
		this(() -> value, t -> false);
	}

	/**
	 * Creates a new reference that is local to the current {@link Runner}.
	 * 
	 * @param creator   creates a new value
	 * @param destroyer destroys an unused value
	 */
	public RunnerLocal(Supplier<T> creator, Consumer<T> destroyer) {
		this.creator = creator;
		this.destroyer = t -> {
			destroyer.accept(t);
			return true;
		};
	}

	/**
	 * Creates a new reference that is local to the current {@link Runner}.
	 * 
	 * @param creator   creates a new value
	 * @param destroyer destroys an unused value. Return {@code true} if the value
	 *                  was destroyed or {@code false} if the value was kept
	 */
	public RunnerLocal(Supplier<T> creator, Function<T, Boolean> destroyer) {
		this.creator = creator;
		this.destroyer = destroyer;
	}

	/**
	 * Gets the value for this {@link Runner} and keeps track of the reference. Use
	 * {@link #abandon()} to abandon that reference, so that the value can
	 * eventually be destroyed.
	 * 
	 * Consider caching the returned value, because calling {@link #get(Object)} is
	 * slower.
	 * 
	 * @param parent parent of the reference, usually {@code this}
	 * @return the value local to the current {@link Runner}
	 */
	public synchronized T get(Object parent) {
		getLocalUsers().add(new Reference(parent));
		return values.computeIfAbsent(Runner.getCurrentRunner(), k -> creator.get());
	}

	/**
	 * Abandons the reference and destroys the value if it is unused.
	 * 
	 * @param parent parent of the reference, usually {@code this}
	 */
	public synchronized void abandon(Object parent) {
		Set<Reference> localUsers = getLocalUsers();
		localUsers.remove(new Reference(parent));
		if (localUsers.isEmpty())
			values.computeIfPresent(Runner.getCurrentRunner(),
					(k, v) -> Boolean.TRUE.equals(destroyer.apply(v)) ? null : v);
	}

	private Set<Reference> getLocalUsers() {
		return users.computeIfAbsent(Runner.getCurrentRunner(), k -> new HashSet<>());
	}

}
