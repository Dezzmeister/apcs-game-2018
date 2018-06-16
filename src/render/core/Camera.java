package render.core;

import java.util.HashMap;
import java.util.Map;

import render.math.RenderUtils;
import render.math.Vector2;

public class Camera {
	public Vector2 pos;
	public Vector2 dir;
	public Vector2 plane;
	private Vector2 sidedir;
	
	private float moveSpeed = 0.055f;
	private float rotSpeed = 0.005f;
	
	public float fogDistance = 10;
	
	public int yOffset = 0;
	private int upDownLimit;
	
	private static final float HALF_PI = (float)Math.PI/2.0f;
	private static final float TWO_PI = (float)Math.PI*2.0f;
	/**
	 * I love lookup tables.
	 */
	private Map<Float,Float> preComputedSineLUT = new HashMap<Float,Float>();
	
	public Camera setPos(Vector2 _pos) {
		pos = _pos;
		return this;
	}
	
	public Camera setDir(Vector2 _dir) {
		dir = _dir;
		computeSideDir();
		return this;
	}
	
	public Camera setPlane(Vector2 _plane) {
		plane = _plane;
		return this;
	}
	
	/**
	 * Sets the maximum value (in pixels) for vertical mouselook (y-shearing).
	 * 
	 * @param limit maximum 
	 */
	public void setVerticalMouselookLimit(int limit) {
		upDownLimit = limit;
	}
	
	public double getRotationSpeed() {
		return rotSpeed;
	}
	
	public void setRotationSpeed(float speed) {
		rotSpeed = speed;
	}
	
	public double getMoveSpeed() {
		return moveSpeed;
	}
	
	public void setMoveSpeed(float speed) {
		moveSpeed = speed;
	}
	
	private static final float COS_T = (float) Math.cos(Math.PI/2.0);
	private static final float SIN_T = (float) Math.sin(Math.PI/2.0);
	
	private void computeSideDir() {		
		float x = (float) (dir.x*COS_T - dir.y*SIN_T);
		float y = (float) (dir.x*SIN_T + dir.y*COS_T);
		
		sidedir = new Vector2(x,y);
	}
	
	/**
	 * Used with LUT rotation methods. This method takes a potential rotation factor and computes
	 * sine, cosine, and negative sine. It puts these values in a lookup table to avoid computing them 
	 * in real time. The lookup table returns the sine of the key: to get cosine, use (HALF_PI+key); to get negative sine,
	 * use (TWO_PI-speed).
	 * 
	 * @param factor
	 */
	public void preComputeFactor(float factor) {
		float speed = factor * this.rotSpeed;
		preComputedSineLUT.put(speed, (float)Math.sin(speed));
		preComputedSineLUT.put(HALF_PI+speed, (float)Math.cos(speed));
		preComputedSineLUT.put(TWO_PI-speed, -(float)Math.sin(speed));
	}
	
	public void clearRotationLUT() {
		preComputedSineLUT.clear();
	}
	
	/**
	 * A faster implementation of <code>rotateLeft()</code> that uses a lookup table. Use in conjunction with
	 * <code>preComputeFactor()</code>.
	 * 
	 * @param factor multiplied by internal rotation speed and used to rotate
	 */
	public void rotateLeftLUT(float factor) {
		float speed = factor * this.rotSpeed;
		
		float oldDirX = dir.x;
		float cr = preComputedSineLUT.get(HALF_PI + speed);
		float sr = preComputedSineLUT.get(speed);
		dir.x = dir.x * cr - dir.y * sr;
	    dir.y = oldDirX * sr + dir.y * cr;
	    float oldPlaneX = plane.x;
	    plane.x = plane.x * cr - plane.y * sr;
	    plane.y = oldPlaneX * sr + plane.y * cr;
	}
	
	/**
	 * A faster implementation of <code>rotateRight()</code> that uses a lookup table. Use in conjunction with
	 * <code>preComputeFactor()</code>.
	 * 
	 * @param factor multiplied by internal rotation speed and used to multiply
	 */
	public void rotateRightLUT(float factor) {
		float speed = factor * this.rotSpeed;
		
		float oldDirX = dir.x;
		float cr = preComputedSineLUT.get(HALF_PI + speed);
		float sr = preComputedSineLUT.get(TWO_PI - speed);
	    dir.x = dir.x * cr - dir.y * sr;
	    dir.y = oldDirX * sr + dir.y * cr;
	    float oldPlaneX = plane.x;
	    plane.x = plane.x * cr - plane.y * sr;
	    plane.y = oldPlaneX * sr + plane.y * cr;
	}
	
