package render.core;

import image.SquareTexture;

/**
 * Represents a map/level containing Sprites and Blocks to be rendered.
 *
 * @author Joe Desmond
 */
public class WorldMap {
	public static SquareTexture DEFAULT_FLOOR = new SquareTexture("assets/textures/floor32.png",32);
	public static SquareTexture DEFAULT_CEILING = new SquareTexture("assets/textures/ceil32.png",32);
	
	private Block[][] blocks;
	private SquareTexture[][] floorMap;
	private SquareTexture[][] ceilMap;
	
	public WorldMap(Block[][] _blocks) {
		blocks = _blocks;
		floorMap = new SquareTexture[blocks.length][blocks[0].length];
		ceilMap = new SquareTexture[blocks.length][blocks[0].length];
	}
	
	public WorldMap(Block[][] _blocks, SquareTexture[][] _floorMap, SquareTexture[][] _ceilMap) {
		blocks = _blocks;
		floorMap = _floorMap;
		ceilMap = _ceilMap;
	}
	
	/**
	 * Replaces all border Blocks with <code>block</code>.
	 * @param block Block to set border to
	 */
	public WorldMap setBorder(Block block) {
		for (int col = 0; col < blocks[0].length; col++) {
			blocks[0][col] = block;
			blocks[blocks.length-1][col] = block;
		}
		
		for (int row = 0; row < blocks.length; row++) {
			blocks[row][0] = block;
			blocks[row][blocks[0].length-1] = block;
		}
		
		return this;
	}
	
	public Block getBlockAt(int x, int y) {
		return blocks[y][x];
	}
	
	public SquareTexture getFloorAt(int x, int y) {
		return floorMap[x][y];
	}
	
	public SquareTexture getCeilingAt(int x, int y) {
		return ceilMap[x][y];
	}
}
