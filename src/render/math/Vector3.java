package render.math;

/**
 * Represents a Vector in 3D space.
 *
 * @author Joe Desmond
 */
public class Vector3 {
	
	public static final Vector3 ORIGIN = new Vector3(0, 0, 0, 0);

	public float x;
	public float y;
	public float z;

	public float length;

	public Vector3(float _x, float _y, float _z) {
		x = _x;
		y = _y;
		z = _z;

		updateLength();
	}

	/**
	 * This constructor is used by <code>normalize()</code> to avoid overhead from
	 * <code>updateLength()</code>, because a Vector returned by
	 * <code>normalize</code> is guaranteed to have a length of 1.
	 * <p>
	 * This constructor is also used by <code>ORIGIN</code>.
	 *
	 * @param _x
	 * @param _y
	 * @param _z
	 * @param _length
	 */
	private Vector3(float _x, float _y, float _z, float _length) {
		x = _x;
		y = _y;
		z = _z;

		length = _length;
	}

	public static float distance(Vector3 v0, Vector3 v1) {
		return (float) Math.sqrt(
				((v0.x - v1.x) * (v0.x - v1.x)) + ((v0.y - v1.y) * (v0.y - v1.y)) + ((v0.z - v1.z) * (v0.z - v1.z)));
	}

	public Vector3 add(Vector3 v) {
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}

	public static Vector3 cross(Vector3 a, Vector3 b) {
		float x = (a.y * b.z) - (a.z * b.y);
		float y = (a.z * b.x) - (a.x * b.z);
		float z = (a.x * b.y) - (a.y * b.x);

		return new Vector3(x, y, z);
	}

	/**
	 * Returns a Vector with the same direction as this one, but with a length of
	 * 1.0. Ensure that the length of this Vector is updated for accurate results.
	 *
	 * @return
	 */
	public Vector3 normalize() {
		return new Vector3(x / length, y / length, z / length, 1.0f);
	}

	/**
	 * This method should be called by the user to update the length of the Vector.
	 * This kind of control is given to the user to ensure that this class is as
	 * fast as possible. As a result, the user must be responsible and know when
	 * they need to update the length before an operation.
	 */
	public void updateLength() {
		length = distance(ORIGIN, this);
	}
}
