package render.core.gpu;

/**
 * Static utility methods for GPU code.
 *
 * @author Joe Desmond
 */
public class GPUtils {

	private GPUtils() {

	}

	public static float distance(float _v0x, float _v0y, float _v1x, float _v1y) {
		return (float) Math.sqrt(((_v0x - _v1x) * (_v0x - _v1x)) + ((_v0y - _v1y) * (_v0y - _v1y)));
	}

	public static float[] rayHitSegment(float rayStartX, float rayStartY, float rayDirectionX, float rayDirectionY,
			float wallv0x, float wallv0y, float wallv1x, float wallv1y) {
		float s1x;
		float s1y;
		float s2x;
		float s2y;

		s1x = rayDirectionX - rayStartX;
		s1y = rayDirectionY - rayStartY;

		s2x = wallv1x - wallv0x;
		s2y = wallv1y - wallv0y;
		
		float s, t;
		s = (-s1y * (rayStartX - wallv0x) + s1x * (rayStartY - wallv0y)) / (-s2x * s1y + s1x * s2y);
		t = (s2x * (rayStartY - wallv0y) - s2y * (rayStartX - wallv0x)) / (-s2x * s1y + s1x * s2y);
		
		if (s >= 0 && s <= 1 && t >= 0) {
			return new float[] {rayStartX + (t * s1x), rayStartY + (t * s1y)};
		}
		return null;
	}
}
