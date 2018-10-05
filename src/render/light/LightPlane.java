package render.light;

import render.core.Wall;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

/**
 * Four {@link Vector3}'s define the corners of a 4-edged LightPlane, which
 * contains lightmap data. This fits our Raycaster since every tile/face has 4
 * edges.
 *
 * @author Joe Desmond
 */
public final class LightPlane {
	
	public final Vector3 v0;
	public final Vector3 v1;
	public final Vector3 v2;
	public final Vector3 v3;

	public final Triangle t0;
	public final Triangle t1;

	public final int lumelWidth;
	public final int lumelHeight;
	public final int[] map;

	/**
	 * Creates a LightPlane from four Vectors defining the corners of a quad, and
	 * two ints defining the resolution of the LightPlane, in lumels.
	 *
	 * @param _v0
	 *            First vertex
	 * @param _v1
	 *            Second vertex
	 * @param _v2
	 *            Third vertex
	 * @param _v3
	 *            Fourth vertex
	 * @param _resWidth
	 *            x-resolution of the lightplane, in lumels
	 * @param _resHeight
	 *            y-resolution of the lightplane, in lumels
	 */
	public LightPlane(Vector3 _v0, Vector3 _v1, Vector3 _v2, Vector3 _v3, int _resWidth, int _resHeight) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
		v3 = _v3;

		t0 = new Triangle(v0, v1, v2).setUVCoords(new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1));
		t1 = new Triangle(v2, v3, v1).setUVCoords(new Vector2(1, 1), new Vector2(0, 1), new Vector2(0, 0));

		lumelWidth = _resWidth;
		lumelHeight = _resHeight;

		map = new int[lumelWidth * lumelHeight];
		ensureCoplanarity();
	}

	/**
	 * Creates a 3D LightPlane for a ceiling tile located at (x,y) with a resolution
	 * of <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 * LightPlanes for floor and ceiling tiles should be generated for
	 * fake/<code>SPACE</code> Blocks.
	 *
	 * @param x
	 *            x coordinate of the ceiling tile
	 * @param y
	 *            y coordinate of the ceiling tile
	 * @param squareResolution
	 *            resolution of the LightPlane, in lumels
	 * @return A LightPlane for this ceiling tile
	 */
	public static final LightPlane createFromCeilingTile(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x, y, 1);
		Vector3 v1 = new Vector3(x + 1, y, 1);
		Vector3 v2 = new Vector3(x + 1, y + 1, 1);
		Vector3 v3 = new Vector3(x, y + 1, 1);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}

	/**
	 * Creates a 3D LightPlane for a floor tile located at (x,y) with a resolution
	 * of <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 * LightPlanes for floor and ceiling tiles should be generated for
	 * fake/<code>SPACE</code> Blocks.
	 *
	 * @param x
	 *            x coordinate of the floor tile
	 * @param y
	 *            y coordinate of the floor tile
	 * @param squareResolution
	 *            resolution of both sides of the LightPlane, in lumels
	 * @return A LightPlane for a floor tile
	 */
	public static final LightPlane createFromFloorTile(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x, y, 0);
		Vector3 v1 = new Vector3(x + 1, y, 0);
		Vector3 v2 = new Vector3(x + 1, y + 1, 0);
		Vector3 v3 = new Vector3(x, y + 1, 0);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}

	/**
	 * Creates a 3D LightPlane for the side of a {@link render.core.Block Block}
	 * facing the positive x direction, at the location (x,y) with a resolution of
	 * <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 *
	 * @param x
	 *            x coordinate of the Block
	 * @param y
	 *            y coordinate of the Block
	 * @param squareResolution
	 *            resolution of both sides of the LightPlane, in lumels
	 * @return A LightPlane for a Block side facing positive x
	 */
	public static final LightPlane createFromPosXSide(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x + 1, y, 0);
		Vector3 v1 = new Vector3(x + 1, y + 1, 0);
		Vector3 v2 = new Vector3(x + 1, y + 1, 1);
		Vector3 v3 = new Vector3(x + 1, y, 1);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}

	/**
	 * Creates a 3D LightPlane for the side of a {@link render.core.Block Block}
	 * facing the negative x direction, at the location (x,y) with a resolution of
	 * <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 *
	 * @param x
	 *            x coordinate of the Block
	 * @param y
	 *            y coordinate of the Block
	 * @param squareResolution
	 *            resolution of both sides of the LightPlane, in lumels
	 * @return A LightPlane for a Block side facing negative x
	 */
	public static final LightPlane createFromNegXSide(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x, y, 0);
		Vector3 v1 = new Vector3(x, y + 1, 0);
		Vector3 v2 = new Vector3(x, y + 1, 1);
		Vector3 v3 = new Vector3(x, y, 1);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}

	/**
	 * Creates a 3D LightPlane for the side of a {@link render.core.Block Block}
	 * facing the negative y direction, at the location (x,y) with a resolution of
	 * <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 *
	 * @param x
	 *            x coordinate of the Block
	 * @param y
	 *            y coordinate of the Block
	 * @param squareResolution
	 *            resolution of both sides of the LightPlane, in lumels
	 * @return A LightPlane for a Block side facing negative y
	 */
	public static final LightPlane createFromNegYSide(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x, y, 0);
		Vector3 v1 = new Vector3(x + 1, y, 0);
		Vector3 v2 = new Vector3(x + 1, y, 1);
		Vector3 v3 = new Vector3(x, y, 1);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}
	
	/**
	 * Creates a 3D LightPlane for the side of a {@link render.core.Block Block}
	 * facing the positive y direction, at the location (x,y) with a resolution of
	 * <code>squareResolution</code> by <code>squareResolution</code> lumels.
	 *
	 * @param x
	 *            x coordinate of the Block
	 * @param y
	 *            y coordinate of the Block
	 * @param squareResolution
	 *            resolution of both sides of the LightPlane, in lumels
	 * @return A LightPlane for a Block side facing positive y
	 */
	public static final LightPlane createFromPosYSide(int x, int y, int squareResolution) {
		Vector3 v0 = new Vector3(x, y + 1, 0);
		Vector3 v1 = new Vector3(x + 1, y + 1, 0);
		Vector3 v2 = new Vector3(x + 1, y + 1, 1);
		Vector3 v3 = new Vector3(x, y + 1, 1);

		return new LightPlane(v0, v1, v2, v3, squareResolution, squareResolution);
	}

	/**
	 * Creates a 3D LightPlane for a {@link render.core.Wall Wall}.
	 * <code>verticalLumels</code> defines the vertical component of the resolution
	 * in lumels; the horizontal component is calculated to ensure square lumels.
	 * <b>BEWARE: two sides of a Wall may need different LightPlanes!</b>
	 *
	 * @param wall
	 * @param verticalLumels
	 * @return
	 */
	public static final LightPlane createFromWall(Wall wall, int verticalLumels) {
		Vector3 v0 = new Vector3(wall.v0.x, wall.v0.y, 0);
		Vector3 v1 = new Vector3(wall.v1.y, wall.v1.y, 0);
		Vector3 v2 = new Vector3(wall.v1.x, wall.v1.y, 1);
		Vector3 v3 = new Vector3(wall.v0.x, wall.v0.y, 1);

		wall.updateLength();

		return new LightPlane(v0, v1, v2, v3, (int) (wall.length * verticalLumels), verticalLumels);
	}

	private void ensureCoplanarity() {
		if (t0.getNormal() != t1.getNormal()) {
			System.out.println("All points in a LightPlane must be coplanar!");
		}
	}

	public Triangle[] getTriangles() {
		return new Triangle[] {t0, t1};
	}
}
