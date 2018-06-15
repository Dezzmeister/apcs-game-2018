package render.core;

import image.SquareTexture;
import image.Texture;

/**
 * Represents a map/level containing Sprites and Blocks to be rendered.
 *
 * @author Joe Desmond
 */
public class WorldMap {
	public static SquareTexture DEFAULT_FLOOR = new SquareTexture("assets/textures/floor32.png",32);
	public static SquareTexture DEFAULT_CEILING = new SquareTexture("assets/textures/ceil32.png",32);
	
	private Block[][] blocks;
	private Texture[][] floorMap;
	private Texture[][] ceilMap;
	
	public WorldMap(Block[][] _blocks) {
		blocks = _blocks;
		floorMap = new Texture[blocks.length][blocks[0].length];
		ceilMap = new Texture[blocks.length][blocks[0].length];
	}
	
	public WorldMap(Block[][] _blocks, Texture[][] _floorMap, Texture[][] _ceilMap) {
		blocks = _blocks;
		floorMap = _floorMap;
		ceilMap = _ceilMap;
	}
	
	public Block getBlockAt(int x, int y) {
		return blocks[y][x];
	}
	
	public Texture getFloorAt(int x, int y) {
		return floorMap[x][y];
	}
	
	public Texture getCeilingAt(int x, int y) {
		return ceilMap[x][y];
	}
}
