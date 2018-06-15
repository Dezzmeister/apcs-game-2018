package render.core;

import image.GeneralTexture;
import image.Texture;
import render.math.Vector2;

/**
 * This represents a Wall as opposed to a Block in the game. In the game, there can be either a 4-sided block or a block that is actually a sector
 * and has custom Walls defined in it. Each Wall has a texture which can be tiled to fit the Wall a certain way.
 * Walls are represented not as 3D surfaces, but as 2D lines because this raycaster does everything in 2D.
 *
 * @author Joe Desmond
 */
public class Wall {
	public static final GeneralTexture DEFAULT_TEXTURE = new GeneralTexture("assets/raycast/textures/darkbricks.png",512,512);
	
	/**
	 * One endpoint of this Wall
	 */
	public Vector2 v0;
	/**
	 * One endpoint of this Wall
	 */
	public Vector2 v1;
	public float length;
	public GeneralTexture texture = DEFAULT_TEXTURE;
	public float xTiles = 1;
	public float yTiles = 1;
	
	public Wall() {
		
	}
	
	public Wall(float x1, float y1, float x2, float y2, GeneralTexture _texture) {
		v0 = new Vector2(x1,y1);
		v1 = new Vector2(x2,y2);
		texture = _texture;
		updateLength();
	}
	
	public Wall(float x1, float y1, float x2, float y2) {
		v0 = new Vector2(x1,y1);
		v1 = new Vector2(x2,y2);
		updateLength();
	}
	
	public Wall(Vector2 _v0, Vector2 _v1) {
		v0 = _v0;
		v1 = _v1;
		updateLength();
	}
	
	public Wall setTexture(GeneralTexture _texture) {
		texture = _texture;
		return this;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public void updateLength() {
		length = Vector2.distance(v0, v1);
	}
	
	/**
	 * Allows a texture to be repeated multiple times (tiled) along a wall. By default, 
	 * xTiles is 1 and yTiles is 1, so the texture is stretched to fit the wall even
	 * if it may look ridiculous in doing so. Use this method to fix that: For example,
	 * if <code>tile(2,1)</code> is called, then this Wall's texture will appear twice on the Wall;
	 * both textures will be side-by-side. Likewise, if <code>tile(3,2)</code> is called, the texture will appear 6 times on the wall,
	 * arranged in a grid with a height of 2 and a width of 3. Each texture will be distorted to fit in a space 1/3 of the wall's width by
	 * 1/2 of its height.
	 * 
	 * @param _xTiles how many times to repeat the x texture
	 * @param _yTiles how many times to repeat the y texture
	 * @return this Wall
	 */
	public Wall tile(float _xTiles, float _yTiles) {
		xTiles = _xTiles;
		yTiles = _yTiles;
		return this;
	}
	
	/**
	 * Calls <code>updateLength()</code> before returning the updated length.
	 * As opposed to dereferencing <code>length</code>, this returns an updated
	 * length.
	 * 
	 * @return length of this Wall, after calling <code>updateLength()</code>
	 */
	public float length() {
		updateLength();
		return length;
	}
	
	/**
	 * Takes a Vector that is assumed to lie on this Wall and returns the normalized distance from
	 * the first endpoint of this wall to this Vector. For the purpose of speed, <code>getNorm()</code> does not perform
	 * any checks to ensure that the supplied Vector lies on this Wall. For this reason, it is possible to receive
	 * values greater than 1 if the supplied Vector is not on this Wall.
	 * 
	 * @param v Vector, should be known to lie on Wall
	 * @return normalized distance from first endpoint to v
	 */
	public float getNorm(Vector2 v) {
		return Vector2.distance(v, v0)/length;
	}
}
