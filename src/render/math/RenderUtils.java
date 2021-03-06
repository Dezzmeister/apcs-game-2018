package render.math;

import render.core.Pillar;
import render.core.Wall;
import render.core.true3D.Line;

/**
 * This class holds utility methods for rendering and other stuff.
 *
 * @author Joe Desmond
 */
public final class RenderUtils {
	
	public static final float HALF_PI = (float) Math.PI / 2.0f;
	public static final float TWO_PI = (float) Math.PI * 2.0f;

	/**
	 * This class is meant as a utility class; it holds only static utility methods.
	 * I don't want people creating instances of it.
	 */
	private RenderUtils() {
		throw new RuntimeException("don't you dare try to create a RenderUtils object");
	}

	/**
	 * Calculates the position where a ray intersects a Wall. Returns null if it
	 * doesn't.
	 *
	 * @param rayStart
	 *            ray endpoint
	 * @param rayDirection
	 *            and point determining the direction of the ray
	 * @param segment
	 *            a Wall
	 * @return point where the ray hits the Wall, or null
	 */
	public static Vector2 rayHitSegment(Vector2 rayStart, Vector2 rayDirection, Wall segment) {
		Vector2 r0 = rayStart;
		Vector2 r1 = rayDirection;
		Vector2 a = segment.v0;
		Vector2 b = segment.v1;

		Vector2 s1, s2;
		s1 = new Vector2(r1.x - r0.x, r1.y - r0.y);
		s2 = new Vector2(b.x - a.x, b.y - a.y);
		
		float s, t;
		s = (-s1.y * (r0.x - a.x) + s1.x * (r0.y - a.y)) / (-s2.x * s1.y + s1.x * s2.y);
		t = (s2.x * (r0.y - a.y) - s2.y * (r0.x - a.x)) / (-s2.x * s1.y + s1.x * s2.y);
		
		if (s >= 0 && s <= 1 && t >= 0) {
			return new Vector2(r0.x + (t * s1.x), r0.y + (t * s1.y));
		}
		return null;
	}

	// TODO: do this
	public static Vector2 rayHitCircle(Vector2 rayStart, Vector2 rayDirection, Pillar circle) {
		return null;
	}

	/**
	 * Returns the angle between two Walls, in radians.
	 *
	 * @param wall1
	 *            first wall
	 * @param wall2
	 *            second wall
	 * @return angle in radians
	 */
	public static float altAngleBetweenLines(Wall wall1, Wall wall2) {
		float angle1 = (float) Math.atan2(wall1.v0.y - wall1.v1.y, wall1.v0.x - wall1.v1.x);
		float angle2 = (float) Math.atan2(wall2.v0.y - wall2.v1.y, wall2.v0.x - wall2.v1.x);
		
		return Math.abs(angle1 - angle2);
	}

	public static float angleBetweenVectors(Vector2 v0, Vector2 v1) {
		float dot = Vector2.dot(v0, v1);

		v0.updateLength();
		v1.updateLength();

		float cos = dot / (v0.length * v1.length);

		return (float) Math.acos(cos);
	}

	@Deprecated
	public static float angleBetweenLines(Wall wall1, Wall wall2) {

		return (float) Math.acos(cosOfAngleBetweenLines(wall1, wall2));
	}

	public static float cosOfAngleBetweenLines(Wall wall1, Wall wall2) {
		Vector2 A = new Vector2(wall2.v0.x - wall1.v0.x, wall2.v0.y - wall1.v0.y);
		Vector2 B = new Vector2(wall2.v1.x - wall1.v1.x, wall2.v1.y - wall1.v1.y);

		A.updateLength();
		B.updateLength();

		Vector2 normA = A.normalize();
		Vector2 normB = B.normalize();

		float dot = (normA.x * normB.x) + (normA.y * normB.y);

		return dot;
	}

	/**
	 * Returns true if the point <code>testPoint</code> is to the left of the ray
	 * defined by <code>start</code> and <code>direction</code>. Returns false if
	 * the point is to the right or colinear.
	 *
	 * @param start
	 *            Beginning endpoint of the ray
	 * @param direction
	 *            Determines the direction of the ray, and therefore what defines
	 *            right/left
	 * @param testPoint
	 *            Point being tested
	 * @return True if on left of ray, false if on right or colinear
	 */
	public static boolean isLeftOfRay(Vector2 start, Vector2 direction, Vector2 testPoint) {

		return (direction.x - start.x) * (testPoint.y - start.y) > (direction.y - start.y) * (testPoint.x - start.x);
	}

	public static int darkenWithThreshold(int color, float normValue, int threshold) {
		int darkenBy = (int) (normValue * threshold);
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;

		red -= (red - darkenBy >= 0) ? darkenBy : red;
		green -= (green - darkenBy >= 0) ? darkenBy : green;
		blue -= (blue - darkenBy >= 0) ? darkenBy : blue;

		return (red << 16) | (green << 8) | blue;
	}

	public static double clamp(float val, float minVal, float maxVal) {
		if (val < minVal) {
			return minVal;
		}
		if (val > maxVal) {
			return maxVal;
		}
		return val;
	}

	public static Vector3 linePlaneIntersection(Line l, Triangle plane) {
		Vector3 p0 = l.v0;
		Vector3 p1 = l.v1;
		Vector3 p_co = plane.v0;
		Vector3 p_no = plane.getNormal();
		final float epsilon = 0.000001f;
		
		Vector3 u = p0.minus(p1);
		float dot = Vector3.dot(p_no, u);
		
		if (Math.abs(dot) > epsilon) {
			Vector3 w = p0.minus(p_co);
			float fac = -Vector3.dot(p_no, w) / dot;
			
			if (fac > 0) {
				return null;
			}
			
			u = u.scale(fac);
			return p0.plus(u);
		}
		
		return null;
	}
}
