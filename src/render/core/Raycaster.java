package render.core;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import render.math.Vector2;

/**
 * Uses multiple threads to render an image.
 *
 * @author Joe Desmond
 */
public class Raycaster {
	private int WIDTH, HEIGHT, HALF_HEIGHT;
	private int actualWidth, actualHeight;
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
	private LatchRef latchref;
	private Wall perpWall = new Wall();
	private float[] fisheyeLUT;
	private double[] wallDistLUT;
	
	public Raycaster(int resWidth, int resHeight, int renderWidth, int renderHeight, Camera _camera, WorldMap _worldMap, int threads) {
		camera = _camera;
		world = _worldMap;
		rendererCount = threads;
		WIDTH = renderWidth;
		HEIGHT = renderHeight;
		actualWidth = resWidth;
		actualHeight = resHeight;
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
			latchref.latch.countDown();
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
	
	private void createThreadPoolRenderers() {
		if (rendererCount > WIDTH) {
			String error = "It is impossible to have more thread renderers than stripes on the screen!";
			System.err.println(error);
			System.exit(0);
		}
		
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(rendererCount);
		System.out.println(executor.getCorePoolSize() + " threads created for thread renderers.");
		
		//In case the executor could not make all the threads we wanted
		rendererCount = executor.getCorePoolSize();
		
		renderers = new ThreadRenderer[rendererCount];
		latchref = new LatchRef();
		
		int interval = (WIDTH - (WIDTH % rendererCount))/rendererCount;
		
		int step = 0;
		
		while (step+interval < WIDTH) {
			int i = step/interval;
			
			renderers[i] = new ThreadRenderer(step,step+interval,i);
			step += interval;
		}

		renderers[renderers.length-1] = new ThreadRenderer(step,WIDTH,rendererCount-1);
	}
	
	public void render(Graphics g) {
		
	}
}
