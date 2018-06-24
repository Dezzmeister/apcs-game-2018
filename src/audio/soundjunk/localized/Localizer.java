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
	
	public Localizer() {
		
	}
	
	/**
	 * Sets the point that this Localizer will use to determine sound balance, volume, and
	 * other stuff.
	 * 
	 * @param _listener
	 * 			Location to be used in sound calculations
	 */
	public void setListener(Vector2 _listener) {
		listener = _listener;
	}
	
	/**
	 * Sets the point that this Localizer uses to determine the direction in which the listener
	 * is facing. 
	 * <p>
	 * 1. The listener is defined to be directly facing this point.
	 * <p>
	 * 2. The distance from this point to the listener does not matter.
	 * 
	 * @param _direction
	 * 			Direction in which the listener is facing
	 */
	public void setListenerDirection(Vector2 _direction) {
		listenerDirection = _direction;
	}
	
	public float findBalance(List<Speaker> speakers) {		
		int enabledSpeakers = 0;
		float totalBalance = 0;
		
		for (Speaker s : speakers) {
			if (s.isOn()) {
				enabledSpeakers++;
				Vector2 speakerLocation;
				
				if (s == Speaker.ATPLAYER) {
					speakerLocation = listener;
				} else {
					speakerLocation = s.position;
				}
				
				float angle = (float) Math.toDegrees(RenderUtils.angleBetweenLines(new Wall(listenerDirection,listener), new Wall(listener,speakerLocation)));
				boolean onLeft = RenderUtils.isLeftOfRay(listener, listenerDirection, speakerLocation);
				
				//System.out.println(angle + " " + onLeft);
				
				if (onLeft) {
					if (angle <= 90) {
						totalBalance -= (angle/90);
					} else {
						totalBalance -= 1-((angle-90)/90);
					}
				} else {
					if (angle <= 90) {
						totalBalance += (angle/90);
					} else {
						totalBalance += 1-((angle-90)/90);
					}
				}
				
				System.out.println(totalBalance);
			}
		}
		
		return totalBalance/enabledSpeakers;
	}
}
