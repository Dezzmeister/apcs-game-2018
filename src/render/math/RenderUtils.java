package render.math;

import render.core.Pillar;
import render.core.Wall;

/**
 * This class holds utility methods for rendering and other stuff.
 *
 * @author Joe Desmond
 */
public class RenderUtils {
	
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
	public static float angleBetweenLines(Wall wall1, Wall wall2) {
		float angle1 = (float) Math.atan2(wall1.v0.y - wall1.v1.y, wall1.v0.x - wall1.v1.x);
		float angle2 = (float) Math.atan2(wall2.v0.y - wall2.v1.y, wall2.v0.x - wall2.v1.x);

		return Math.abs(angle1 - angle2);
	}
	
	/**
	 * Returns a negative number if <code>point</code> lies on one side of the line,
	 * a positive number if it lies on the other, and 0 if it is colinear.
	 * 
	 * @param line
	 * 			Wall representing the line
	 * @param point
	 * 			Point in question
	 * @return
	 * 			A float, the sign of which determines which side of the line the point is on
	 */
	public static float whichSideOfLine(Wall line, Vector2 point) {
		
		return new Matrix2(new float[] {
				line.v0.x-point.x, line.v1.x-point.x,
				line.v0.y-point.y, line.v1.y-point.y
		}).determinant();
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
}
