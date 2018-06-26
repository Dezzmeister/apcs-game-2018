package render.light.cellmaps;

import render.core.Wall;
import render.light.LightPlane;

/**
 * Represents a Cell Map for a Block that has been customized.
 *
 * @author Joe Desmond
 */
public class CustomBlockCellMap extends CellMap {
	private Wall[] walls;
	
	public CustomBlockCellMap(int x, int y, int resolution, Wall ... _walls) {
		super(2 + (2 * _walls.length));
		walls = _walls;
		lightplanes[0] = LightPlane.createFromFloorTile(x, y, resolution);
		lightplanes[1] = LightPlane.createFromCeilingTile(x, y, resolution);
		
		for (int i = 0; i < walls.length; i++) {
			lightplanes[i+2] = LightPlane.createFromWall(walls[i], resolution);
		}
	}
	
	public LightPlane getFloorLightPlane() {
		return lightplanes[0];
	}
	
	public LightPlane getCeilingLightPlane() {
		return lightplanes[1];
	}
	
	public LightPlane getLightPlane(Wall key) {
		for (int i = 0; i < walls.length; i++) {
			if (walls[i] == key) {
				return lightplanes[i+2];
			}
		}
		
		return null;
	}
}
