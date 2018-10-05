package audio.soundjunk;

import java.util.concurrent.atomic.AtomicInteger;

public class Repeater implements Runnable {
	
	private final String[] paths;
	private final AtomicInteger rate;
	private final int interval;
	private volatile boolean enabled = false;

	public Repeater(AtomicInteger _rate, int _repeatEveryMS, String... _paths) {
		paths = _paths;
		interval = _repeatEveryMS;
		rate = _rate;
	}

	@Override
	public void run() {
		while (true) {
			if (enabled) {
				if (rate.get() > 0) {
					try {
						Thread.sleep(interval / rate.get());
						int random = (int) (Math.random() * paths.length);
						Wav.playSound(paths[random]);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}
}
