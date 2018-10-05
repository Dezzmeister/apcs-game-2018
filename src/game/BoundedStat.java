package game;

import java.util.concurrent.atomic.AtomicReference;

public final class BoundedStat {
	
	private final AtomicReference<Float> level;
	private final int min;
	private final int max;

	public BoundedStat(final int _min, final int _max, final float start) {
		min = _min;
		max = _max;

		level = new AtomicReference<Float>(start);
	}

	public BoundedStat(final int _min, final int _max) {
		this(_min, _max, _max);
	}

	public void lose(final float amount) {
		level.set(level.get() - amount < min ? min : level.get() - amount);
	}

	public void gain(final float amount) {
		level.set(level.get() + amount > max ? max : level.get() + amount);
	}

	public float get() {
		return level.get();
	}
}
