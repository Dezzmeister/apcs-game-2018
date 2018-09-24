package render.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import image.Entity;
import image.GeneralTexture;
import image.HUD;
import image.SquareTexture;
import image.Texture;
import image.ViewModel;
import main.Game;
import main.entities.Bean;
import main.entities.Dwight;
import render.core.true3D.Line;
import render.light.Side;
import render.math.RenderUtils;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

/**
 * Uses multiple threads to render a map. Uses a Camera holding player data such
 * as position, direction, and FoV.
 * 
 * This rendering engine is a <u><i><b>BEAUTIFUL</b></i></u> hybrid that uses a DDA for blocks, a ray/line-segment intersection
 * algorithm for custom walls, and a line/plane intersection algorithm with barycentric rasterization for true 3D objects. 
 * As of right now, it only supports vertical stripes of transparency in custom walls.
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
	protected float ASPECT, FINAL_ASPECT;
	
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
	private volatile List <Entity> sprites;
	
	public static final float FULL_FOG_DISTANCE = 10f; //5f
	public static final int OLD_SHADE_THRESHOLD = 100;
	public static final int SHADE_THRESHOLD = 200;
	
	protected int rendererCount;
	protected ThreadRenderer[] renderers;
	protected ThreadPoolExecutor executor;
	protected LatchRef latchref;
	protected double[] wallDistLUT;
	protected AtomicBoolean enabled = new AtomicBoolean(true);
	protected Game parentGame;
	protected int upDownEnabled = 0;
	public boolean finished = true;
	public boolean linearShade = false;
	private HUD hud;
	private ViewModel currentViewModel;
	private int dwightsKilled = 0;
	private List<Entity> hitEntities = new ArrayList<Entity>();
	private float closestWallAtCenter = 0;
	private List<Vector3> modelQueue = Collections.synchronizedList(new ArrayList<Vector3>());
	
	private double[] zbuf2;
	
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
		
		FINAL_ASPECT = FINAL_WIDTH/(float)FINAL_HEIGHT;
		
		ASPECT = WIDTH/(float)HEIGHT;
		
		init();
	}
	
	private void init() {
		zbuf = new double[WIDTH];
		zbuf2 = new double[WIDTH * HEIGHT];
		HALF_HEIGHT = HEIGHT / 2;
		camera.setVerticalMouselookLimit(HEIGHT / 8);
		
		resetZBuffer();
		populateWallDistLUT();
		generateDimensionLUTs();
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
		
		g.drawString("Dwights killed: " + dwightsKilled, 5, 50);
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
		sprites = world.entities;
		finished = false;
		handleMouseInput();
		getCameraVectors();
		resetModelQueue();
		resetImage();
	}

	protected void render() {
		if (hud.getHealth() > 0) {
			preRender();
			parallelRender();
			renderSprites();
			renderAllVisibleModels();
			drawOverlays();
			finalizeRender();
			postRender();
		} else {
			drawDeathScreen();
		}
	}
	
	protected void postRender() {
		saveClosestWallAtCenter();
		resetZBuffer();
		drawDebugInfo();
		drawCrosshair();
		finished = true;
	}
	
	protected void drawOverlays() {
		drawHUD();
		drawCurrentViewModel();
	}
	
	private void drawDeathScreen() {
		resetImage();
		img = Block.DwightElements.DEATH;
		finalizeRender();
	}
	
	private void saveClosestWallAtCenter() {
		closestWallAtCenter = (float) zbuf[WIDTH/2];
	}
	
	private void resetModelQueue() {
		modelQueue = new ArrayList<Vector3>();
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
			for (int j = 0; j < HEIGHT; j++) {
				zbuf2[i + j * WIDTH] = Double.POSITIVE_INFINITY;
			}
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
	
	private void renderSprites() {
		hitEntities = new ArrayList<Entity>();
		sprites = new ArrayList<Entity>();
		
		if (dwightList != null) {
			sprites.addAll(dwightList);
		}
		if (beanList != null) {
			sprites.addAll(beanList);
		}
		
		for (int i = 0; i < sprites.size(); i++) {
	    	sprites.get(i).order = i;
	    	sprites.get(i).updateDistance();
	    }
		
		sprites.sort(null);
		
		for (int i = 0; i < sprites.size(); i++) {
			//System.out.println(i + " " + sprites.get(i).distance);
			
			Entity active = sprites.get(i);
			
			Vector2 spriteVector = active.pos;
	    	double spriteX = spriteVector.x - pos.x;
	    	double spriteY = spriteVector.y - pos.y;
	    	
	    	//Faster than using Matrix2 class
	    	double invDet = 1.0 / (plane.x * dir.y - dir.x * plane.y);
	    	double transformX = invDet * (dir.y * spriteX - dir.x * spriteY);
	    	double transformY = invDet * (-plane.y * spriteX + plane.x * spriteY);
	    	
	    	int spriteScreenX = (int)((WIDTH >> 1) * (1 + transformX / transformY));
	    	
	    	int spriteHeight = Math.abs((int)((HEIGHT / transformY) * FINAL_ASPECT));
	    	int drawStartY = -(spriteHeight >> 1) + (HEIGHT >> 1);
	    	if (drawStartY < 0) {
	    		drawStartY = 0;
	    	}
	    	int drawEndY = (spriteHeight >> 1) + (HEIGHT >> 1);
	    	if (drawEndY >= HEIGHT) {
	    		drawEndY = HEIGHT - 1;
	    	}
	    	
	    	int spriteWidth = Math.abs((int)(HEIGHT / transformY));
	    	int drawStartX = -(spriteWidth >> 1) + spriteScreenX;
	    	if (drawStartX < 0) {
	    		drawStartX = 0;
	    	}
	    	int drawEndX = (spriteWidth >> 1) + spriteScreenX;
	    	if (drawEndX >= WIDTH) {
	    		drawEndX = WIDTH - 1;
	    	}
	    	
	    	if (drawStartX < WIDTH/2 && drawEndX > WIDTH/2) {
	    		hitEntities.add(active);
	    	}
	    	
	    	SquareTexture texture = active.getActiveTexture();
	    	
	    	int texWidth = texture.SIZE;
	    	int texHeight = texture.SIZE;
	    	for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
	    		int texX = (int)(((stripe - ((-spriteWidth >> 1) + spriteScreenX)) << 8) * texWidth / spriteWidth) >> 8;
	    		if (transformY > 0 && stripe > 0 && stripe < WIDTH && transformY < zbuf[stripe]) {
	    			
	    			for (int y = drawStartY; y < drawEndY; y++) {
	    				int d = (y << 8) - (HEIGHT << 7)  + (spriteHeight << 7);
	    				int texY = ((d * texHeight)/spriteHeight) >> 8;
	    				
	    				int index = texX + texWidth * texY;
	    				//If the index is out of bounds, black is drawn
	    				int	color = index < texture.pixels.length && index >= 0 ? texture.pixels[index] : 0;
	    				
	    				if (color != active.alpha) {
	    					
	    					zbuf2[stripe + y * WIDTH] = transformY;

	    					img.setRGB(stripe, y, shade(active.distance,color));
	    				}
	    			}
	    		}
	    	}
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
				
				//Modified DDA that switches to slower ray/line segment intersection tests when encountering a custom Block
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
					if (block.isVisible() && (block.getProximity() == -1 || block.getProximity() > Vector2.distance(pos, new Vector2(mapX,mapY)))) {
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
									
									float wallLength = testing.length;
									float normDist = Vector2.distance(testing.v0, tested)/wallLength;
									int textureDist = (int) (normDist * l.texture.width);
									
									if (tempDist < zbuf[x] && l.texture.pixels[textureDist] != Texture.ALPHA) {
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
						} else if (block.isModel()) {
							Vector3 location = new Vector3(mapX, mapY, 0);
							
							tryUpdateModelQueue(location);
							
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

				lineHeight = (int) ((HEIGHT / perpWallDist) * FINAL_ASPECT);

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
				
				for (int y = 0; y < HEIGHT; y++) {
					zbuf2[x + y * WIDTH] = perpWallDist;
				}

				textureFloorAndCeiling(x);
				if (x == (WIDTH / 2)) {
					//System.out.println(sideHit);
				}
			}
		}
		
		private void tryUpdateModelQueue(Vector3 location) {
			
			synchronized (modelQueue) {
				for (int i = 0; i < modelQueue.size(); i++) {
					if (modelQueue.get(i).equals(location)) {
						return;
					}
				}
			
				modelQueue.add(location);
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

			if (side) {
				texX = (int) (wallX * block.sideTexture.SIZE);
				texX = (int) ((texX * block.sideXTiles) % block.sideTexture.SIZE);
			} else {
				texX = (int) (wallX * block.frontTexture.SIZE);
				texX = (int) ((texX * block.frontXTiles) % block.frontTexture.SIZE);
			}

			if ((!side && rdirx > 0)) {
				texX = block.frontTexture.SIZE - texX - 1;
			} else if (side && rdiry < 0) {
				texX = block.sideTexture.SIZE - texX - 1;
			}

			for (int y = drawStart; y < drawEnd; y++) {
				int texY;

				if (side) {
					texY = ((((y << 1) - HEIGHT + lineHeight) * block.sideTexture.SIZE) / lineHeight) >> 1;
					texY = (int) ((texY * block.sideYTiles) % block.sideTexture.SIZE);
				} else {
					texY = ((((y << 1) - HEIGHT + lineHeight) * block.frontTexture.SIZE) / lineHeight) >> 1;
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

				img.setRGB(x, y, shade((float)perpWallDist,color));
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

				img.setRGB(x, y, shade((float)perpWallDist,color));
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
				currentDist = wallDistLUT[y - HALF_HEIGHT] * FINAL_ASPECT;

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
				
				int ceilColor = (ceilingtex.pixels[ceilingtex.SIZE * ceilTexY + ceilTexX]);

				img.setRGB(x, y, shade((float)currentDist, color));
				img.setRGB(x, HEIGHT-y, shade((float)currentDist,ceilColor));
			}
		}
	}
	
	private int shade(float distance, int color) {
		float darkenBy;
		if (linearShade) {
			float normValue = (float) (distance / FULL_FOG_DISTANCE);
			
			darkenBy = (normValue >= 1 ? 1 : normValue) * SHADE_THRESHOLD;
		} else {
			float _x = (float) (distance >= FULL_FOG_DISTANCE ? FULL_FOG_DISTANCE : distance);
			float a = SHADE_THRESHOLD/100.0f;
			float b = 2 * FULL_FOG_DISTANCE;
			darkenBy = (-(a * _x) * (_x - b));
		}
		
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		
		red -= (red - darkenBy >= 0) ? darkenBy : red;
		green -= (green - darkenBy >= 0) ? darkenBy : green;
		blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
		
		color = (red << 16) | (green << 8) | blue;
		
		return color;
	}
	
	//positive z points up
	private void OLD_renderAllVisibleModels() {	
		Map<Vector3, Vector3> renderedVertices = new HashMap<Vector3, Vector3>();
		
		Vector3 pos3D = new Vector3(camera.pos.x, 0.0f, camera.pos.y);
		
		Vector3 cameraPos = new Vector3(camera.pos.x, 0.5f, camera.pos.y);
		//System.out.println("camerapos: " + cameraPos);
		
		Vector2 plane = camera.pos.add(camera.dir).add(camera.plane);
		//System.out.println("plane: " + plane);
		Vector2 negplane = camera.pos.add(camera.dir).add(new Vector2(-camera.plane.x,-camera.plane.y));
		//System.out.println("negplane: " + negplane);
		
		Vector3 plane0 = new Vector3(plane.x, 0.0f, plane.y);
		Vector3 plane1 = new Vector3(plane.x, 1.0f, plane.y);
		Vector3 plane2 = new Vector3(negplane.x, 0.5f, negplane.y);
		
		Triangle viewPlane = new Triangle(plane0, plane2, plane1);		
		
		for (Vector3 location : modelQueue) {
			Block block = world.getBlockAt((int)location.x, (int)location.z);
			
			for (Triangle t : block.model.triangles) {
				Vector3 v0 = t.v0.plus(location);
				Vector3 v1 = t.v1.plus(location);
				Vector3 v2 = t.v2.plus(location);
				
				Line l0 = new Line(cameraPos,v0);
				Line l1 = new Line(cameraPos,v1);
				Line l2 = new Line(cameraPos,v2);
				
				Vector3 hit0;
				Vector3 hit1;
				Vector3 hit2;
				
				if (!renderedVertices.containsKey(v0)) {
					hit0 = RenderUtils.linePlaneIntersection(l0, viewPlane);
					//System.out.println("v0: " + v0);
					//System.out.println("hit0: " + hit0);
					renderedVertices.put(v0, hit0);
				} else {
					hit0 = renderedVertices.get(v0);
				}
				
				if (!renderedVertices.containsKey(v1)) {
					hit1 = RenderUtils.linePlaneIntersection(l1, viewPlane);
					renderedVertices.put(v1, hit1);
					//System.out.println("hit1: " + hit1);
				} else {
					hit1 = renderedVertices.get(v1);
				}
				
				if (!renderedVertices.containsKey(v2)) {
					hit2 = RenderUtils.linePlaneIntersection(l2, viewPlane);
					renderedVertices.put(v2, hit2);
				} else {
					hit2 = renderedVertices.get(v2);
				}
				
				//System.out.println(hit2);
				
				Vector2 screen0 = findPointOnScreen(hit0);
				Vector2 screen1 = findPointOnScreen(hit1);
				Vector2 screen2 = findPointOnScreen(hit2);
				
				
				//System.out.println("screen0: " + screen0);
				//System.out.println("screen1: " + screen1);
				//System.out.println("screen2: " + screen2);
				/*
				g.setColor(new Color(t.debug_color));
				int[] xPoints = {(int)screen0.x, (int)screen1.x, (int)screen2.x};
				int[] yPoints = {(int)screen0.y, (int)screen1.y, (int)screen2.y};
				g.fillPolygon(xPoints, yPoints, 3);
				*/
				//rasterizer.rasterizeTriangle(screen0, screen1, screen2, t.debug_color);
			}
		}
	}
	
	//x and y correspond to world map axes; z is vertical axis; positive z points up
	private void renderAllVisibleModels() {
		Map<Vector3, Vector3> renderedVertices = new HashMap<Vector3, Vector3>();
		
		//These three Vectors define the plane through which the player views the world, in world space
		Vector3 plane0 = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x, camera.pos.y + camera.dir.y + camera.plane.y, 0.0f);
		Vector3 plane1 = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x, camera.pos.y + camera.dir.y + camera.plane.y, 1.0f);
		Vector3 plane2 = new Vector3(camera.pos.x + camera.dir.x - camera.plane.x, camera.pos.y + camera.dir.y - camera.plane.y, 0.0f);
		
		//plane2 is the left edge of the screen; plane0 is the right edge of the screen
		
		Vector3 cameraPos = new Vector3(camera.pos.x, camera.pos.y, 0.5f);
		Vector3 cameraLeft = new Vector3(camera.pos.x - camera.plane.x, camera.pos.y - camera.plane.y, 0);
		
		Triangle viewPlane = new Triangle(plane0, plane1, plane2);
		
		float zTranslate = (FINAL_ASPECT - 1)/2;
		
		for (Vector3 location : modelQueue) {
			Block block = world.getBlockAt((int)location.x, (int)location.y);
			
			for (Triangle t : block.model.triangles) {
				
				//These three Vectors are the triangle's vertices in model space
				Vector3 _v0 = new Vector3(t.v0.x, t.v0.y, t.v0.z);
				Vector3 _v1 = new Vector3(t.v1.x, t.v1.y, t.v1.z);
				Vector3 _v2 = new Vector3(t.v2.x, t.v2.y, t.v2.z);
				
				//The vertices are scaled to fit with the final aspect ratio
				_v0.z = (t.v0.z * FINAL_ASPECT) - zTranslate;
				_v1.z = (t.v1.z * FINAL_ASPECT) - zTranslate;
				_v2.z = (t.v2.z * FINAL_ASPECT) - zTranslate;
				
				//These three Vectors are the triangle's vertices, translated to world space
				Vector3 v0 = location.plus(_v0);
				Vector3 v1 = location.plus(_v1);
				Vector3 v2 = location.plus(_v2);
				
				if (isBehindPlayer(v0,v1,v2, cameraLeft, cameraPos)) {
					continue;
				}
				
				Vector3 i0 = getOrCalculateAndStore(renderedVertices, cameraPos, v0, viewPlane);
				Vector3 i1 = getOrCalculateAndStore(renderedVertices, cameraPos, v1, viewPlane);
				Vector3 i2 = getOrCalculateAndStore(renderedVertices, cameraPos, v2, viewPlane);
				
				if (i0 == null || i1 == null || i2 == null) {
					continue;
				}
				
				Vector2 s0 = locateOnScreen(i0, plane2, plane0);
				Vector2 s1 = locateOnScreen(i1, plane2, plane0);
				Vector2 s2 = locateOnScreen(i2, plane2, plane0);
				
				rasterizer.rasterizeTriangleBarycentric(s0, s1, s2, t, v0, v1, v2, cameraPos);
			}
		}
	}
	
	private boolean isBehindPlayer(Vector3 _v0, Vector3 _v1, Vector3 _v2, Vector3 _plane0, Vector3 _plane1) {
		Vector2 v0 = _v0.discardZ();
		Vector2 v1 = _v1.discardZ();
		Vector2 v2 = _v2.discardZ();
		Vector2 plane0 = _plane0.discardZ();
		Vector2 plane1 = _plane1.discardZ();
		
		boolean first = RenderUtils.isLeftOfRay(plane1, plane0, v0);
		boolean second = RenderUtils.isLeftOfRay(plane1, plane0, v1);
		boolean third = RenderUtils.isLeftOfRay(plane1,plane0,v2);
		
		return first && second && third;
	}
	
	private Vector2 locateOnScreen(Vector3 i, Vector3 plane0, Vector3 plane1) {
		int y = (int)((1 - i.z) * HEIGHT);
		
		Vector2 p0 = new Vector2(plane0.x,plane0.y);
		Vector2 p1 = new Vector2(plane1.x,plane1.y);
		
		Wall wall = new Wall(p0, p1);
		Vector2 i2 = new Vector2(i.x,i.y);
		
		float norm = wall.getNorm(i2);
		
		Vector2 leftWallEndp = new Vector2(p0.x + camera.dir.x, p0.y + camera.dir.y);
		
		if (RenderUtils.isLeftOfRay(p0, leftWallEndp, i2)) {
			norm = -norm;
		}
		
		int x = (int) (norm * WIDTH);
		
		return new Vector2(x,y);
	}
	
	private Vector3 getOrCalculateAndStore(Map<Vector3,Vector3> renderedVertices, Vector3 cameraPos, Vector3 v, Triangle viewPlane) {
		if (renderedVertices.containsKey(v)) {
			return renderedVertices.get(v);
		} else {
			Vector3 i = RenderUtils.linePlaneIntersection(new Line(cameraPos, v), viewPlane);
			renderedVertices.put(v, i);
			return i;
		}
	}
	
	@Deprecated
	private Vector2 findPointOnScreen(Vector3 v) {
		//x -> x
		//z -> y
		Vector3 pos3D = new Vector3(camera.pos.x, 0.0f, camera.pos.y);
		Vector2 plane = camera.pos.add(camera.dir).add(camera.plane);
		Vector2 negplane = camera.pos.add(camera.dir).add(new Vector2(-camera.plane.x,-camera.plane.y));
		
		Vector3 plane0 = new Vector3(plane.x, 0.0f, plane.y);
		Vector3 plane1 = new Vector3(negplane.x, 0.0f, negplane.y);
		
		float y = v.y * 1000;
		float x = (Vector3.distance(v,plane0)) * 1000;
		
		//System.out.println(Vector3.distance(v,plane0) + Vector3.distance(v,plane1));
		//System.out.println(Vector3.distance(plane0, plane1));
		
		return new Vector2(x,y);
	}
	
	private Rasterizer rasterizer = new Rasterizer();
	
	private class VectorAssociate {
		Vector2 v2;
		Vector3 v3;
		
		public VectorAssociate(Vector2 _v2, Vector3 _v3) {
			v2 = _v2;
			v3 = _v3;
		}
	}
	
	@SuppressWarnings("serial")
	private class Rasterizer {
		
		private void rasterizeTriangleBarycentric(Vector2 v0, Vector2 v1, Vector2 v2, Triangle triangle, Vector3 v03, Vector3 v13, Vector3 v23, Vector3 camera) {
			float v03Dist = Vector3.distance(v03, camera);
			float v13Dist = Vector3.distance(v13, camera);
			float v23Dist = Vector3.distance(v23, camera);
			
			VectorAssociate a0 = new VectorAssociate(v0,v03);
			VectorAssociate a1 = new VectorAssociate(v1,v13);
			VectorAssociate a2 = new VectorAssociate(v2,v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {{
				add(a0);
				add(a1);
				add(a2);
			}};
			ySorted.sort((a, b) -> (int)(a.v2.y - b.v2.y));
			
			List<VectorAssociate> xSorted = new ArrayList<VectorAssociate>() {{
				add(a0);
				add(a1);
				add(a2);
			}};
			xSorted.sort((a, b) -> (int)(a.v2.x - b.v2.x));
			
			int startX = (int)xSorted.get(0).v2.x;
			int endX = (int)xSorted.get(2).v2.x;
			
			int startY = (int)ySorted.get(0).v2.y;
			int endY = (int)ySorted.get(2).v2.y;
			
			for (int y = startY; y <= endY; y++) {
				if (y >= 0 && y < HEIGHT) {
					for (int x = startX; x <= endX; x++) {
						if (x >= 0 && x < WIDTH) {
							float w0;
							float w1;
							float w2;
							
							float yv1myv2 = v1.y - v2.y;
							float pxmxv2 = x - v2.x;
							float xv0mxv2 = v0.x - v2.x;
							float xv2mxv1 = v2.x - v1.x;
							float pymyv2 = y - v2.y;
							float yv0myv2 = v0.y - v2.y;
							float yv2myv0 = v2.y - v0.y;
							
							float denom = (yv1myv2 * xv0mxv2) + (xv2mxv1 * yv0myv2);
							
							w0 = ((yv1myv2 * pxmxv2) + (xv2mxv1 * pymyv2))/denom;
							
							if (w0 < 0) {
								continue;
							}
							
							w1 = ((yv2myv0 * pxmxv2) + (xv0mxv2 * pymyv2))/denom;
							
							if (w1 < 0) {
								continue;
							}
							
							w2 = 1 - w0 - w1;
							
							if (w2 < 0) {
								continue;
							}
							
							float distance = (w0 * v03Dist) + (w1 * v13Dist) + (w2 * v23Dist);
							
							if (distance < zbuf2[x + y * WIDTH]) {
								zbuf2[x + y * WIDTH] = distance;
								
								int color = triangle.color;
								
								if (triangle.uv0 != null) {
									Vector2 uv0 = triangle.uv0.scale(w0);
									Vector2 uv1 = triangle.uv1.scale(w1);
									Vector2 uv2 = triangle.uv2.scale(w2);
									
									Vector2 uv = uv0.add(uv1).add(uv2);
									
									GeneralTexture texture = triangle.texture;
									
									int texX = (int) (uv.x * texture.width);
									int texY = (int) (uv.y * texture.height);
									
									if (texX >= 0 && texX < texture.width && texY >= 0 && texY < texture.height) {
										color = texture.pixels[texX + texY * texture.width];
									} else {
										color = 0;
									}
								}
								
								img.setRGB(x, y, shade(distance,color));
							}
						}
					}
				}
			}
		}
		
		@Deprecated
		private void rasterizeTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, Vector3 v03, Vector3 v13, Vector3 v23, Vector3 camera) {
			VectorAssociate a0 = new VectorAssociate(v0,v03);
			VectorAssociate a1 = new VectorAssociate(v1,v13);
			VectorAssociate a2 = new VectorAssociate(v2,v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {{
				add(a0);
				add(a1);
				add(a2);
			}};
			ySorted.sort((a, b) -> (int)(a.v2.y - b.v2.y));
			
			Vector2 _v1 = ySorted.get(0).v2;
			Vector2 _v2 = ySorted.get(1).v2;
			Vector2 _v3 = ySorted.get(2).v2;
			
			Vector3 _v13 = ySorted.get(0).v3;
			Vector3 _v23 = ySorted.get(1).v3;
			Vector3 _v33 = ySorted.get(2).v3;
			
			float _v13Dist = Vector3.distance(camera, _v13);
			float _v23Dist = Vector3.distance(camera, _v23);
			float _v33Dist = Vector3.distance(camera, _v33);
			
			if ((int)_v2.y == (int)_v3.y) {
				rasterizeFlatBottomTriangle(_v1, _v2, _v3, color, _v13Dist, _v23Dist, _v33Dist);
			} else if ((int)_v1.y == (int)_v2.y) {
				rasterizeFlatTopTriangle(_v1, _v2, _v3, color, _v13Dist, _v23Dist, _v33Dist);
			} else {
				Vector2 _v4 = new Vector2((int)(_v1.x + ((_v2.y - _v1.y) / (_v3.y - _v1.y)) * (_v3.x - _v1.x)), _v2.y);
				
				float _v43Dist = (_v13Dist + _v33Dist)/2.0f;
				
				rasterizeFlatBottomTriangle(_v1, _v2, _v4, color, _v13Dist, _v23Dist, _v43Dist);
				rasterizeFlatTopTriangle(_v2, _v4, _v3, color, _v23Dist, _v43Dist, _v33Dist);
			}
		}
		
		private void rasterizeFlatBottomTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, float topVertexDist, float leftVertexDist, float rightVertexDist) {
			float invslope1 = (v1.x - v0.x) / (v1.y - v0.y);
			float invslope2 = (v2.x - v0.x) / (v2.y - v0.y);
			
			float curx1 = v0.x;
			float curx2 = v0.x;
			
			float yDiff = v1.y - v0.y;
			float leftDistDiff = topVertexDist-leftVertexDist;
			float rightDistDiff = topVertexDist-rightVertexDist;
			
			for (int scanlineY = (int)v0.y; scanlineY <= v1.y; scanlineY++) {
				float leftSideDistance = (leftDistDiff * (scanlineY/yDiff)) + leftVertexDist;
				float rightSideDistance = (rightDistDiff * (scanlineY/yDiff) + rightVertexDist);
				
				drawHorizontalLine((int)curx1, (int)curx2, scanlineY, color, leftSideDistance, rightSideDistance);
				curx1 += invslope1;
				curx2 += invslope2;
			}
		}
		
		private void rasterizeFlatTopTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, float bottomVertexDist, float leftVertexDist, float rightVertexDist) {
			float invslope1 = (v2.x - v0.x) / (v2.y - v0.y); 
			float invslope2 = (v2.x - v1.x) / (v2.y - v1.y);
			
			float curx1 = v2.x;
			float curx2 = v2.x;
			
			float yDiff = v2.y - v1.y;
			float leftDistDiff = bottomVertexDist-leftVertexDist;
			float rightDistDiff = bottomVertexDist-rightVertexDist;
			
			for (int scanlineY = (int)v2.y; scanlineY > v1.y; scanlineY--) {
				float leftSideDistance = (leftDistDiff * (scanlineY/yDiff)) + leftVertexDist;
				float rightSideDistance = (rightDistDiff * (scanlineY/yDiff) + rightVertexDist);
				
				drawHorizontalLine((int)curx1, (int)curx2, scanlineY, color, leftSideDistance, rightSideDistance);
				curx1 -= invslope1;
				curx2 -= invslope2;
			}
		}
		
		private void drawHorizontalLine(int x0, int x1, int y, int color, float leftDist, float rightDist) {
			int xBegin = Math.min(x0, x1);
			int xEnd = Math.max(x0, x1);
			
			float distDiff = rightDist-leftDist;
			float xDiff = xEnd-xBegin;
			
			if (y >= 0 && y < HEIGHT) {
				for (int x = xBegin; x < xEnd; x++) {					
					if (x >= 0 && x < WIDTH) {
						float distance = (distDiff * (x/xDiff)) + leftDist;
						if (zbuf2[x + y * WIDTH] > distance) {
							zbuf2[x + y * WIDTH] = distance;
							
							img.setRGB(x, y, shade(distance,color));
						}
					}
				}
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
	
	public List<Entity> getHitEntities() {
		return hitEntities;
	}
	
	public float getClosestWallToCenter() {
		return closestWallAtCenter;
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
	
	public void setEntities(List<Entity> _entities) {
		sprites = _entities;
	}
	
	private List<Dwight> dwightList;
	
	public void setDwights(List<Dwight> dwights) {
		dwightList = dwights;
	}
	
	private List<Bean> beanList;
	
	public void setBeans(List<Bean> beans) {
		beanList = beans;
	}
	
	public void setEntities(List<Dwight> dwights, List<Bean> beans) {
		dwightList = dwights;
		beanList = beans;
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
	
	private float[] widthLUT;
	private float[] heightLUT;
	
	private void generateDimensionLUTs() {
		
		widthLUT = new float[WIDTH];
		for (int x = 0; x < WIDTH; x++) {
			widthLUT[x] = x/(float)WIDTH;
		}
		
		heightLUT = new float[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			heightLUT[y] = y/(float)HEIGHT;
		}
	}
	
	public void setHUD(HUD _hud) {
		hud = _hud;
	}
	
	private void drawHUD() {
		switch (hud.fittedTo()) {
			case BOTTOM:
				drawHUDOnBottom();
				break;
		}
	}
	
	private void drawHUDOnBottom() {
		int startAt = (int) (hud.beginAt * HEIGHT);
		
		for (int y = startAt; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int hudY = (int)(heightLUT[y] * hud.HEIGHT);
				int hudX = (int)(widthLUT[x] * hud.WIDTH);
				
				int color = hud.pixels[hudX + hudY * hud.WIDTH];
				
				if (color == HUD.HEALTH_COLOR || color == HUD.COFFEE_COLOR) {
					color = 0x6E6E6E;
				}
				
				img.setRGB(x, y, color);
			}
		}
		
		drawHealthAndCoffeeBars();		
	}
	
	private void drawHealthAndCoffeeBars() {
		Rectangle healthBar = hud.healthBar();
		
		float normHealthWidth = ((hud.getHealth()/100.0f) * healthBar.width)/(float)hud.WIDTH;
		normHealthWidth = (normHealthWidth < 0) ? 0 : (normHealthWidth > 1.0f) ? 1.0f : normHealthWidth;
		
		int healthWidth = (int) (normHealthWidth * WIDTH);
		int healthHeight = (int) ((healthBar.height/(float)hud.HEIGHT) * HEIGHT);
		int healthX = (int)((healthBar.x/(float)hud.WIDTH) * WIDTH);
		int healthY = (int)((healthBar.y/(float)hud.HEIGHT) * HEIGHT);
		
		Rectangle coffeeBar = hud.coffeeBar();
		float normCoffeeWidth = ((hud.getCoffee()/100.0f) * coffeeBar.width)/(float)hud.WIDTH;
		normCoffeeWidth = (normCoffeeWidth < 0) ? 0 : (normCoffeeWidth > 1.0f) ? 1.0f : normCoffeeWidth;
		
		int coffeeWidth = (int) (normCoffeeWidth * WIDTH);
		int coffeeHeight = (int) ((coffeeBar.height/(float)hud.HEIGHT) * HEIGHT);
		int coffeeX = (int)((coffeeBar.x/(float)hud.WIDTH) * WIDTH);
		int coffeeY = (int)((coffeeBar.y/(float)hud.HEIGHT) * HEIGHT);
		
		for (int y = healthY; y < healthHeight + healthY; y++) {
			for (int x = healthX; x < healthWidth + healthX; x++) {
				img.setRGB(x, y, HUD.HEALTH_COLOR);
			}
		}
		
		for (int y = coffeeY; y < coffeeHeight + coffeeY; y++) {
			for (int x = coffeeX; x < coffeeWidth + coffeeX; x++) {
				img.setRGB(x, y, HUD.COFFEE_COLOR);
			}
		}
	}
	
	public void setCurrentViewModel(ViewModel _viewModel) {
		currentViewModel = _viewModel;
	}
	
	public void setDwightsKilled(int killed) {
		dwightsKilled = killed;
	}
	
	public void drawCurrentViewModel() {
		if (currentViewModel != null) {
			SquareTexture frame = currentViewModel.getActiveFrame();
			
			int workableHeight = (int) (hud.beginAt * HEIGHT);
			
			int modelSize = (int) (currentViewModel.scaleValue * HEIGHT);

			int halfModelSize = modelSize/2;
			
			int startX = (WIDTH/2) - halfModelSize;
			int startY = workableHeight - modelSize;
			
			if (startX < 0) {
				startX = 0;
			}
			
			if (startY < 0) {
				startY = 0;
			}
			
			for (int y = startY; y < startY + modelSize; y++) {
				
				for (int x = startX; x < startX + modelSize; x++) {
					int imgX = (int) (frame.SIZE * ((x - startX)/(float)modelSize));
					int imgY = (int) (frame.SIZE * ((y - startY)/(float)modelSize));
					
					int color = frame.pixels[imgX + frame.SIZE * imgY];
					
					if (color != Texture.ALPHA) {
						img.setRGB(x, y, color);
					}
				}
			}
		}
	}
}
