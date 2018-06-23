package render.core;

import image.GeneralTexture;
import image.SquareTexture;

/**
 * Represents a 1x1 space in a world map. Can be either a full block or a custom
 * "sector" containing custom Walls.
 *
 * @author Joe Desmond
 */
public class Block {

	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/default32.png", 32);
	public static final Block SPACE = new Block("space").fakeBlock();
	
	/**
	 * If this Block is custom, this will contain this Block's custom Walls.
	 */
	public Wall[] walls = null;
	private boolean solid = true;
	public String name;
	public SquareTexture frontTexture = DEFAULT_TEXTURE;
	public SquareTexture sideTexture = DEFAULT_TEXTURE;
	public float frontXTiles = 1;
	public float frontYTiles = 1;
	public float sideXTiles = 1;
	public float sideYTiles = 1;
	
	public Block(String _name) {
		name = _name;
	}
	
	/**
	 * Allows a Block to be rendered as a group of custom Walls defined relative to
	 * block space instead of a default block. If solid, collision will still occur
	 * with block boundaries.
	 *
	 * @param _walls
	 *            varArgs parameter: can be a Wall[] or any number of Walls
	 * @return this Block
	 */
	public Block customize(Wall... _walls) {
		walls = _walls;
		return this;
	}
	
	/**
	 * Makes this block non-solid.
	 */
	public Block fakeBlock() {
		solid = false;
		return this;
	}
	
	public void solidify() {
		solid = true;
	}
	
	public boolean isSolid() {
		return solid;
	}
	
	/**
	 * Returns true if this block is defined as a collection of Walls instead of a
	 * full Block.
	 *
	 * @return true if this Block is Walls
	 */
	public boolean isCustom() {
		return walls != null;
	}
	
	public Block tileFront(float _xTiles, float _yTiles) {
		frontXTiles = _xTiles;
		frontYTiles = _yTiles;
		
		return this;
	}
	
	public Block tileSide(float _xTiles, float _yTiles) {
		sideXTiles = _xTiles;
		sideYTiles = _yTiles;
		
		return this;
	}
	
	/**
	 * Apply this texture to the front and back (x-facing sides) of the block.
	 *
	 * @param front
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applyFrontTexture(SquareTexture front) {
		frontTexture = front;
		return this;
	}
	
	/**
	 * Apply this texture to the y-facing sides of the block.
	 *
	 * @param side
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applySideTexture(SquareTexture side) {
		sideTexture = side;
		return this;
	}
	
	/**
	 * Applies one texture to all sides of the Block.
	 *
	 * @param texture
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applyTexture(SquareTexture texture) {
		frontTexture = texture;
		sideTexture = texture;
		return this;
	}
	
	/**
	 * Contains Blocks defined as cubicle walls, for use in our office-themed maps.
	 * The CUBICLE and SIDE textures are not final.
	 *
	 * @author Joe Desmond
	 */
	public static class CubicleWalls {

		public static final GeneralTexture CUBICLE = new GeneralTexture("assets/textures/joj32.png", 32, 32);
		public static final GeneralTexture SIDE = new GeneralTexture("assets/textures/side.png", 2, 20);
		
		private static float thickness = 0.1f;
		/**
		 * A Cubicle Wall aligned on the Y axis.
		 */
		public static final Block CUBICLE_Y = new Block("cubicle y").customize(
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f - thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f + thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 0.0f).setTexture(SIDE),
				new Wall(0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(SIDE));
		
		/**
		 * A Cubicle wall aligned on the X axis.
		 */
		public static final Block CUBICLE_X = new Block("cubicle x").customize(
				new Wall(0.0f, 0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(1.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(0.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f - thickness / 2.0f).setTexture(CUBICLE),
				new Wall(0.0f, 0.5f + thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(CUBICLE));
	}
}
