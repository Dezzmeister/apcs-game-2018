package render.math;

/**
 * Represents a 2D Vector with an x and y coordinate.
 *
 * @author Joe Desmond
 */
public class Vector2 {
	public static final Vector2 ORIGIN = new Vector2(0,0);
	
	public float x;
	public float y;
	public float length = -1;
	
	public Vector2(float _x, float _y) {
		x = _x;
		y = _y;
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
	
	public Vector2 add(Vector2 v0) {
		return new Vector2(v0.x + x, v0.y + y);
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
