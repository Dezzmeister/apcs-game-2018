package render.light.cellmaps;

import render.light.LightPlane;

/**
 * Represents a collection of four LightPlanes, each corresponding to one side
 * of a Block.
 *
 * @author Joe Desmond
 */
public class BlockCellMap extends CellMap {

	public BlockCellMap(LightPlane posx, LightPlane negx, LightPlane posy, LightPlane negy) {
		super(posx, negx, posy, negy);
	}

	/**
	 * Creates a BlockCellMap from a position and a resolution (in lumels).
	 *
	 * @param x
	 *            x position of the Block in the world
	 * @param y
	 *            y position of the Block in the world
	 * @param squareResolution
	 *            resolution, in lumels, of one side of each LightPlane
	 */
	public BlockCellMap(int x, int y, int squareResolution) {
		super(4);

		lightplanes[0] = LightPlane.createFromPosXSide(x, y, squareResolution);
		lightplanes[1] = LightPlane.createFromNegXSide(x, y, squareResolution);
		lightplanes[2] = LightPlane.createFromPosYSide(x, y, squareResolution);
		lightplanes[3] = LightPlane.createFromNegYSide(x, y, squareResolution);
	}

	/**
	 * Returns the LightPlane corresponding to the side of a Block facing positive
	 * x.
	 *
	 * @return LightPlane of the positive x face
	 */
	public LightPlane getPositiveXLightPlane() {
		return lightplanes[0];
	}

	/**
	 * Returns the LightPlane corresponding to the side of a Block facing negative
	 * x.
	 *
	 * @return LightPlane of the negative x face
	 */
	public LightPlane getNegativeXLightPlane() {
		return lightplanes[1];
	}

	/**
	 * Returns the LightPlane corresponding to the side of a Block facing positive
	 * y.
	 *
	 * @return LightPlane of the positive y face
	 */
	public LightPlane getPositiveYLightPlane() {
		return lightplanes[2];
	}

	/**
	 * Returns the LightPlane corresponding to the side of a Block facing negative
	 * y.
	 *
	 * @return LightPlane of the negative y face
	 */
	public LightPlane getNegativeYLightPlane() {
		return lightplanes[3];
	}
}
