package render.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import image.GeneralTexture;
import image.SquareTexture;
import main.Game;
import render.light.Side;
import render.math.RenderUtils;
import render.math.Vector2;

/**
 * Uses multiple threads to render a map. Uses a Camera holding player data such
 * as position, direction, and FoV.
 *
 * @author Joe Desmond
 */
public class Raycaster extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7071993726323961319L;
	protected int WIDTH, HEIGHT, HALF_HEIGHT;
	protected int FINAL_WIDTH, FINAL_HEIGHT;
	protected double[] zbuf;
	
	// private Sprite[] sprites;
	protected Vector2 pos;
	protected Vector2 dir;
	protected Vector2 plane;
	protected BufferedImage img;
	protected Graphics2D g2;
	protected Graphics g;
	public Camera camera;
	public WorldMap world;
	
	public static final float FULL_FOG_DISTANCE = 5f;
	public static final int SHADE_THRESHOLD = 100;
	
	protected int rendererCount;
	protected ThreadRenderer[] renderers;
	protected ThreadPoolExecutor executor;
	protected LatchRef latchref;
	protected double[] wallDistLUT;
	protected AtomicBoolean enabled = new AtomicBoolean(true);
	protected Game parentGame;
	protected int upDownEnabled = 0;
	public boolean finished = true;
	
	/**
	 * Creates a <code>Raycaster</code> object that can render a WorldMap. The
	 * <code>_parentGame</code> object should be whatever Game will have control of
	 * this Raycaster.
	 * <p>
	 * Raycaster needs a Camera(<code>_camera</code>) and a
	 * WorldMap(<code>_worldMap</code>) to render: the Camera holds important player
	 * data, and the WorldMap holds data about the appearance of the world.
	 * <p>
	 * Raycaster also needs to know the resolution of the final image
	 * (<code>resWidth</code>,<code>resHeight</code>): i. e., the width and height
	 * of the window in which this game will be run. This resolution <b>DOES NOT</b>
	 * affect rendering speed.
	 * <p>
	 * Raycaster needs to know the resolution of the image it will render
	 * to(<code>renderWidth</code>,<code>renderHeight</code>). This resolution
	 * <b>DOES</b> affect rendering speed; larger images will take more time to
	 * render. This image will be scaled to fit the resolution defined by
	 * <code>resWidth</code> and <code>resHeight</code>, so this resolution should
	 * be <b>SMALLER</b> than the final resolution.
	 * <p>
	 * Raycaster uses multiple threads to render a scene, so it needs to know how
	 * many threads it should use. Ideally, this number should correspond to the
	 * number of logical cores in your CPU.
	 *
	 * @param _parentGame
	 *            Game object that uses this Raycaster
	 * @param _camera
	 *            Camera used by this Raycaster to determine player position, FoV,
	 *            and direction
	 * @param _worldMap
	 *            Initial map to be rendered by this Raycaster
	 * @param resWidth
	 *            Width of the final image
	 * @param resHeight
	 *            Height of the final image
	 * @param renderWidth
	 *            Width of the rendered image
	 * @param renderHeight
	 *            Height of the rendered image
	 * @param threads
	 *            Threads to be used when rendering
	 */
	public Raycaster(Game _parentGame, Camera _camera, WorldMap _worldMap, int resWidth, int resHeight, int renderWidth,
			int renderHeight, int threads) {
		parentGame = _parentGame;
		camera = _camera;
		world = _worldMap;
		rendererCount = threads;
		WIDTH = renderWidth;
		HEIGHT = renderHeight;
		FINAL_WIDTH = resWidth;
		FINAL_HEIGHT = resHeight;
		
		init();
	}
	
	private void init() {
		zbuf = new double[WIDTH];
		HALF_HEIGHT = HEIGHT / 2;
		camera.setVerticalMouselookLimit(HEIGHT / 8);
		
		resetZBuffer();
		populateWallDistLUT();
		createThreadPoolRenderers();
	}
	
	private void getCameraVectors() {
		pos = camera.pos;
		dir = camera.dir;
		plane = camera.plane;
	}
	
	private void resetImage() {
		g2 = (Graphics2D) g;
		g2.setBackground(Color.BLACK);
		g2.clearRect(0, 0, FINAL_WIDTH, FINAL_HEIGHT);
		
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	}
	
	// TODO: Remove this when we have a proper HUD
	private void drawDebugInfo() {
		g.setColor(Color.GREEN);
		g.drawString("x: " + pos.x, 5, 20);
		g.drawString("y: " + pos.y, 5, 35);
	}
	
	private void drawCrosshair() {
		g.setColor(Color.GREEN);
		g.drawLine(FINAL_WIDTH / 2, (FINAL_HEIGHT / 2) - 10, FINAL_WIDTH / 2, (FINAL_HEIGHT / 2) + 10);
		g.drawLine((FINAL_WIDTH / 2) - 10, FINAL_HEIGHT / 2, (FINAL_WIDTH / 2) + 10, FINAL_HEIGHT / 2);
	}
	
	protected void finalizeRender() {
		g2.drawImage(img, 0, 0, FINAL_WIDTH, FINAL_HEIGHT, null);
	}
	
	/**
	 * Uses multiple threads to render the scene.
	 */
	private void parallelRender() {
		latchref.update(rendererCount);
		for (ThreadRenderer renderer : renderers) {
			executor.execute(renderer);
		}
		
		try {
			latchref.latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void preRender() {
		finished = false;
		handleMouseInput();
		getCameraVectors();
		resetImage();
	}

	protected void render() {
		preRender();
		parallelRender();
		finalizeRender();
		postRender();
	}
	
	protected void postRender() {
		resetZBuffer();
		drawDebugInfo();
		drawCrosshair();
		finished = true;
	}
	
	/**
	 * Render this Raycaster's WorldMap to a Graphics object. <b>DO NOT CALL THIS
	 * METHOD; IT IS HANDLED BY REPAINTMANAGER.</b>
	 *
	 * @param graphics
	 */
	@Override
	public void paintComponent(Graphics graphics) {
		if (enabled.get()) {
			updateGraphics(graphics);
			render();
		}
	}
	
	private void resetZBuffer() {
		for (int i = 0; i < WIDTH; i++) {
			zbuf[i] = Double.POSITIVE_INFINITY;
		}
	}
	
	private void populateWallDistLUT() {
		wallDistLUT = new double[HALF_HEIGHT];
		wallDistLUT[0] = Double.POSITIVE_INFINITY;
		for (int i = 1; i < HALF_HEIGHT; i++) {
			int y = i + (HALF_HEIGHT);
			wallDistLUT[i] = HEIGHT / ((2.0 * y) - HEIGHT);
		}
	}
	
	public synchronized void start() {
		// setVisible(true);
		// requestFocusInWindow();
		enabled.set(true);
	}
	
	public synchronized void stop() {
		enabled.set(false);
	}
	
	private void handleMouseInput() {
		// System.out.println(parentGame.mouse.dx());
		if (parentGame.mouse.dx() < 0) {
			camera.rotateLeft(Math.abs(parentGame.mouse.dx()));
		} else if (parentGame.mouse.dx() > 0) {
			camera.rotateRight(parentGame.mouse.dx());
		}

		if (parentGame.mouse.dy() < 0) {
			camera.cheapRotateUp(Math.abs(parentGame.mouse.dy()) & upDownEnabled, HEIGHT);
		} else if (parentGame.mouse.dy() > 0) {
			camera.cheapRotateDown(parentGame.mouse.dy() & upDownEnabled, HEIGHT);
		}
	}
	
	/**
	 * Renders a portion of the image from a given starting point to a given ending
	 * point. Should be run on a separate Thread.
	 *
	 * @author Joe Desmond
	 */
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
			this.render();
			latchref.latch.countDown();
		}
		
		private double adjMapX;
		private double adjMapY;
		private int adjStepX;
		private int adjStepY;
		private boolean side = false;
		private double xNorm;
		private double rdirx;
		private double rdiry;
		private int mapX;
		private int mapY;
		private Vector2 customHit;
		private int drawStart;
		private int drawEnd;
		private int trueDrawStart;
		private int lineHeight;
		private double perpWallDist = 0;
		private Block block;
		private Wall hitWall;
		private double wallX;
		private Side sideHit;
		
		/**
		 * Uses 2D raycasting. It casts a ray into the scene for every x-value from
		 * <code>startX</code> to <code>endX</code>, checking for intersections with
		 * "renderable" objects in the World using digital differential analysis (DDA).
		 * The speed of this algorithm does not vary with the size of the world.
		 */
		private void render() {
			for (int x = startX; x < endX; x++) {
				block = null;

				xNorm = 2 * x / (double) WIDTH - 1.0;

				rdirx = dir.x + plane.x * xNorm;
				rdiry = dir.y + plane.y * xNorm;

				mapX = (int) pos.x;
				mapY = (int) pos.y;

				double sideDistX;
				double sideDistY;

				double deltaDistX = Math.sqrt(1 + (rdiry * rdiry) / (rdirx * rdirx));
				double deltaDistY = Math.sqrt(1 + (rdirx * rdirx) / (rdiry * rdiry));

				int stepX;
				int stepY;

				boolean hit = false;

				if (rdirx < 0) {
					stepX = -1;
					sideDistX = (pos.x - mapX) * deltaDistX;
				} else {
					stepX = 1;
					sideDistX = (mapX + 1.0 - pos.x) * deltaDistX;
				}

				if (rdiry < 0) {
					stepY = -1;
					sideDistY = (pos.y - mapY) * deltaDistY;
				} else {
					stepY = 1;
					sideDistY = (mapY + 1.0 - pos.y) * deltaDistY;
				}
				Vector2 currentLoc;
				customHit = null;
				Vector2 tested = null;
				hitWall = null;

				dda: while (!hit) {
					if (sideDistX < sideDistY) {
						sideDistX += deltaDistX;
						mapX += stepX;
						side = false;
					} else {
						sideDistY += deltaDistY;
						mapY += stepY;
						side = true;
					}
					block = world.getBlockAt(mapX, mapY);
					if (block != Block.SPACE) {
						if (block.isCustom()) {
							currentLoc = new Vector2(mapX, mapY);
							Vector2 rayDirection = new Vector2((float) rdirx + pos.x, (float) rdiry + pos.y);
							for (Wall l : block.walls) {
								Wall testing = new Wall(l.v0.add(currentLoc), l.v1.add(currentLoc)).tile(l.xTiles,
										l.yTiles);
								tested = RenderUtils.rayHitSegment(pos, rayDirection, testing);

								if (tested != null) {
									double tempDist;
									if (!side) {
										tempDist = ((tested.x - pos.x + (1 - Math.abs(stepX)) / 2)) / rdirx;
									} else {
										tempDist = ((tested.y - pos.y + (1 - Math.abs(stepY)) / 2)) / rdiry;
									}
									if (tempDist < zbuf[x]) {
										zbuf[x] = tempDist;
										testing.texture = l.texture;
										hitWall = testing;
										customHit = tested;
									}
								}
							}
							if (customHit != null) {
								break dda;
							}
						} else {
							determineSideHit(stepX, stepY);
							hit = true;
						}
					}
				}

				if (customHit != null) {
					adjMapX = customHit.x;
					adjMapY = customHit.y;
					adjStepX = Math.abs(stepX);
					adjStepY = Math.abs(stepY);
				} else {
					adjMapX = mapX;
					adjMapY = mapY;
					adjStepX = stepX;
					adjStepY = stepY;
				}

				if (!side) {
					perpWallDist = ((adjMapX - pos.x + (1 - adjStepX) / 2)) / rdirx;
				} else {
					perpWallDist = ((adjMapY - pos.y + (1 - adjStepY) / 2)) / rdiry;
				}

				lineHeight = (int) (HEIGHT / perpWallDist);

				drawStart = -(lineHeight >> 1) + HALF_HEIGHT;
				trueDrawStart = drawStart;
				if (drawStart < 0) {
					drawStart = 0;
				}
				drawEnd = (lineHeight >> 1) + HALF_HEIGHT;
				if (drawEnd >= HEIGHT) {
					drawEnd = HEIGHT - 1;
				}

				if (customHit == null) {
					textureBlock(x);
				} else {
					textureCustomBlock(x);
				}
				// img.setRGB(x, drawStart, 0xFFFF0000);
				// img.setRGB(x, drawEnd, 0xFFFF0000);

				zbuf[x] = perpWallDist;

				textureFloorAndCeiling(x);
				if (x == (WIDTH / 2)) {
					// System.out.println(sideHit);
				}
			}
		}
		
		private void determineSideHit(int stepX, int stepY) {
			if (side) {
				if (stepY < 0) {
					sideHit = Side.POSY;
				} else {
					sideHit = Side.NEGY;
				}
			} else {
				if (stepX < 0) {
					sideHit = Side.POSX;
				} else {
					sideHit = Side.NEGX;
				}
			}
		}
		
		private void textureBlock(int x) {
			if (side) {
				wallX = (pos.x + ((adjMapY - pos.y + (1 - adjStepY) / 2) / rdiry) * rdirx);
			} else {
				wallX = (pos.y + ((adjMapX - pos.x + (1 - adjStepX) / 2) / rdirx) * rdiry);
			}

			wallX -= Math.floor(wallX);

			int texX;

			texX = (int) (wallX * block.frontTexture.SIZE);

			if (side) {
				texX = (int) ((texX * block.sideXTiles) % block.sideTexture.SIZE);
			} else {
				texX = (int) ((texX * block.frontXTiles) % block.frontTexture.SIZE);
			}

			if ((!side && rdirx > 0) || (side && rdiry < 0)) {
				texX = block.frontTexture.SIZE - texX - 1;
			}

			for (int y = drawStart; y < drawEnd; y++) {
				int texY;

				texY = ((((y << 1) - HEIGHT + lineHeight) * block.frontTexture.SIZE) / lineHeight) >> 1;

				if (side) {
					texY = (int) ((texY * block.sideYTiles) % block.sideTexture.SIZE);
				} else {
					texY = (int) ((texY * block.frontYTiles) % block.frontTexture.SIZE);
				}

				int color;
				if (!side && (texX + (texY * block.frontTexture.SIZE)) < block.frontTexture.pixels.length
						&& (texX + (texY * block.frontTexture.SIZE)) >= 0) {
					color = block.frontTexture.pixels[texX + (texY * block.frontTexture.SIZE)];
				} else if ((texX + (texY * block.sideTexture.SIZE)) < block.sideTexture.pixels.length
						&& (texX + (texY * block.sideTexture.SIZE)) >= 0) {
					color = (block.sideTexture.pixels[texX + (texY * block.sideTexture.SIZE)]);
				} else {
					color = 0;
				}
				float normValue = (float) (perpWallDist / FULL_FOG_DISTANCE);
				color = RenderUtils.darkenWithThreshold(color, normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD);

				img.setRGB(x, y, color);
			}
		}
		
		private void textureCustomBlock(int x) {
			wallX = hitWall.getNorm(customHit);

			wallX -= Math.floor(wallX);

			int texX;
			GeneralTexture texture = hitWall.texture;

			texX = (int) (texture.width * wallX * hitWall.xTiles) % texture.width;

			for (int y = drawStart; y < drawEnd; y++) {
				int texY;
				texY = (int) ((((y - trueDrawStart) / (float) lineHeight) * texture.height) * hitWall.yTiles)
						% texture.height;

				int color = 0;

				int index = (texX + texY * texture.width);

				if (index >= 0 && index < texture.pixels.length) {
					color = texture.pixels[index];
				}

				float normValue = (float) (perpWallDist / FULL_FOG_DISTANCE);
				color = RenderUtils.darkenWithThreshold(color, normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD);
				img.setRGB(x, y, color);
			}
		}
		
		private void textureFloorAndCeiling(int x) {
			double floorXWall;
			double floorYWall;

			if (customHit != null) {
				floorXWall = adjMapX;
				floorYWall = adjMapY;
			} else if (!side && rdirx > 0) {
				floorXWall = mapX;
				floorYWall = mapY + wallX;
			} else if (!side && rdirx < 0) {
				floorXWall = mapX + 1.0;
				floorYWall = mapY + wallX;
			} else if (side && rdiry > 0) {
				floorXWall = mapX + wallX;
				floorYWall = mapY;
			} else {
				floorXWall = mapX + wallX;
				floorYWall = mapY + 1.0;
			}

			double currentDist;

			if (drawEnd < 0) {
				drawEnd = HEIGHT;
			}

			SquareTexture floortex;
			SquareTexture ceilingtex;

			for (int y = drawEnd + 1; y < HEIGHT; y++) {
				currentDist = wallDistLUT[y - HALF_HEIGHT];

				double weight = (currentDist) / (perpWallDist);

				double currentFloorX = weight * floorXWall + (1.0 - weight) * pos.x;
				double currentFloorY = weight * floorYWall + (1.0 - weight) * pos.y;

				floortex = world.getFloorAt((int) currentFloorX, (int) currentFloorY);
				ceilingtex = world.getCeilingAt((int) currentFloorX, (int) currentFloorY);

				int floorTexX;
				int floorTexY;
				floorTexX = (int) (currentFloorX * floortex.SIZE) % floortex.SIZE;
				floorTexY = (int) (currentFloorY * floortex.SIZE) % floortex.SIZE;

				int ceilTexX;
				int ceilTexY;
				if (floortex.SIZE == ceilingtex.SIZE) {
					ceilTexX = floorTexX;
					ceilTexY = floorTexY;
				} else {
					ceilTexX = (int) (currentFloorX * ceilingtex.SIZE) % ceilingtex.SIZE;
					ceilTexY = (int) (currentFloorY * ceilingtex.SIZE) % ceilingtex.SIZE;
				}

				int color = (floortex.pixels[floortex.SIZE * floorTexY + floorTexX]);
				// int color = 0xFF323232;
				int ceilColor = (ceilingtex.pixels[ceilingtex.SIZE * ceilTexY + ceilTexX]);
				// int ceilColor = 0xFF505050;
				float normValue = (float) (currentDist / FULL_FOG_DISTANCE);
				color = RenderUtils.darkenWithThreshold(color, normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD);
				ceilColor = RenderUtils.darkenWithThreshold(ceilColor, normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD);
				img.setRGB(x, y, color);
				img.setRGB(x, (HEIGHT - y), ceilColor);
			}
		}
	}
	
	private class LatchRef {

		CountDownLatch latch = new CountDownLatch(0);
		
		public void update(int value) {
			latch = new CountDownLatch(value);
		}
	}
	
	public void resetCameraAndMap(Camera _camera, WorldMap _worldMap) {
		camera = _camera;
		world = _worldMap;
	}
	
	private void createThreadPoolRenderers() {
		if (rendererCount > WIDTH) {
			String error = "It is impossible to have more thread renderers than stripes on the screen!";
			System.err.println(error);
			System.exit(0);
		}
		
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(rendererCount);
		System.out.println(executor.getCorePoolSize() + " threads created for thread renderers.");
		
		// In case the executor could not make all the threads we wanted
		rendererCount = executor.getCorePoolSize();
		
		renderers = new ThreadRenderer[rendererCount];
		latchref = new LatchRef();
		
		int interval = (WIDTH - (WIDTH % rendererCount)) / rendererCount;
		
		int step = 0;
		
		while (step + interval < WIDTH) {
			int i = step / interval;
			
			renderers[i] = new ThreadRenderer(step, step + interval, i);
			step += interval;
		}

		renderers[renderers.length - 1] = new ThreadRenderer(step, WIDTH, rendererCount - 1);
	}

	public void shutdown() {
		executor.shutdownNow();
		System.out.println("Shutting down all Raycaster threads.");
	}
	
	public void enableVerticalMouselook() {
		upDownEnabled = 0xFFFFFFFF;
	}
	
	public void disableVerticalMouselook() {
		upDownEnabled = 0;
	}
	
	public void updateGraphics(Graphics graphics) {
		g = graphics;
	}
}
