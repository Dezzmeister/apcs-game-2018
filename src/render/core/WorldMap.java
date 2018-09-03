package render.core;

import java.util.ArrayList;
import java.util.List;

import image.Entity;
import image.SquareTexture;
import render.light.Light;
import render.light.cellmaps.BlockCellMap;
import render.light.cellmaps.CellMap;
import render.light.cellmaps.CustomBlockCellMap;
import render.light.cellmaps.EmptyCellMap;
import render.light.cellmaps.FakeBlockCellMap;

/**
 * Represents a map/level containing Sprites and Blocks to be rendered.
 *
 * @author Joe Desmond
 */
public class WorldMap {
	public static final int LIGHT_RESOLUTION = 100;

	public static final SquareTexture DEFAULT_FLOOR = new SquareTexture("assets/textures/floor32.png", 32);
	public static final SquareTexture DEFAULT_CEILING = new SquareTexture("assets/textures/ceil32.png", 32);
	
	private Block[][] blocks;
	private SquareTexture[][] floorMap;
	private SquareTexture[][] ceilMap;
	private CellMap[][] lightmaps;
	public Light[] lights;
	public List<Entity> entities = new ArrayList<Entity>();

	// private Speaker[] speakers;
	
	public WorldMap(Block[][] _blocks) {
		blocks = _blocks;
		floorMap = new SquareTexture[blocks.length][blocks[0].length];
		ceilMap = new SquareTexture[blocks.length][blocks[0].length];
		lightmaps = new CellMap[blocks.length][blocks[0].length];
		
		initDefaultFloorMap();
		initDefaultCeilingMap();
		//initEmptyLightMaps();
	}
	
	public WorldMap(Block[][] _blocks, SquareTexture[][] _floorMap, SquareTexture[][] _ceilMap) {
		blocks = _blocks;
		floorMap = _floorMap;
		ceilMap = _ceilMap;
		//initEmptyLightMaps();
	}
	
	public WorldMap setFloorMap(SquareTexture[][] _floorMap) {
		floorMap = _floorMap;
		return this;
	}
	
	public WorldMap setCeilingMap(SquareTexture[][] _ceilMap) {
		ceilMap = _ceilMap;
		return this;
	}
	
	public WorldMap addEntity(Entity entity) {
		entities.add(entity);
		return this;
	}
	
	public WorldMap addEntities(Entity ... _entities) {
		for (Entity e : _entities) {
			entities.add(e);
		}
		
		return this;
	}
	
	/*
	 * public WorldMap setSpeakers(Speaker ... _speakers) { speakers = _speakers;
	 * return this; }
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
	
	@SuppressWarnings("unused")
	private void initEmptyLightMaps() {
		for (int row = 0; row < lightmaps.length; row++) {
			for (int col = 0; col < lightmaps[row].length; col++) {
				Block block = blocks[row][col];
				
				if (!block.isSolid() && block != Block.SPACE) {
					lightmaps[row][col] = new FakeBlockCellMap(col,row,LIGHT_RESOLUTION);
				} else if (block.isCustom()) {
					lightmaps[row][col] = new CustomBlockCellMap(col,row,LIGHT_RESOLUTION);
				} else if (block == Block.SPACE) {
					lightmaps[row][col] = new EmptyCellMap(col,row,LIGHT_RESOLUTION);
				} else {
					lightmaps[row][col] = new BlockCellMap(col,row,LIGHT_RESOLUTION);
				}
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
