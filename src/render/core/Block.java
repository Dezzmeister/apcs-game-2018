package render.core;

import image.SquareTexture;

/**
 * Represents a 1x1 space in a world map. Can be either a full block or a custom "sector" containing custom Walls.
 *
 * @author Joe Desmond
 */
public class Block {
	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/default32.png",32);
	public static final Block SPACE = new Block("space").fakeBlock();
	
	public Wall[] walls = null;
	private boolean solid = true;
	public String name;
	public SquareTexture frontTexture = DEFAULT_TEXTURE;
	public SquareTexture sideTexture = DEFAULT_TEXTURE;
	
	public Block(String _name) {
		name = _name;
	}
	
	/**
	 * Allows a Block to be rendered as a group of custom Walls defined relative to block space
	 * instead of a default block. If solid, collision will still occur with block boundaries.
	 * 
	 * @param _walls varArgs parameter: can be a Wall[] or any number of Walls
	 * @return this Block
	 */
	public Block customize(Wall ... _walls) {
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
	 * Returns true if this block is defined as a collection of Walls instead of a full Block.
	 * 
	 * @return true if this Block is Walls
	 */
	public boolean isCustom() {
		return walls != null;
	}
	
	public Block applyFrontTexture(SquareTexture front) {
		frontTexture = front;
		return this;
	}
	
	public Block applySideTexture(SquareTexture side) {
		sideTexture = side;
		return this;
	}
	
	/**
	 * Applies one texture to all sides of the Block.
	 * 
	 * @param texture texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applyTexture(SquareTexture texture) {
		frontTexture = texture;
		sideTexture = texture;
		return this;
	}
}
