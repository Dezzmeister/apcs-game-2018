package image;

import java.util.HashMap;
import java.util.Map;

/**
 * A ViewModel that has an idle state, a primary state, and a secondary state. When this ViewModel is
 * in one of its states, it will play the corresponding animation, provided <code>update()</code> is called
 * regularly. ViewModel also supports the addition of more states and animations that can be activated
 * by <code>activateState()</code>.
 *
 * @author Joe Desmond
 */
public class ViewModel {
	private Map<String, Animation> states = new HashMap<String, Animation>();
	
	private final Animation idle;
	private final Animation primary;
	private final Animation secondary;
	
	private String activeState = "idle";
	private String defaultState = "idle";
	
	private int time = 0;
	private long lastTime = System.nanoTime();
	
	private int activeFrameIndex = 0;
	private SquareTexture activeFrame;
	private Animation activeAnimation;
	public float scaleValue;
	private boolean locked = false;
	
	public ViewModel(Animation _idle, Animation _primary, Animation _secondary, float _scaleValue) {
		idle = _idle;
		primary = _primary;
		secondary = _secondary;
		
		activeFrame = idle.frames[0];
		activeAnimation = idle;
		scaleValue = _scaleValue;
		
		states.put("idle",idle);
		states.put("primary", primary);
		states.put("secondary", secondary);
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
			activeState = defaultState;
			activeAnimation = states.get(defaultState);
			time = 0;
		} else {
			activeFrameIndex += framesToSkip;
			time -= (framesToSkip * activeAnimation.frameInterval);
		}
		
		lastTime = System.nanoTime();
		activeFrame = activeAnimation.frames[activeFrameIndex];
	}
	
	/**
	 * Returns true if the action was successful; the primary fire animation could
	 * be started.
	 * 
	 * @return true if the animation was started successfully
	 */
	public boolean primaryFire() {
		if (activeState.equals(defaultState) && !locked && primary != null) {
			activeState = "primary";
			activeAnimation = primary;
			time = 0;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the action was successful; the secondary fire animation could
	 * be started.
	 * 
	 * @return true if the animation was started successfully
	 */
	public boolean secondaryFire() {
		if (activeState.equals(defaultState) && !locked && secondary != null) {
			activeState = "secondary";
			activeAnimation = secondary;
			time = 0;
			return true;
		}
		
		return false;
	}
	
	public void addState(String stateName, Animation animation) {
		states.put(stateName, animation);
	}
	
	/**
	 * Returns true if the action was successful; the state's animation could
	 * be started.
	 * 
	 * @return true if the animation was started successfully; false if the state does not exist or the animation could not be started
	 */
	public boolean activateState(String stateName) {
		Animation anim = states.get(stateName);
		
		if (anim == null) {
			System.err.println("No such ViewModel state \"" + stateName + "\" exists!");
		} else {
			if (activeState.equals(defaultState) && !locked) {
				activeState = stateName;
				activeAnimation = anim;
				time = 0;
				return true;
			}
		}
		
		return false;
	}
	
	public void setDefaultState(String stateName) {
		Animation anim = states.get(stateName);
		
		if (anim == null) {
			System.err.println("No such ViewModel state \"" + stateName + "\" exists!");
		} else {
			defaultState = stateName;
		}
	}
	
	public void lock() {
		locked = true;
	}
	
	public void unlock() {
		locked = false;
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
