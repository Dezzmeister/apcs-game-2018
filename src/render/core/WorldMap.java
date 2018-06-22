package render.core;

import image.SquareTexture;

/**
 * Represents a map/level containing Sprites and Blocks to be rendered.
 *
 * @author Joe Desmond
 */
public class WorldMap {
	
	public static SquareTexture DEFAULT_FLOOR = new SquareTexture("assets/textures/floor32.png", 32);
	public static SquareTexture DEFAULT_CEILING = new SquareTexture("assets/textures/ceil32.png", 32);

	private Block[][] blocks;
	private SquareTexture[][] floorMap;
	private SquareTexture[][] ceilMap;
	
	//private Speaker[] speakers;

	public WorldMap(Block[][] _blocks) {
		blocks = _blocks;
		floorMap = new SquareTexture[blocks.length][blocks[0].length];
		ceilMap = new SquareTexture[blocks.length][blocks[0].length];

		initDefaultFloorMap();
		initDefaultCeilingMap();
	}

	public WorldMap(Block[][] _blocks, SquareTexture[][] _floorMap, SquareTexture[][] _ceilMap) {
		blocks = _blocks;
		floorMap = _floorMap;
		ceilMap = _ceilMap;
	}

	public WorldMap setFloorMap(SquareTexture[][] _floorMap) {
		floorMap = _floorMap;
		return this;
	}

	public WorldMap setCeilingMap(SquareTexture[][] _ceilMap) {
		ceilMap = _ceilMap;
		return this;
	}
	/*
	public WorldMap setSpeakers(Speaker ... _speakers) {
		speakers = _speakers;
		return this;
	}
	*/
	/**
	 * Replaces all border Blocks with <code>block</code>.
	 * 
	 * @param block
	 *            Block to set border to
	 */
	public WorldMap setBorder(Block block) {
		for (int col = 0; col < blocks[0].length; col++) {
			blocks[0][col] = block;
			blocks[blocks.length - 1][col] = block;
		}

		for (int row = 0; row < blocks.length; row++) {
			blocks[row][0] = block;
			blocks[row][blocks[0].length - 1] = block;
		}

		return this;
	}

	private void initDefaultFloorMap() {
		for (int row = 0; row < floorMap.length; row++) {
			for (int col = 0; col < floorMap[row].length; col++) {
				floorMap[row][col] = DEFAULT_FLOOR;
			}
		}
	}

	private void initDefaultCeilingMap() {
		for (int row = 0; row < ceilMap.length; row++) {
			for (int col = 0; col < ceilMap[row].length; col++) {
				ceilMap[row][col] = DEFAULT_CEILING;
			}
		}
	}

	public Block getBlockAt(int x, int y) {
		return blocks[y][x];
	}

	public Block setBlockAt(int x, int y, Block block) {
		Block old = blocks[y][x];
		blocks[y][x] = block;
		return old;
	}

	public SquareTexture getFloorAt(int x, int y) {
		return floorMap[y][x];
	}

	public SquareTexture getCeilingAt(int x, int y) {
		return ceilMap[y][x];
	}
}
