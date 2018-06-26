package render.light;

/**
 * Contains lightmap information for an empty {@link render.core.Block Block}. Has 2 {@link LightPlane LightPlanes}, one for the floor and one for the ceiling.
 *
 * @author Joe Desmond
 */
public class EmptyCellMap extends CellMap {
	
	/**
	 * Creates two empty LightPlanes from an x and y value of an empty cell and a resolution for each side of each plane.
	 * 
	 * @param x x position of the empty cell
	 * @param y y position of the empty cell
	 * @param squareResolution resolution of each side of each LightPlane, in lumels
	 */
	public EmptyCellMap(int x, int y, int squareResolution) {
		lightplanes[0] = LightPlane.createFromFloorTile(x, y, squareResolution);
		lightplanes[1] = LightPlane.createFromCeilingTile(x, y, squareResolution);
	}
	
	/**
	 * Returns the LightPlane corresponding to this EmptyCellMap's floor.
	 * 
	 * @return floor LightPlane
	 */
	public LightPlane getFloorLightPlane() {
		return lightplanes[0];
	}
	
	/**
	 * Returns the LightPlane corresponding to this EmptyCellMap's ceiling.
	 * 
	 * @return ceiling LightPlane
	 */
	public LightPlane getCeilingLightPlane() {
		return lightplanes[1];
	}
}
