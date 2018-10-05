package render.light;

import render.math.Vector3;

public class Light {
	
	public Vector3 pos;
	public int color;
	public boolean calculating = false;

	public Light(Vector3 _pos, int _color) {
		pos = _pos;
		color = _color;
	}
}
