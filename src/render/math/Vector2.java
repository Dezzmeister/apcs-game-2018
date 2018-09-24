package render.math;

import java.util.Objects;

/**
 * Represents a 2D Vector with an x and y coordinate.
 *
 * @author Joe Desmond
 */
public class Vector2 {

	public static final Vector2 ORIGIN = new Vector2(0, 0, 0);
	
	public float x;
	public float y;
	public float length = -1;
	
	public Vector2(float _x, float _y) {
		x = _x;
		y = _y;
	}
	
	protected Vector2(float _x, float _y, float _length) {
		x = _x;
		y = _y;
		length = _length;
	}
	
	public void updateLength() {
		length = (float) Math.sqrt((x * x) + (y * y));
	}
	
	public Vector2 subtract(Vector2 v) {
		return new Vector2(x - v.x, y - v.y);
	}
	
	/**
	 * Returns the Euclidean distance from one Vector2 to another. Uses
	 * <code>Math.sqrt()</code>.
	 *
	 * @param _v0
	 *            first Vector2
	 * @param _v1
	 *            second Vector2
	 * @return Euclidean distance between both vectors
	 */
	public static float distance(Vector2 _v0, Vector2 _v1) {
		return (float) Math.sqrt(((_v0.x - _v1.x) * (_v0.x - _v1.x)) + ((_v0.y - _v1.y) * (_v0.y - _v1.y)));
	}
	
	public Vector2 normalize() {
		return new Vector2(x / length, y / length);
	}
	
	public Vector2 add(Vector2 v0) {
		return new Vector2(v0.x + x, v0.y + y);
	}
	
	public Vector2 scale(float f) {
		return new Vector2(x * f, y * f);
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof Vector2)) {
			return false;
		}
		
		Vector2 vec2 = (Vector2) o;
		
		return x == vec2.x && y == vec2.y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x,y);
	}
}
