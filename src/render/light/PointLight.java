package render.light;

import render.math.Vector3;

/**
 * Represents a point-light source in the world.
 *
 * @author Joe Desmond
 */
public class PointLight {
	public Vector3 pos;
	
	public PointLight(Vector3 _pos) {
		pos = _pos;
	}
}
