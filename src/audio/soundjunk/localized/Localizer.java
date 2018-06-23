package audio.soundjunk.localized;

import java.util.List;

import render.math.Vector2;

/**
 * Utility class for handling sound localization.
 *
 * @author Joe Desmond
 */
public class Localizer {
	private Vector2 listener = Vector2.ORIGIN;
	
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
	
	public float findBalance(List<Speaker> speakers) {
		//TODO: write this method
		return 0;
	}
}
