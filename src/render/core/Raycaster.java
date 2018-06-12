package render.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import render.math.Vector2;

public class Raycaster {
	private int WIDTH, HEIGHT, HALF_HEIGHT;
	private double[] zbuf;
	//private Sprite[] sprites;
	private Vector2 pos;
	private Vector2 dir;
	private Vector2 plane;
	private BufferedImage img;
	private Graphics2D g2;
	private Camera camera;
	private WorldMap world;
	
	public static final float FULL_FOG_DISTANCE = 5f;
	public static final int SHADE_THRESHOLD = 100;
	
	private int rendererCount = 4;
	private ThreadRenderer[] renderers;
	private ThreadPoolExecutor executor;
	private LatchRef latch = new LatchRef();
	private Wall perpWall = new Wall();
	private float[] fisheyeLUT;
	private double[] wallDistLUT;
	
	public Raycaster(Camera _camera, WorldMap _worldMap, int threads) {
		camera = _camera;
		world = _worldMap;
		rendererCount = threads;
	}
	
	private class ThreadRenderer implements Runnable {
		int startX;
		int endX;
		int id;
		
		ThreadRenderer(int _startX, int _endX, int _id) {
			startX = _startX;
			endX = _endX;
			id = _id;
		}
		
		@Override
		public void run() {
			render();
			latch.latch.countDown();
		}
		
		private void render() {
			
		}
	}
	
	private class LatchRef {
		CountDownLatch latch = new CountDownLatch(0);
		
		public void update(int value) {
			latch = new CountDownLatch(value);
		}
	}
}
