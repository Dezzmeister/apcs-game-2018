package render.light.cellmaps;

import render.light.LightPlane;

/**
 * 
 *
 * @author Joe Desmond
 */
public class FakeBlockCellMap extends CellMap {
	
	public FakeBlockCellMap(int x, int y, int squareResolution) {
		super(6);
		lightplanes[0] = LightPlane.createFromFloorTile(x, y, squareResolution);
		lightplanes[1] = LightPlane.createFromCeilingTile(x, y, squareResolution);
		lightplanes[0] = LightPlane.createFromPosXSide(x, y, squareResolution);
		lightplanes[1] = LightPlane.createFromNegXSide(x, y, squareResolution);
		lightplanes[2] = LightPlane.createFromPosYSide(x, y, squareResolution);
		lightplanes[3] = LightPlane.createFromNegYSide(x, y, squareResolution);
	}
	
	public LightPlane getPositiveXLightPlane() {
		return lightplanes[0];
	}
	
	public LightPlane getNegativeXLightPlane() {
		return lightplanes[1];
	}
	
	public LightPlane getPositiveYLightPlane() {
		return lightplanes[2];
	}
	
	public LightPlane getNegativeYLightPlane() {
		return lightplanes[3];
	}
}
