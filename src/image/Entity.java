package image;

import render.core.Camera;
import render.math.Vector2;

public abstract class Entity implements Comparable<Entity> {

	/**
	 * (242,35,248)
	 */
	public static int defaultAlpha = 0xFFF223F8;
	/**
	 * The alpha color is used during rendering to determine which pixels of the
	 * image should not be visible (transparent). Because the BufferedImage used
	 * during rendering does not support the alpha channel, a color must be defined
	 * here that represents a 100% transparent pixel in the image.
	 */
	public int alpha = defaultAlpha;

	public Vector2 pos;
	private boolean invisible;
	public float distance = 0;
	public int order = 0;
	protected Camera player;

	public int xDrawBegin = 0;
	public int xDrawEnd;
	public int yDrawBegin = 0;
	public int yDrawEnd;

	protected Entity(Vector2 initialPosition) {
		pos = initialPosition;
	}

	protected Entity() {

	}

	/**
	 * Manually sets the rectangular portion of the sprite that will be tested
	 * during rendering. By default, this rectangle encompasses the entire sprite.
	 * However, it can be resized to speed up the rendering process.
	 * <p>
	 * For example, if the entire upper portion of the sprite is transparent, the
	 * drawable bounds can be resized to avoid testing that portion of the image at
	 * all.
	 *
	 * @param _xBegin
	 *            leftmost x bound
	 * @param _yBegin
	 *            upper y bound
	 * @param _xEnd
	 *            rightmost x bound
	 * @param _yEnd
	 *            lower y bound
	 * @return this Sprite
	 */
	public Entity setDrawableBounds(int _xBegin, int _yBegin, int _xEnd, int _yEnd) {
		xDrawBegin = _xBegin;
		yDrawBegin = _yBegin;
		xDrawEnd = _xEnd;
		yDrawEnd = _yEnd;
		return this;
	}

	public void setCamera(Camera _camera) {
		player = _camera;
	}

	public int width() {
		return xDrawEnd - xDrawBegin;
	}

	public int height() {
		return yDrawEnd - yDrawBegin;
	}

	public void setAlphaColor(int _alpha) {
		alpha = _alpha;
	}

	public void moveTo(Vector2 newPosition) {
		pos = newPosition;
	}

	/**
	 * Adds the Vector formed by (x,y) to this Entity's position.
	 *
	 * @param x
	 *            distance to move in x direction
	 * @param y
	 *            distance to move in y direction
	 */
	public void move(float x, float y) {
		pos = pos.add(new Vector2(x, y));
	}
	
	/**
	 * Returns the active {@link SquareTexture} for this Entity. For items, this
	 * texture is constant; for actors, this texture should change to show a
	 * different view of an Entity.
	 *
	 * @return active texture for this Entity
	 */
	public abstract SquareTexture getActiveTexture();

	/**
	 * Returns the position of this Entity in the world.
	 *
	 * @return location of this Entity
	 */
	public Vector2 getPosition() {
		return pos;
	}

	/**
	 * Returns the Camera containing player data used by this Entity.
	 *
	 * @return
	 */
	public Camera getPlayerCamera() {
		return player;
	}

	public void hide() {
		invisible = true;
	}

	public void show() {
		invisible = false;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public void updateDistance() {
		distance = Vector2.distance(player.pos, pos);
	}

	@Override
	public int compareTo(Entity arg0) {
		if (distance > arg0.distance) {
			return -1;
		} else {
			return 1;
		}
	}

	public static void setDefaultAlpha(int _default) {
		defaultAlpha = _default;
	}
}
