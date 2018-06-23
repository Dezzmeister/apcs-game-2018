package audio.soundjunk.localized;

import render.math.Vector2;

public class Speaker {
	public static final Speaker ATPLAYER = new Speaker(null);
	
	public Vector2 position = new Vector2(0,0);
	public String soundName;
	private boolean on = true;
	
	public Speaker(Vector2 _position) {
		position = _position;
	}
	
	public Speaker(float x, float y) {
		position = new Vector2(x,y);
	}
	
	public void turnOn() {
		on = true;
	}
	
	public void turnOff() {
		on = false;
	}
	
	public boolean isOn() {
		return on;
	}
}
