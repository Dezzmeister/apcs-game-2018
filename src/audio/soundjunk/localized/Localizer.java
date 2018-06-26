package audio.soundjunk.localized;

import java.util.List;

import render.core.Wall;
import render.math.RenderUtils;
import render.math.Vector2;

/**
 * Utility class for handling sound localization.
 *
 * @author Joe Desmond
 */
public class Localizer {

	private Vector2 listener = Vector2.ORIGIN;
	private Vector2 listenerDirection = Vector2.ORIGIN;
	/**
	 * The distance at which the gain is minGain.
	 */
	private float maxDistance;
	
	/**
	 * The gain at maxDistance. Not guaranteed to be the minimum gain, otherwise sounds may always be heard
	 * regardless of distance.
	 */
	private float minGain;
	private float maxGain = 0;
	
	public Localizer(float _maxDistance, float _minGain) {
		maxDistance = _maxDistance;
		minGain = _minGain;
	}
	
	public Localizer(float _maxDistance, float _minGain, float _maxGain) {
		maxDistance = _maxDistance;
		minGain = _minGain;
		maxGain = _maxGain;
	}
	
	/**
	 * Sets the point that this Localizer will use to determine sound balance,
	 * volume, and other stuff.
	 *
	 * @param _listener
	 *            Location to be used in sound calculations
	 */
	public void setListener(Vector2 _listener) {
		listener = _listener;
	}
	
	/**
	 * Sets the point that this Localizer uses to determine the direction in which
	 * the listener is facing.
	 * <p>
	 * 1. The listener is defined to be directly facing this point.
	 * <p>
	 * 2. The distance from this point to the listener does not matter.
	 *
	 * @param _direction
	 *            Direction in which the listener is facing
	 */
	public void setListenerDirection(Vector2 _direction) {
		listenerDirection = _direction;
	}
	
	public void setMaxDistance(float _maxDistance) {
		maxDistance = _maxDistance;
	}
	
	public void setMinGain(float _minGain) {
		minGain = _minGain;
	}
	
	public void setMaxGain(float _maxGain) {
		maxGain = _maxGain;
	}
	
	public float findBalance(List<Speaker> speakers) {
		int enabledSpeakers = 0;
		float totalBalance = 0;
		System.out.println(Math.random());
		
		for (Speaker s : speakers) {
			if (s.isOn()) {
				enabledSpeakers++;
				Vector2 speakerLocation;
				
				if (s == Speaker.ATPLAYER) {
					speakerLocation = listener;
				} else {
					speakerLocation = s.position;
				}
				
				float angleCos = RenderUtils.cosOfAngleBetweenLines(new Wall(listenerDirection, listener), new Wall(listener, speakerLocation));
				
				float angleSin = (float) Math.sqrt(1 - (angleCos * angleCos));
				
				boolean onLeft = RenderUtils.isLeftOfRay(listener, listenerDirection, speakerLocation);
				
				if (onLeft) {
					totalBalance -= angleSin;
				} else {
					totalBalance += angleSin;
				}
			}
		}
		
		return totalBalance / enabledSpeakers;
	}
	
	/**
	 * Uses a formula for finding the total resistance in a parallel circuit to determine total gain based on
	 * distance.
	 * 
	 * @param speakers
	 * @return
	 */
	public float findGain(List<Speaker> speakers) {
		float invDistances = 0;
		
		for (Speaker s : speakers) {
			if (s.isOn()) {
				float distance = Vector2.distance(listener, s.position);
			
				if (distance != 0) {
					float invDistance = 1/distance;
					invDistances += invDistance;
				}
			}
		}
		
		float completeDistance = 1/invDistances;
		if (invDistances != 0) {
			return maxGain - ((completeDistance/maxDistance)*(maxGain-minGain));
		} else {
			return Float.NEGATIVE_INFINITY;
		}
	}
}
