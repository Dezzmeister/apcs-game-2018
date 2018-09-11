package image;

public class ViewModel {
	private final Animation idle;
	private final Animation primary;
	private final Animation secondary;
	
	private int time = 0;
	private long lastTime = System.nanoTime();
	
	private int activeFrameIndex = 0;
	private SquareTexture activeFrame;
	private Animation activeAnimation;
	public float scaleValue;
	
	public ViewModel(Animation _idle, Animation _primary, Animation _secondary, float _scaleValue) {
		idle = _idle;
		primary = _primary;
		secondary = _secondary;
		
		activeFrame = idle.frames[0];
		activeAnimation = idle;
		scaleValue = _scaleValue;
	}
	
	public SquareTexture getActiveFrame() {
		return activeFrame;
	}
	
	public void update() {
		
		long newTime = System.nanoTime();
		int timePassed = (int)((newTime - lastTime) / 1000000);
		time += timePassed;
		int framesToSkip = time / activeAnimation.frameInterval;
		
		if (activeFrameIndex + framesToSkip >= activeAnimation.frames.length) {
			activeFrameIndex = 0;
			activeAnimation = idle;
			time = 0;
		} else {
			activeFrameIndex += framesToSkip;
			time -= (framesToSkip * activeAnimation.frameInterval);
		}
		
		lastTime = System.nanoTime();
		activeFrame = activeAnimation.frames[activeFrameIndex];
	}
	
	public void primaryFire() {
		if (activeAnimation == idle) {
			activeAnimation = primary;
			time = 0;
		}
	}
	
	public void secondaryFire() {
		if (activeAnimation == idle) {
			activeAnimation = secondary;
			time = 0;
		}
	}
	
	public static class Animation {
		private final SquareTexture[] frames;
		private final int frameInterval;
		
		public Animation(SquareTexture[] _frames, int _frameIntervalInMillis) {
			frames = _frames;
			frameInterval = _frameIntervalInMillis;
		}
	}
}