	/**
	 * Rotates the camera left by a factor of the camera's internal rotation speed.
	 * 
	 * @param factor multiplied by the rotation speed, product used to rotate left
	 */
	public void rotateLeft(float factor) {
		float speed = factor * this.rotSpeed;
		
		float oldDirX = dir.x;
		float cr = (float) Math.cos(speed);
		float sr = (float) Math.sin(speed);
		dir.x = dir.x * cr - dir.y * sr;
	    dir.y = oldDirX * sr + dir.y * cr;
	    float oldPlaneX = plane.x;
	    plane.x = plane.x * cr - plane.y * sr;
	    plane.y = oldPlaneX * sr + plane.y * cr;
	}
	
	/**
	 * Rotates the camera right by a factor of the camera's internal rotation speed.
	 * 
	 * @param factor multiplied by the rotation speed, product used to rotate right
	 */
	public void rotateRight(float factor) {
		float speed = factor * this.rotSpeed;
		
		float oldDirX = dir.x;
		float cr = (float) Math.cos(-speed);
		float sr = (float) Math.sin(-speed);
	    dir.x = dir.x * cr - dir.y * sr;
	    dir.y = oldDirX * sr + dir.y * cr;
	    float oldPlaneX = plane.x;
	    plane.x = plane.x * cr - plane.y * sr;
	    plane.y = oldPlaneX * sr + plane.y * cr;
	}
	
	public void moveForward(WorldMap map, float factor) {
		float speed = factor * moveSpeed;
		
		float xTimesSpeed = dir.x * speed;
		float yTimesSpeed = dir.y * speed;
		
		if (!map.getBlockAt((int)(pos.x + xTimesSpeed), (int)pos.y).isSolid()) {
			pos.x += xTimesSpeed;
		}
		if (!map.getBlockAt((int)pos.x,(int)(pos.y + yTimesSpeed)).isSolid()) {
			pos.y += yTimesSpeed;
		}
	}
	
	public void moveBackward(WorldMap map, float factor) {
		float speed = factor * moveSpeed;
		
		float xTimesSpeed = dir.x * speed;
		float yTimesSpeed = dir.y * speed;
		
		if (!map.getBlockAt((int)(pos.x - xTimesSpeed),(int)pos.y).isSolid()) {
			pos.x -= xTimesSpeed;
		}
	    if (!map.getBlockAt((int)pos.x,(int)(pos.y - yTimesSpeed)).isSolid()) {
	    	pos.y -= yTimesSpeed;
	    }
	}
	
	public void moveLeft(WorldMap map, float factor) {
		computeSideDir();
		
		float speed = factor * moveSpeed;
		
		float xTimesSpeed = sidedir.x * speed;
		float yTimesSpeed = sidedir.y * speed;
		
		if (!map.getBlockAt((int)(pos.x + xTimesSpeed), (int)pos.y).isSolid()) {
			pos.x += xTimesSpeed;
		}
		if (!map.getBlockAt((int)pos.x,(int)(pos.y + yTimesSpeed)).isSolid()) {
			pos.y += yTimesSpeed;
		}
	}
	
	public void moveRight(WorldMap map, float factor) {
		computeSideDir();
		
		float speed = factor * moveSpeed;
		
		float xTimesSpeed = sidedir.x * speed;
		float yTimesSpeed = sidedir.y * speed;
		
		if (!map.getBlockAt((int)(pos.x - xTimesSpeed),(int)pos.y).isSolid()) {
			pos.x -= xTimesSpeed;
		}
	    if (!map.getBlockAt((int)pos.x,(int)(pos.y - yTimesSpeed)).isSolid()) {
	    	pos.y -= yTimesSpeed;
	    }
	}
	
	public void cheapRotateUp(float factor, int height) {
		float speed = factor * this.rotSpeed;
		
		yOffset += (int)(speed * height);
		yOffset = (int)RenderUtils.clamp(yOffset, -upDownLimit, upDownLimit);
	}
	
	public void cheapRotateDown(float factor, int height) {
		float speed = factor * this.rotSpeed;
		
		yOffset -= (int)(speed * height);
		yOffset = (int)RenderUtils.clamp(yOffset, -upDownLimit, upDownLimit);
	}
}
