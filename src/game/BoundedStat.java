package game;

import java.util.concurrent.atomic.AtomicInteger;

public final class BoundedStat {
	private final AtomicInteger level;
	private final int min;
	private final int max;
	
	public BoundedStat(final int _min, final int _max, final int start) {
		min = _min;
		max = _max;
		
		level = new AtomicInteger(start);
	}
	
	public BoundedStat(final int _min, final int _max) {
		this(_min, _max, _max);
	}
	
	public void lose(final int amount) {
		level.set(level.get() - amount < min ? min : level.get() - amount);
	}
	
	public void gain(final int amount) {
		level.set(level.get() + amount > max ? max : level.get() + amount);
	}
	
	public int get() {
		return level.get();
	}
}
