package render.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.aparapi.Kernel;

import image.Entity;
import image.GeneralTexture;
import image.HUD;
import image.SquareTexture;
import image.Texture;
import image.ViewModel;
import main.Game;
import main.GameConstants;
import main.entities.Bean;
import main.entities.Dwight;
import render.core.true3D.Frustum;
import render.core.true3D.Line;
import render.core.true3D.Transformer;
import render.light.Side;
import render.math.Matrix2;
import render.math.Matrix4;
import render.math.RenderUtils;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

/**
 * Uses multiple threads to render a map. Uses a Camera holding player data such
 * as position, direction, and FoV.
 *
 * This rendering engine is a <u><i><b>BEAUTIFUL</b></i></u> hybrid that uses a
 * DDA for blocks, a ray/line-segment intersection algorithm for custom walls,
 * and a line/plane intersection algorithm with barycentric rasterization for
 * true 3D objects. As of right now, it only supports vertical stripes of
 * transparency in custom walls. <br>
 * Three different algorithms are used to project the world onto the screen:
 * <ol>
 * <li>For every stripe of pixels on the screen, Digital Differential Analysis
 * is used to trace a ray along the XY plane through the cells (1x1x1 Block
 * spaces) of the map until a visible cell is hit. If the cell contains a
 * standard full-size block, the perpendicular distance to the block is
 * calculated (length of the line perpendicular to the screen and intersecting
 * where the block was hit) and the height of the screen is divided by this
 * distance to determine how many pixels tall the wall should be (this is called
 * the "perspective divide"). (NOTE: The Euclidean distance is also calculated
 * and stored in the z-buffers* to ensure accuracy when drawing true 3D models.)
 * The perpendicular distance is used to interpolate the texture's
 * perspective-correct x value for the stripe, and the stripe of pixels at this
 * x value in the texture is then used to texture the stripe on the screen.</li>
 * <br>
 * <li>If the cell hit by the DDA ray is defined as a <b>custom block</b>, different
 * steps must be taken. (NOTE: A custom block is defined as a collection of
 * variable-length lines within a 1x1 space. Walls can be non-orthogonal;
 * however, collision still occurs with the cell's boundaries.) For each line in
 * the cell, the line is translated to world space and a ray/line-segment
 * intersection algorithm is used to determine where the ray hits the line, if
 * it does at all. If it does hit the line, the Euclidean distance from the
 * player to the intersection point is calculated and stored in the z-buffers
 * and a corrected perpendicular distance is used for the perspective divide. (A
 * fisheye effect occurs if you use the Euclidean distance from the player to
 * the wall for the perspective divide.) Texture values are interpolated as
 * usual and the DDA continues through the next stripe of pixels; however, if
 * the ray does not intersect with any walls in the cell, or if the stripe of
 * texture pixels has the transparency pixel at the top ({@link image.Texture.ALPHA ALPHA}), the DDA will continue
 * to the next cell hit by the current ray.</li> <br>
 * <li>If the cell hit by the DDA is defined as a true 3D model (NOTE: A true 3D
 * model is defined as a collection of textured/colored triangles in a 1x1x1
 * cell space), the location of the model is added to a queue (if it isn't
 * already in the queue) and the DDA continues through the same ray. After the
 * DDA finishes rendering all visible 2D elements, the true 3D algorithm
 * iterates through the queue of 3D model locations. For each location, the
 * {@link render.core.Block Block} at that location is found and the algorithm
 * iterates through all of the model's triangles. For each triangle in the
 * model, the triangle is translated to world space and a ray/plane intersection
 * algorithm is used to project the vertices of the triangle onto the viewing
 * plane (the screen in the world). (NOTE: The player is <b>always</b> looking at the
 * world from 0.5 units above the ground; z=0.5.) A {@link java.util.HashMap
 * HashMap} is kept containing all previously calculated triangle vertices and
 * their projected counterparts. If any vertex of the triangle exists in this
 * HashMap, the aforementioned projection calculation is skipped for that
 * vertex. The resultant {@link render.math.Vector3 Vector3} is not yet ready to
 * be displayed on the screen; it is still a point on the view plane, so its
 * normalized XY distance from the leftmost point of the viewing plane is used
 * as the x value and its z-coordinate is used as the y value since the
 * z-coordinate is already normalized (Nothing in the world should be above z=1 or
 * below z=0). Of course, this normalized point is scaled with the dimensions of
 * the screen to produce the actual location of the point on the screen, in
 * pixels. <br>
 * After each vertex has been projected onto the screen, the triangle must be
 * rasterized. Some simple comparisons are done with the three screen points to
 * obtain the rectangle bounding the triangle: the maximum and minimum x and y
 * coordinates of all three points are taken; these define the smallest AABB
 * containing the triangle. For each point on the screen in the rectangle, the
 * point on the screen is reverse-projected onto the view plane and a ray is
 * cast from the player through this point to determine the 3D location
 * represented by this point on the screen. This 3D location is crucial because
 * projective transformations are not affine; meaning that linear relationships
 * are not necessarily preserved. Example: The 3D location represented by the
 * midpoint of two projected points on the screen is not guaranteed to be the
 * midpoint of the two 3D points represented by the two projected points.
 * Computing the 3D location allows the rasterizer to determine two essential
 * things: <br>
 * - The depth value of the pixel <br>
 * - The perspective-correct barycentric weights (used in texture mapping) <br>
 * The barycentric weights of the 3D point with respect to the 3 vertices of the
 * triangle in world space are calculated. If any of them are negative, it means
 * that the 3D point being tested is not in the triangle, so the rasterizer
 * continues to the next point on the screen. If not, the depth value of the 3D
 * point is calculated and tested against the 2D z-buffer. If the point is
 * visible, the rasterizer scales each UV vector by their respective
 * barycentric weights and adds them to determine the UV texture coordinate for
 * that point. The color value for that point is obtained by scaling the
 * aforementioned UV coordinate by the dimensions of the texture. Of course, if
 * textures are disabled or the triangle does not have a texture, the triangle's
 * color is used instead.
 * </ol>
 * <br>
 * * A z-buffer is a solution to the visibility problem; it allows the renderer
 * to accurately draw concave or intersecting geometry. For the 2D elements of
 * the world, a 1D array is used as the z-buffer. For every stripe, the distance
 * from the player to whatever has been drawn at that stripe is stored in
 * <code>zbuf[x]</code>. When sprites are drawn, the distance of each stripe of
 * pixels in the sprite is tested against the z-buffer to determine if it is in
 * front of or behind whatever has been drawn there already. If it is closer, a
 * 2D z-buffer for 3D elements is updated. For each pixel in the stripe, if the
 * pixel is not transparent, the distance of the stripe is stored in
 * <code>zbuf2[x + y * WIDTH]</code> (zbuf2 is a 1D array representing a 2D
 * construct). When 3D elements are rendered after sprites have been drawn,
 * their depth values are calculated and tested against depth values in the 2D
 * z-buffer to determine visibility.
 *
 *
 * @author Joe Desmond
 */
public final class Raycaster extends JPanel {

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
	private volatile List<Entity> sprites;
	
	public static final float FULL_FOG_DISTANCE = 10f; // 5f
	public static final int OLD_SHADE_THRESHOLD = 100;
	public static final int SHADE_THRESHOLD = 200;
	public static final int TRUE_3D_SHADE_THRESHOLD = 75;
	
	protected int rendererCount;
	protected ThreadRenderer[] renderers;
	protected ThreadPoolExecutor executor;
	protected LatchRef latchref;
	protected double[] wallDistLUT;
	protected AtomicBoolean enabled = new AtomicBoolean(true);
	protected Game parentGame;
	protected int upDownEnabled = 0;
	public boolean finished = true;
	public ShadeType shadeType = ShadeType.QUADRATIC;
	private HUD hud;
	private ViewModel currentViewModel;
	private int dwightsKilled = 0;
	private List<Entity> hitEntities = new ArrayList<Entity>();
	private float closestWallAtCenter = 0;
	private List<Vector3> modelQueue = Collections.synchronizedList(new ArrayList<Vector3>());
	
	private float[] zbuf2;
	private int[] screen;

	private boolean true3DTexturesEnabled = true;
	
	private Vector2 goalPos = null;
	
	private boolean touchingGoal = false;
	private int winCount = 0;
	private int deathCount = 0;
	
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
		
		FINAL_ASPECT = FINAL_WIDTH / (float) FINAL_HEIGHT;
		
		ASPECT = WIDTH / (float) HEIGHT;

		HUD_TRUE_HEIGHT = HEIGHT;
		
		init();
	}
	
	private void init() {
		zbuf = new double[WIDTH];
		zbuf2 = new float[WIDTH * HEIGHT];
		screen = new int[WIDTH * HEIGHT];
		HALF_HEIGHT = HEIGHT / 2;
		camera.setVerticalMouselookLimit(HEIGHT / 8);
		
		resetZBuffer();
		populateWallDistLUT();
		generateDimensionLUTs();
		createThreadPoolRenderers();
		createFrustum();
	}
	
	public Raycaster setGoalPos(Vector2 _goalPos) {
		goalPos = _goalPos;
		return this;
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
		Arrays.parallelSetAll(screen, i -> 0);
	}
	
	// TODO: Remove this when we have a proper HUD
	private void drawDebugInfo() {
		g.setColor(Color.GREEN);
		g.drawString("x: " + pos.x, 5, 20);
		g.drawString("y: " + pos.y, 5, 35);
		
		g.drawString("Dwights killed: " + dwightsKilled, 5, 50);
		g.drawString("Wins: " + winCount, 5, 65);
		g.drawString("Deaths: " + deathCount, 5, 80);
	}
	
	private void drawCrosshair() {
		g.setColor(Color.GREEN);
		g.drawLine(FINAL_WIDTH / 2, (FINAL_HEIGHT / 2) - 10, FINAL_WIDTH / 2, (FINAL_HEIGHT / 2) + 10);
		g.drawLine((FINAL_WIDTH / 2) - 10, FINAL_HEIGHT / 2, (FINAL_WIDTH / 2) + 10, FINAL_HEIGHT / 2);
	}
	
	private void drawCompass() {
		if (goalPos != null) {
			float distance = Vector2.distance(goalPos, pos);
			
			if (distance <= 1) {
				touchingGoal = true;
			} else {
				touchingGoal = false;
			}
			
			Vector2 goal = new Vector2(goalPos.x - camera.pos.x, goalPos.y - camera.pos.y);
			float angle = RenderUtils.angleBetweenVectors(camera.dir, goal);
			
			if (RenderUtils.isLeftOfRay(pos,dir.add(pos),goalPos)) {
				angle = -angle;
			}
			
			angle += (float)(Math.PI/2.0f);
			
			int length = 45;
			int x = (int)(Math.cos(angle) * length);
			int y = (int)(Math.sin(angle) * length);
		
			int radius = 50;
		
			g.setColor(Color.GRAY);
			g.fillOval(FINAL_WIDTH-(radius*2), 0, radius*2, radius*2);
			g.fillRect(FINAL_WIDTH - 3*radius/2, 9*radius/4, radius, 2*radius/5);
			
			g.setColor(Color.BLACK);
			g.drawRect(FINAL_WIDTH - 3*radius/2, 9*radius/4, radius, 2*radius/5);
			g.drawOval(FINAL_WIDTH-(radius*2), 0, radius*2, radius*2);
			
			g.setColor(Color.GREEN);
			g.drawString(Integer.toString((int)distance), FINAL_WIDTH - 5*radius/4, 51*radius/20);
			g.drawLine(FINAL_WIDTH-radius, radius, FINAL_WIDTH-radius-x, radius-y);
		}
	}
	
	public boolean playerIsTouchingGoal() {
		return touchingGoal;
	}
	
	protected void finalizeRender() {
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		img.getRaster().setDataElements(0, 0, WIDTH, HEIGHT, screen);
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
	
	public boolean wonGame = false;

	protected void render() {		
		if (hud.getHealth() > 0) {
			preRender();
			parallelRender();
			renderSprites();
			renderAllVisibleModelsWithoutMatrices();
			drawOverlays();
			finalizeRender();
			// renderAllVisibleModelsWithMatrices();
			postRender();
		} else {
			drawDeathScreen();
		}
		
		if (wonGame) {
			drawWinScreen();
		}
	}
	
	protected void postRender() {
		saveClosestWallAtCenter();
		resetZBuffer();
		drawDebugInfo();
		drawCrosshair();
		drawCompass();
		finished = true;
	}
	
	protected void drawOverlays() {
		drawHUD();
		drawCurrentViewModel();
	}
	
	private void drawWinScreen() {
		if (Block.DwightElements.WIN != null) {
			resetImage();
			img = Block.DwightElements.WIN;
			finalizeRender();
		}
	}
	
	private void drawDeathScreen() {
		if (Block.DwightElements.DEATH != null) {
			resetImage();
			img = Block.DwightElements.DEATH;
			finalizeRender();
		}
	}
	
	private void saveClosestWallAtCenter() {
		closestWallAtCenter = (float) zbuf[WIDTH / 2];
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
				zbuf2[i + j * WIDTH] = Float.POSITIVE_INFINITY;
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
	
	public void addWin() {
		winCount++;
	}
	
	public void addDeath() {
		deathCount++;
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
			// System.out.println(i + " " + sprites.get(i).distance);
			
			Entity active = sprites.get(i);
			
			Vector2 spriteVector = active.pos;
			double spriteX = spriteVector.x - pos.x;
			double spriteY = spriteVector.y - pos.y;

			// Faster than using Matrix2 class
			double invDet = 1.0 / (plane.x * dir.y - dir.x * plane.y);
			double transformX = invDet * (dir.y * spriteX - dir.x * spriteY);
			double transformY = invDet * (-plane.y * spriteX + plane.x * spriteY);

			int spriteScreenX = (int) ((WIDTH >> 1) * (1 + transformX / transformY));

			int spriteHeight = Math.abs((int) ((HEIGHT / transformY) * FINAL_ASPECT));
			int drawStartY = -(spriteHeight >> 1) + (HEIGHT >> 1);
			if (drawStartY < 0) {
				drawStartY = 0;
			}
			int drawEndY = (spriteHeight >> 1) + (HEIGHT >> 1);
			if (drawEndY >= HEIGHT) {
				drawEndY = HEIGHT - 1;
			}

			int spriteWidth = Math.abs((int) (HEIGHT / transformY));
			int drawStartX = -(spriteWidth >> 1) + spriteScreenX;
			if (drawStartX < 0) {
				drawStartX = 0;
			}
			int drawEndX = (spriteWidth >> 1) + spriteScreenX;
			if (drawEndX >= WIDTH) {
				drawEndX = WIDTH - 1;
			}

			if (drawStartX < WIDTH / 2 && drawEndX > WIDTH / 2) {
				hitEntities.add(active);
			}

			SquareTexture texture = active.getActiveTexture();

			int texWidth = texture.SIZE;
			int texHeight = texture.SIZE;
			for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
				int texX = ((stripe - ((-spriteWidth >> 1) + spriteScreenX)) << 8) * texWidth / spriteWidth >> 8;
				if (transformY > 0 && stripe > 0 && stripe < WIDTH && transformY < zbuf[stripe]) {

					for (int y = drawStartY; y < drawEndY; y++) {
						int d = (y << 8) - (HEIGHT << 7) + (spriteHeight << 7);
						int texY = ((d * texHeight) / spriteHeight) >> 8;

						int index = texX + texWidth * texY;
						// If the index is out of bounds, black is drawn
						int color = index < texture.pixels.length && index >= 0 ? texture.pixels[index] : 0;

						if (color != active.alpha) {

							zbuf2[stripe + y * WIDTH] = (float)transformY;

							//img.setRGB(stripe, y, shade(active.distance, color));
							screen[stripe + y * WIDTH] = shade(active.distance,color);
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

				boolean deflected = false;

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
				
				// Modified DDA that switches to slower ray/line segment intersection tests when
				// encountering a custom Block
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
					if (block == Block.DEFLECTOR) {
						/*
						 * if (!deflected) { double temp = deltaDistX;
						 * 
						 * deltaDistX = deltaDistY; deltaDistY = temp; deflected = true; }
						 */
						deflected = true;
					} else if (block.isVisible() && (block.getProximity() == -1
							|| block.getProximity() > Vector2.distance(pos, new Vector2(mapX, mapY)))) {
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
									float normDist = Vector2.distance(testing.v0, tested) / wallLength;
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

				/*
				 * DRUGS: perpWallDist -= (Math.random() * perpWallDist * 0.1);
				 */

				if (deflected && sideHit == Side.POSX) {
					// perpWallDist -= (Math.random() * perpWallDist * 0.05);

					perpWallDist *= 2.0f;
				}

				lineHeight = (int) ((HEIGHT / (perpWallDist)) * FINAL_ASPECT);

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

				textureFloorAndCeiling(x);
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
			/**
			 * Used to determine EXACTLY where the ray hit and set both z-buffers properly
			 * so that true 3D rendering works correctly
			 */
			Vector2 exact;

			if (side) {
				wallX = (pos.x + ((adjMapY - pos.y + (1 - adjStepY) / 2) / rdiry) * rdirx);
				
				if (sideHit == Side.POSY) {
					exact = new Vector2((float) wallX, mapY + 1);
				} else {
					exact = new Vector2((float) wallX, mapY);
				}

			} else {
				wallX = (pos.y + ((adjMapX - pos.x + (1 - adjStepX) / 2) / rdirx) * rdiry);

				if (sideHit == Side.POSX) {
					exact = new Vector2(mapX + 1, (float) wallX);
				} else {
					exact = new Vector2(mapX, (float) wallX);
				}
			}

			float trueDistance = Vector2.distance(pos, exact);

			zbuf[x] = trueDistance;
			
			for (int y1 = 0; y1 < HEIGHT; y1++) {
				zbuf2[x + y1 * WIDTH] = trueDistance;
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

				//img.setRGB(x, y, shade(trueDistance, color));
				screen[x + y * WIDTH] = shade(trueDistance,color);
			}
		}
		
		private void textureCustomBlock(int x) {
			wallX = hitWall.getNorm(customHit);

			wallX -= Math.floor(wallX);

			float trueDistance = Vector2.distance(pos, customHit);

			zbuf[x] = trueDistance;
			
			for (int y1 = 0; y1 < HEIGHT; y1++) {
				zbuf2[x + y1 * WIDTH] = trueDistance;
			}

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

				//img.setRGB(x, y, shade((float) perpWallDist, color));
				screen[x + y * WIDTH] = shade((float)perpWallDist,color);
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

				//img.setRGB(x, y, shade((float) currentDist, color));
				screen[x + y * WIDTH] = shade((float) currentDist, color);
				//img.setRGB(x, HEIGHT - y, shade((float) currentDist, ceilColor));
				screen[x + (HEIGHT - y) * WIDTH] = shade((float) currentDist, ceilColor);
			}
		}
	}
	
	private int shade(float distance, int color) {
		float darkenBy;

		switch (shadeType) {
			case LINEAR:
				float normValue = distance / FULL_FOG_DISTANCE;
				
				darkenBy = (normValue >= 1 ? 1 : normValue) * SHADE_THRESHOLD;
				break;
			
			case QUADRATIC:
				float _x = distance >= FULL_FOG_DISTANCE ? FULL_FOG_DISTANCE : distance;
				float a = SHADE_THRESHOLD / 100.0f;
				float b = 2 * FULL_FOG_DISTANCE;
				darkenBy = (-(a * _x) * (_x - b));
				break;
			
			case NONE:
			default:
				darkenBy = 0;
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

	public enum ShadeType {
		NONE, LINEAR, QUADRATIC
	}

	public void setShadeType(ShadeType type) {
		shadeType = type;
	}
	
	// positive z points up
	private void OLD_renderAllVisibleModels() {
		Map<Vector3, Vector3> renderedVertices = new HashMap<Vector3, Vector3>();
		
		Vector3 pos3D = new Vector3(camera.pos.x, 0.0f, camera.pos.y);
		
		Vector3 cameraPos = new Vector3(camera.pos.x, 0.5f, camera.pos.y);
		// System.out.println("camerapos: " + cameraPos);
		
		Vector2 plane = camera.pos.add(camera.dir).add(camera.plane);
		// System.out.println("plane: " + plane);
		Vector2 negplane = camera.pos.add(camera.dir).add(new Vector2(-camera.plane.x, -camera.plane.y));
		// System.out.println("negplane: " + negplane);
		
		Vector3 plane0 = new Vector3(plane.x, 0.0f, plane.y);
		Vector3 plane1 = new Vector3(plane.x, 1.0f, plane.y);
		Vector3 plane2 = new Vector3(negplane.x, 0.5f, negplane.y);
		
		Triangle viewPlane = new Triangle(plane0, plane2, plane1);

		for (Vector3 location : modelQueue) {
			Block block = world.getBlockAt((int) location.x, (int) location.z);
			
			for (Triangle t : block.model.triangles) {
				Vector3 v0 = t.v0.plus(location);
				Vector3 v1 = t.v1.plus(location);
				Vector3 v2 = t.v2.plus(location);
				
				Line l0 = new Line(cameraPos, v0);
				Line l1 = new Line(cameraPos, v1);
				Line l2 = new Line(cameraPos, v2);
				
				Vector3 hit0;
				Vector3 hit1;
				Vector3 hit2;
				
				if (!renderedVertices.containsKey(v0)) {
					hit0 = RenderUtils.linePlaneIntersection(l0, viewPlane);
					// System.out.println("v0: " + v0);
					// System.out.println("hit0: " + hit0);
					renderedVertices.put(v0, hit0);
				} else {
					hit0 = renderedVertices.get(v0);
				}
				
				if (!renderedVertices.containsKey(v1)) {
					hit1 = RenderUtils.linePlaneIntersection(l1, viewPlane);
					renderedVertices.put(v1, hit1);
					// System.out.println("hit1: " + hit1);
				} else {
					hit1 = renderedVertices.get(v1);
				}
				
				if (!renderedVertices.containsKey(v2)) {
					hit2 = RenderUtils.linePlaneIntersection(l2, viewPlane);
					renderedVertices.put(v2, hit2);
				} else {
					hit2 = renderedVertices.get(v2);
				}
				
				// System.out.println(hit2);
				
				Vector2 screen0 = findPointOnScreen(hit0);
				Vector2 screen1 = findPointOnScreen(hit1);
				Vector2 screen2 = findPointOnScreen(hit2);

				// System.out.println("screen0: " + screen0);
				// System.out.println("screen1: " + screen1);
				// System.out.println("screen2: " + screen2);
				/*
				 * g.setColor(new Color(t.debug_color)); int[] xPoints = {(int)screen0.x,
				 * (int)screen1.x, (int)screen2.x}; int[] yPoints = {(int)screen0.y,
				 * (int)screen1.y, (int)screen2.y}; g.fillPolygon(xPoints, yPoints, 3);
				 */
				// rasterizer.rasterizeTriangle(screen0, screen1, screen2, t.debug_color);
			}
		}
	}

	Frustum frustum2;

	private void createFrustum() {
		camera.plane.updateLength();
		camera.dir.updateLength();

		float dim = 0.816f;

		float top = dim;
		float bottom = 0;
		float left = 0;
		float right = dim;
		float near = camera.dir.length;
		float far = GameConstants.TRUE_3D_MAX_RENDER_DISTANCE;

		frustum = new Frustum(top, bottom, left, right, near, far);
	}

	private void createFrustum2() {
		camera.plane.updateLength();
		camera.dir.updateLength();
		System.out.println(camera.dir.length);

		float top = 1f;
		float bottom = 0.0f;
		float left = -0.5f;
		float right = 0.5f;
		float near = 0.75f;
		float far = GameConstants.TRUE_3D_MAX_RENDER_DISTANCE;

		frustum = new Frustum(top, bottom, left, right, near, far);
	}

	private static final Vector2 Y_AXIS = new Vector2(0, 1);
	private static final float FLOAT_PI = (float) Math.PI;
	private Frustum frustum;

	private void renderAllVisibleModelsWithMatrices() {
		Map<Vector3, Vector3> renderedVertices = new HashMap<Vector3, Vector3>();

		Vector3 cameraInWorld = new Vector3(camera.pos.x, camera.pos.y, 0.5f);
		Vector3 leftFoV = new Vector3(camera.pos.x + camera.dir.x - camera.plane.x,
				camera.pos.y + camera.dir.y - camera.plane.y, 0.5f);
		Vector3 rightFoV = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x,
				camera.pos.y + camera.dir.y + camera.plane.y, 0.5f);

		Matrix4 translator = Transformer.createTranslationMatrix(-cameraInWorld.x, -cameraInWorld.y, -cameraInWorld.z);

		// float psi = RenderUtils.altAngleBetweenLines(Y_AXIS, new
		// Wall(0,0,camera.dir.x,camera.dir.y));
		float psi = RenderUtils.angleBetweenVectors(Y_AXIS, camera.dir);

		if (!RenderUtils.isLeftOfRay(new Vector2(0, 0), Y_AXIS, camera.dir)) {
			psi = -psi;
		}

		Matrix4 rotator = Transformer.createZRotationMatrix(-psi);

		/**
		 * After this transformation, camera should be looking at positive Y from the
		 * origin
		 */
		Matrix4 viewMatrix = translator.multiply(rotator);

		Matrix4 projectionMatrix = Transformer.createYDepthProjectionMatrix(frustum);

		Matrix4 transformation = viewMatrix.multiply(projectionMatrix);

		final float zTranslate = (FINAL_ASPECT - 1) / 2;

		for (Vector3 location : modelQueue) {
			Block block = world.getBlockAt((int) location.x, (int) location.y);

			for (Triangle t : block.model.triangles) {
				// These three Vectors are the triangle's vertices in model space
				Vector3 _v0 = new Vector3(t.v0.x, t.v0.y, t.v0.z);
				Vector3 _v1 = new Vector3(t.v1.x, t.v1.y, t.v1.z);
				Vector3 _v2 = new Vector3(t.v2.x, t.v2.y, t.v2.z);
				
				// The vertices are scaled to fit with the final aspect ratio
				_v0.z = (t.v0.z * FINAL_ASPECT) - zTranslate;
				_v1.z = (t.v1.z * FINAL_ASPECT) - zTranslate;
				_v2.z = (t.v2.z * FINAL_ASPECT) - zTranslate;

				_v0.y = (t.v0.y * FINAL_ASPECT) - zTranslate;
				_v1.y = (t.v1.y * FINAL_ASPECT) - zTranslate;
				_v2.y = (t.v2.y * FINAL_ASPECT) - zTranslate;

				_v0.x = (t.v0.x * FINAL_ASPECT) - zTranslate;
				_v1.x = (t.v1.x * FINAL_ASPECT) - zTranslate;
				_v2.x = (t.v2.x * FINAL_ASPECT) - zTranslate;

				Vector3 v0 = location.plus(_v0);
				Vector3 v1 = location.plus(_v1);
				Vector3 v2 = location.plus(_v2);

				if (!(isInFoV(v0, leftFoV, rightFoV, cameraInWorld) || isInFoV(v1, leftFoV, rightFoV, cameraInWorld)
						|| isInFoV(v2, leftFoV, rightFoV, cameraInWorld))) {
					continue;
				}

				Vector3 h0 = transformation.transform(v0);
				Vector3 h1 = transformation.transform(v1);
				Vector3 h2 = transformation.transform(v2);

				// System.out.println("before scale: " + s0);

				Vector3 s0 = h0.scale(1 / h0.w);
				Vector3 s1 = h1.scale(1 / h1.w);
				Vector3 s2 = h2.scale(1 / h2.w);

				Vector2 t0 = findOnScreen(frustum, s0);
				Vector2 t1 = findOnScreen(frustum, s1);
				Vector2 t2 = findOnScreen(frustum, s2);

				// System.out.println("after scale: " + s0);

				// rasterizer.affineRaster(t0, t1, t2, t, v0, v1, v2, cameraInWorld);
				g.setColor(new Color(t.color));

				int[] xPoints = {(int) t0.x, (int) t1.x, (int) t2.x};
				int[] yPoints = {(int) t0.y, (int) t1.y, (int) t2.y};

				g.fillPolygon(xPoints, yPoints, 3);

				// System.out.println(s0);
			}
		}
	}
	
	private Vector2 findOnScreen(Frustum f, Vector3 v) {
		int x = WIDTH - (int) (((v.x - f.left) / (f.right - f.left)) * WIDTH);
		int y = HEIGHT - (int) (((v.z - f.bottom) / (f.top - f.bottom)) * HEIGHT);

		return new Vector2(x, y);
	}
	
	private class GPURasterizer extends Kernel {
		int[] v0;
		int[] v1;
		int[] v2;
		
		final float[] uv0 = new float[] {0,0};
		final float[] uv1 = new float[] {0,0};
		final float[] uv2 = new float[] {0,0};
		int[] texture;
		int texWidth;
		int texHeight;
		
		final float[] v03 = new float[] {0,0,0};
		final float[] v13 = new float[] {0,0,0};
		final float[] v23 = new float[] {0,0,0};
		final float[] cameraPos = new float[] {0,0,0};
		final float[] cameraDir = new float[] {0,0};
		final float[] cameraPlane = new float[] {0,0};
		int darkenBy;
		int color;

		final float[] bv0 = new float[] {0,0,0};
		final float[] bv1 = new float[] {0,0,0};
		float d00;
		float d01;
		float d11;
		float invDenom;
		
		int HUD_TRUE_HEIGHT;
		int WIDTH;
		int HEIGHT;
		int startY;
		int minX;
		int maxX;
		
		int shadeType;
		
		boolean true3DTexturesEnabled;
		boolean uv0Null = false;
		boolean nullFlag = false;
		
		float FULL_FOG_DISTANCE;
		int SHADE_THRESHOLD;
		
		int[] screen;
		
		public void set(Vector2 _v0, Vector2 _v1, Vector2 _v2, Triangle _triangle, Vector3 _v03, Vector3 _v13, Vector3 _v23, Vector3 _camera, Vector2 _plane, Vector2 _dir, int _HUD_TRUE_HEIGHT, int _WIDTH, int _HEIGHT, int _startY, int _minX, int _maxX, boolean _true3DTexturesEnabled, int shadeType, float _FULL_FOG_DISTANCE, int _SHADE_THRESHOLD, int[] _screen) {
			v0 = new int[] {(int) _v0.x, (int) _v0.y};
			v1 = new int[] {(int) _v1.x, (int) _v1.y};
			v2 = new int[] {(int) _v2.x, (int) _v2.y};
			
			if (_triangle.uv0 == null) {
				uv0Null = true;
			} else {
				uv0[0] = _triangle.uv0.x;
				uv0[1] = _triangle.uv0.y;
				
				uv1[0] = _triangle.uv1.x;
				uv1[1] = _triangle.uv1.y;
				
				uv2[0] = _triangle.uv2.x;
				uv2[1] = _triangle.uv2.y;
				
				texture = _triangle.texture.pixels;
				texWidth = _triangle.texture.width;
				texHeight = _triangle.texture.height;
			}	
			
			bv0[0] = _triangle.bv0.x;
			bv0[1] = _triangle.bv0.y;
			bv0[2] = _triangle.bv0.z;
			
			bv1[0] = _triangle.bv1.x;
			bv1[1] = _triangle.bv1.y;
			bv1[2] = _triangle.bv1.z;
			
			d00 = _triangle.d00;
			d01 = _triangle.d01;
			d11 = _triangle.d11;
			invDenom = _triangle.invDenom;
			
			darkenBy = _triangle.darkenBy;
			color = _triangle.color;
			
			v03[0] = _v03.x;
			v03[1] = _v03.y;
			v03[2] = _v03.z;
			
			v13[0] = _v13.x;
			v13[1] = _v13.y;
			v13[2] = _v13.z;
			
			v23[0] = _v23.x;
			v23[1] = _v23.y;
			v23[2] = _v23.z;
			
			cameraPos[0] = _camera.x;
			cameraPos[1] = _camera.y;
			cameraPos[2] = _camera.z;
			
			cameraDir[0] = _dir.x;
			cameraDir[1] = _dir.y;
			
			cameraPlane[0] = _plane.x;
			cameraPlane[1] = _plane.y;
			
			HUD_TRUE_HEIGHT = _HUD_TRUE_HEIGHT;
			WIDTH = _WIDTH;
			HEIGHT = _HEIGHT;
			startY = _startY;
			minX = _minX;
			maxX = _maxX;
			
			true3DTexturesEnabled = _true3DTexturesEnabled;
			
			FULL_FOG_DISTANCE = _FULL_FOG_DISTANCE;
			SHADE_THRESHOLD = _SHADE_THRESHOLD;
			screen = _screen;
		}
		
		private boolean tryDrawn = false;
		
		@Override
		public void run() {
			int y = startY + getGlobalId();
			/*
			boolean drawn = false;
			for (int x = minX; x <= maxX; x++) {
				tryDrawn = tryDraw(x,y);
				
				if (tryDrawn) {
					drawn = true;
				}
				if (tryDrawn && !drawn) {
					tryDrawn = false;
					return;
				}
			}
			*/	
		}
		
		
		private float dot(float[] a, float[] b) {
			return (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]);
		}
		
		
		final float[] crossR = new float[] {0,0,0};
		private void cross(float[] a, float[] b) {
			float _x = (a[1] * b[2]) - (a[2] * b[1]);
			float _y = (a[2] * b[0]) - (a[0] * b[2]);
			float _z = (a[0] * b[1]) - (a[1] * b[0]);
			
			crossR[0] = _x;
			crossR[1] = _y;
			crossR[2] = _z;
		}
		
		final float[] scale3R = new float[] {0,0,0};
		private void scale3(float[] v, float f) {
			scale3R[0] = v[0] * f;
			scale3R[1] = v[1] * f;
			scale3R[2] = v[2] * f;
		}
		
		final float[] scale2R = new float[] {0,0};
		private void scale2(float[] v, float f) {
			scale2R[0] = v[0] * f;
			scale2R[1] = v[1] * f;
		}
		
		final float[] normalR = new float[] {0,0,0}; //Result
		final float[] term0 = new float[] {0,0,0};
		final float[] term1 = new float[] {0,0,0};
		private void getNormal(float[] t0, float[] t1, float[] t2) {
			
			term0[0] = t1[0] - t0[0];
			term0[1] = t1[1] - t0[1];
			term0[2] = t1[2] - t0[2];
			
			term1[0] = t2[0] - t0[0];
			term1[1] = t2[1] - t0[1];
			term1[2] = t2[2] - t0[2];
			
			cross(term0, term1);
			
			normalR[0] = crossR[0];
			normalR[1] = crossR[1];
			normalR[2] = crossR[2];
		}
		
		final float[] lPIR = new float[] {0,0,0}; //Result
		final float[] lpp0 = new float[] {0,0,0};
		final float[] lpp1 = new float[] {0,0,0};
		final float[] p_co = new float[] {0,0,0};
		final float[] p_no = new float[] {0,0,0};
		final float[] lpu = new float[] {0,0,0};
		final float[] lpw = new float[] {0,0,0};
		private void linePlaneIntersection(float[] l0, float[] l1, float[] plane0, float[] plane1, float[] plane2) {
			lpp0[0] = l0[0];
			lpp0[1] = l0[1];
			lpp0[2] = l0[2];
			
			lpp1[0] = l1[0];
			lpp1[1] = l1[1];
			lpp1[2] = l1[2];
			
			p_co[0] = plane0[0];
			p_co[1] = plane0[1];
			p_co[2] = plane0[2];
			
			getNormal(plane0, plane1, plane2);
			p_no[0] = normalR[0];
			p_no[1] = normalR[1];
			p_no[2] = normalR[2];
			
			final float epsilon = 0.000001f;
			
			lpu[0] = lpp0[0] - lpp1[0];
			lpu[1] = lpp0[1] - lpp1[1];
			lpu[2] = lpp0[2] - lpp1[2];
			
			float dot = dot(p_no, lpu);
			
			if (super.abs(dot) > epsilon) {
				lpw[0] = lpp0[0] - p_co[0];
				lpw[1] = lpp1[0] - p_co[1];
				lpw[2] = lpp0[2] - p_co[2];
				
				float fac = -dot(p_no, lpw) / dot;
				
				if (fac > 0) {
					nullFlag = true;
					return;
				}
				
				scale3(lpu,fac);
				
				lpu[0] = scale3R[0];
				lpu[1] = scale3R[1];
				lpu[2] = scale3R[2];
				
				lPIR[0] = lpp0[0] + lpu[0];
				lPIR[1] = lpp0[1] + lpu[1];
				lPIR[2] = lpp0[2] + lpu[2];
				return;
			}
			
			nullFlag = true;
		}
		
		private float distance(float[] v0, float[] v1) {
			return super.sqrt(((v0[0] - v1[0]) * (v0[0] - v1[0])) + ((v0[1] - v1[1]) * (v0[1] - v1[1])) + ((v0[2] - v1[2]) * (v0[2] - v1[2])));
		}
		
		private final float[] inWorld = new float[] {0,0,0};
		private final float[] intersected = new float[] {0,0,0};
		private final float[] weights = new float[] {0,0,0};
		
		private final float[] uv00 = new float[] {0,0};
		private final float[] uv10 = new float[] {0,0};
		private final float[] uv20 = new float[] {0,0};
		
		private final float[] normTexCoord = new float[] {0,0};
		private boolean tryDraw(int x, int y) {
			
			reverseProject(x,y);
			inWorld[0] = reverseProjectR[0];
			inWorld[1] = reverseProjectR[1];
			
			linePlaneIntersection(cameraPos, inWorld, v03, v13, v23);
			
			intersected[0] = lPIR[0];
			intersected[1] = lPIR[1];
			intersected[2] = lPIR[2];

			if (!nullFlag) {
				computeBarycentricWeights(intersected);
				weights[0] = baryWeights[0];
				weights[1] = baryWeights[1];
				weights[2] = baryWeights[2];

				if (!nullFlag) {
					float distance = distance(intersected, cameraPos);
					int col = color;
					
					if (distance < zbuf2[x + y * WIDTH]) {
						zbuf2[x + y * WIDTH] = distance;

						if (!uv0Null && true3DTexturesEnabled) {
							scale2(uv0, weights[0]);
							uv00[0] = scale2R[0];
							uv00[1] = scale2R[1];
							
							scale2(uv1, weights[1]);
							uv10[0] = scale2R[0];
							uv10[1] = scale2R[1];
							
							scale2(uv2, weights[2]);
							uv20[0] = scale2R[0];
							uv20[1] = scale2R[1];
							
							normTexCoord[0] = uv00[0] + uv10[0] + uv20[0];
							normTexCoord[1] = uv00[1] + uv10[1] + uv20[1];
							
							int texX = (int) (normTexCoord[0] * texWidth);
							int texY = (int) (normTexCoord[1] * texHeight);

							int index = texX + texY * texWidth;

							if (index < texture.length) {
								col = texture[index];
							}
						}
						
						int red = (col >> 16) & 0xFF;
						int green = (col >> 8) & 0xFF;
						int blue = col & 0xFF;
						
						red -= (red - darkenBy >= 0) ? darkenBy : red;
						green -= (green - darkenBy >= 0) ? darkenBy : green;
						blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
						
						col = (red << 16) | (green << 8) | blue;

						//img.setRGB(x, y, shade(distance, color));
						screen[x + y * WIDTH] = shade(distance,col);
					}
					
					return true;
				}
			}
			
			nullFlag = false;
			return false;
		}
		
		private int shade(float distance, int color) {
			float darkenBy;

			switch (shadeType) {
				case 1:
					float normValue = distance / FULL_FOG_DISTANCE;
					
					darkenBy = (normValue >= 1 ? 1 : normValue) * SHADE_THRESHOLD;
					break;
				
				case 2:
					float _x = distance >= FULL_FOG_DISTANCE ? FULL_FOG_DISTANCE : distance;
					float a = SHADE_THRESHOLD / 100.0f;
					float b = 2 * FULL_FOG_DISTANCE;
					darkenBy = (-(a * _x) * (_x - b));
					break;
				
				case 0:
				default:
					darkenBy = 0;
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
		
		private final float[] v2bary = new float[] {0, 0, 0};
		private final float[] baryWeights = new float[] {0, 0, 0};
		
		private void computeBarycentricWeights(float[] v) {
			
			v2bary[0] = v[0] - bv0[0];
			v2bary[1] = v[1] - bv0[1];
			v2bary[2] = v[2] - bv0[2];
			
			float d20 = dot(v2bary, bv0);
			float d21 = dot(v2bary, bv1);

			float w1 = (d11 * d20 - d01 * d21) * invDenom;

			if (w1 < 0) {
				nullFlag = true;
				return;
			}

			float w2 = (d00 * d21 - d01 * d20) * invDenom;

			if (w2 < 0) {
				nullFlag = true;
				return;
			}

			float w0 = 1.0f - w1 - w2;

			if (w0 < 0) {
				nullFlag = true;
				return;
			} else {
				baryWeights[0] = w0;
				baryWeights[1] = w1;
				baryWeights[2] = w2;
			}
		}
		
		private final float[] reverseProjectR = new float[] {0,0,0};
		private void reverseProject(int x, int y) {
			float xNorm = (2 * x) / ((float) WIDTH) - 1.0f;

			float rdirx = cameraDir[0] + cameraPlane[0] * xNorm;
			float rdiry = cameraDir[1] + cameraPlane[1] * xNorm;

			float zNorm = 1 - (y / (float) HEIGHT);
			
			reverseProjectR[0] = rdirx + cameraPos[0];
			reverseProjectR[1] = rdiry + cameraPos[1];
			reverseProjectR[2] = zNorm;
		}
	}
	
	private class Rasterizer2 {
		Vector2 v0;
		Vector2 v1;
		Vector2 v2;
		Triangle triangle;
		Vector3 v03;
		Vector3 v13;
		Vector3 v23;
		Vector3 camera;
		int darkenBy;
		
		public void set(Vector2 _v0, Vector2 _v1, Vector2 _v2, Triangle _triangle, Vector3 _v03, Vector3 _v13, Vector3 _v23, Vector3 _camera) {
			v0 = _v0;
			v1 = _v1;
			v2 = _v2;
			triangle = _triangle;
			darkenBy = triangle.darkenBy;
			v03 = _v03;
			v13 = _v13;
			v23 = _v23;
			camera = _camera;
		}
		
		private class Rect {
			Vector2 v0;
			Vector2 v1;
			Vector2 v2;
			Vector2 v3;
			
			public Rect(Vector2 _v0, Vector2 _v2) {
				v0 = _v0;
				v2 = _v2;
				
				v1 = new Vector2(v2.x,v0.y);
				v3 = new Vector2(v0.x,v2.y);
			}
		}
		
		private void perspectiveCorrectScanlineRaster() {
			int minX = (int) Math.min(v0.x, Math.min(v1.x, v2.x));
			int maxX = (int) Math.max(v0.x, Math.max(v1.x, v2.x));
			
			int minY = (int) Math.min(v0.y, Math.min(v1.y, v2.y));
			int maxY = (int) Math.max(v0.y, Math.max(v1.y, v2.y));
			
			if (minY < 0) {
				minY = 0;
			} else if (minY >= HUD_TRUE_HEIGHT) {
				return;
			}
			
			if (maxY >= HUD_TRUE_HEIGHT) {
				maxY = HUD_TRUE_HEIGHT-1;
			} else if (maxY < 0) {
				return;
			}
			
			if (minX < 0) {
				minX = 0;
			} else if (minX >= WIDTH) {
				return;
			}
			
			if (maxX >= WIDTH) {
				maxX = WIDTH-1;
			} else if (maxX < 0) {
				return;
			}
			
			for (int y = minY; y <= maxY; y++) {
				rasterizeLine(y,minX,maxX);
			}
		}
		
		private void rasterizeLine(int y, int minX, int maxX) {			
			boolean drawn = false;
			for (int x = minX; x <= maxX; x++) {
				Vector2 s0 = new Vector2(x,y);
				
				if (tryDraw(s0)) {
					drawn = true;
				} else {
					if (drawn) {
						return;
					}
				}
			}
		}
		
		private List<Rect> subdivide(int subdivisions, List<Rect> rects) {
			if (subdivisions <= 0) {
				return rects;
			}
			
			if (rects.size() > 0) {
				Rect r = rects.get(0);
				Vector2 v0 = r.v0;
				Vector2 v1 = r.v2;
				
				if (v0.x - v1.x <= 2) {
					return rects;
				}
			}
			
			for (int i = rects.size() - 1; i >= 0; i--) {
				Rect rect = rects.remove(i);
				Vector2 v0 = rect.v0;
				Vector2 v1 = rect.v1;
				Vector2 v2 = rect.v2;
				Vector2 v3 = rect.v3;
				
				Vector2 center = new Vector2(v1.x - v0.x, v3.y - v1.y);
				Vector2 topCenter = new Vector2(center.x, v0.y);
				Vector2 bottomCenter = new Vector2(center.x, v3.y);
				Vector2 leftCenter = new Vector2(v0.x, center.y);
				Vector2 rightCenter = new Vector2(v1.x, center.y);
				
				boolean centerHit = tryDraw(center);
				boolean topLeftHit = tryDraw(v0);
				boolean topRightHit = tryDraw(v1);
				boolean bottomRightHit = tryDraw(v2);
				boolean bottomLeftHit = tryDraw(v3);
				
				boolean topCenterHit = tryDraw(topCenter);
				boolean bottomCenterHit = tryDraw(bottomCenter);
				boolean leftCenterHit = tryDraw(leftCenter);
				boolean rightCenterHit = tryDraw(rightCenter);
				
				if (topLeftHit || topCenterHit || leftCenterHit || centerHit) {
					rects.add(new Rect(v0,center));
				}
				
				if (topCenterHit || topRightHit || rightCenterHit || centerHit) {
					rects.add(new Rect(topCenter,rightCenter));
				}
				
				if (centerHit || rightCenterHit || bottomRightHit || bottomCenterHit) {
					rects.add(new Rect(center,v2));
				}
				
				if (leftCenterHit || centerHit || bottomCenterHit || bottomLeftHit) {
					rects.add(new Rect(leftCenter,bottomCenter));
				}				
			}
			
			return subdivide(subdivisions-1,rects);
		}
		
		private void perspectiveCorrectSubdivisionRaster() {
			int minX = (int) Math.min(v0.x, Math.min(v1.x, v2.x));
			int maxX = (int) Math.max(v0.x, Math.max(v1.x, v2.x));
			
			int minY = (int) Math.min(v0.y, Math.min(v1.y, v2.y));
			int maxY = (int) Math.max(v0.y, Math.max(v1.y, v2.y));
			
			Vector2 topLeft = new Vector2(minX,minY);
			Vector2 bottomRight = new Vector2(maxX,maxY);
			Rect drawSpace = new Rect(topLeft,bottomRight);
			
			List<Rect> rects = new ArrayList<Rect>();
			rects.add(drawSpace);
			
			rects = subdivide(8,rects);
		}
		
		private boolean tryDraw(Vector2 onScreen) {
			int x = (int) onScreen.x;
			int y = (int) onScreen.y;
			
			Vector3 inWorld = reverseProject(onScreen);
			
			Line line = new Line(camera, inWorld);
			Vector3 intersected = RenderUtils.linePlaneIntersection(line, triangle);

			if (intersected != null) {
				Vector3 weights = computeBarycentricWeights(intersected);

				if (weights != null) {
					float distance = Vector3.distance(intersected, camera);

					if (distance < zbuf2[x + y * WIDTH]) {
						zbuf2[x + y * WIDTH] = distance;

						int color = triangle.color;

						if (triangle.uv0 != null && true3DTexturesEnabled) {
							Vector2 uv0 = triangle.uv0.scale(weights.x);
							Vector2 uv1 = triangle.uv1.scale(weights.y);
							Vector2 uv2 = triangle.uv2.scale(weights.z);

							Vector2 normTexCoord = uv0.add(uv1).add(uv2);

							int texX = (int) (normTexCoord.x * triangle.texture.width);
							int texY = (int) (normTexCoord.y * triangle.texture.height);

							int index = texX + texY * triangle.texture.width;

							if (index < triangle.texture.pixels.length) {
								color = triangle.texture.pixels[index];
							}
						}
						
						int red = (color >> 16) & 0xFF;
						int green = (color >> 8) & 0xFF;
						int blue = color & 0xFF;
						
						red -= (red - darkenBy >= 0) ? darkenBy : red;
						green -= (green - darkenBy >= 0) ? darkenBy : green;
						blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
						
						color = (red << 16) | (green << 8) | blue;

						//img.setRGB(x, y, shade(distance, color));
						screen[x + y * WIDTH] = shade(distance,color);
					}
					
					return true;
				}
			}
			
			return false;
		}
		
		private Vector3 reverseProject(Vector2 v) {
			float xNorm = (2 * v.x) / ((float) WIDTH) - 1.0f;

			float rdirx = dir.x + plane.x * xNorm;
			float rdiry = dir.y + plane.y * xNorm;

			float zNorm = 1 - (v.y / (float) HEIGHT);

			Vector3 inWorld = new Vector3(rdirx + camera.x, rdiry + camera.y, zNorm);
			
			return inWorld;
		}

		private void perspectiveCorrectRaster() {

			VectorAssociate a0 = new VectorAssociate(v0, v03);
			VectorAssociate a1 = new VectorAssociate(v1, v13);
			VectorAssociate a2 = new VectorAssociate(v2, v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 8563060661357225947L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			ySorted.sort((a, b) -> (int) (a.v2.y - b.v2.y));
			
			List<VectorAssociate> xSorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 1240112325831393490L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			xSorted.sort((a, b) -> (int) (a.v2.x - b.v2.x));

			int startX = (int) xSorted.get(0).v2.x;
			int endX = (int) xSorted.get(2).v2.x;
			
			int startY = (int) ySorted.get(0).v2.y;
			int endY = (int) ySorted.get(2).v2.y;
			
			int darkenBy = triangle.darkenBy;
			
			for (int y = startY; y <= endY; y++) {
				if (y >= 0 && y < HUD_TRUE_HEIGHT) {
					for (int x = startX; x <= endX; x++) {
						if (x >= 0 && x < WIDTH) {
							Vector3 inWorld = reverseProject(new Vector2(x,y));
							
							Line line = new Line(camera, inWorld);
							Vector3 intersected = RenderUtils.linePlaneIntersection(line, triangle);

							if (intersected != null) {
								Vector3 weights = computeBarycentricWeights(intersected);

								if (weights != null) {
									float distance = Vector3.distance(intersected, camera);

									if (distance < zbuf2[x + y * WIDTH]) {
										zbuf2[x + y * WIDTH] = distance;

										int color = triangle.color;

										if (triangle.uv0 != null && true3DTexturesEnabled) {
											Vector2 uv0 = triangle.uv0.scale(weights.x);
											Vector2 uv1 = triangle.uv1.scale(weights.y);
											Vector2 uv2 = triangle.uv2.scale(weights.z);

											Vector2 normTexCoord = uv0.add(uv1).add(uv2);

											int texX = (int) (normTexCoord.x * triangle.texture.width);
											int texY = (int) (normTexCoord.y * triangle.texture.height);

											int index = texX + texY * triangle.texture.width;

											if (index < triangle.texture.pixels.length) {
												color = triangle.texture.pixels[index];
											}
										}
										
										int red = (color >> 16) & 0xFF;
										int green = (color >> 8) & 0xFF;
										int blue = color & 0xFF;
										
										red -= (red - darkenBy >= 0) ? darkenBy : red;
										green -= (green - darkenBy >= 0) ? darkenBy : green;
										blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
										
										color = (red << 16) | (green << 8) | blue;

										//img.setRGB(x, y, shade(distance, color));
										screen[x + y * WIDTH] = shade(distance,color);
									}
								}
							}
						}
					}
				}
			}
		}

		private Vector3 computeBarycentricWeights(Vector3 v) {
			Vector3 v0 = triangle.bv0;
			Vector3 v1 = triangle.bv1;
			Vector3 v2 = v.minus(triangle.v0);
			float d00 = triangle.d00;
			float d01 = triangle.d01;
			float d11 = triangle.d11;
			float d20 = Vector3.dot(v2, v0);
			float d21 = Vector3.dot(v2, v1);
			float invDenom = triangle.invDenom;

			float w1 = (d11 * d20 - d01 * d21) * invDenom;

			if (w1 < 0) {
				return null;
			}

			float w2 = (d00 * d21 - d01 * d20) * invDenom;

			if (w2 < 0) {
				return null;
			}

			float w0 = 1.0f - w1 - w2;

			if (w0 < 0) {
				return null;
			} else {
				return new Vector3(w0, w1, w2);
			}
		}
	}
	
	private boolean isInFoV(Vector3 v0, Vector3 fovLeft, Vector3 fovRight, Vector3 camera) {
		Vector2 origin = camera.discardZ();
		Vector2 rightEndp = fovRight.discardZ();
		Vector2 leftEndp = fovLeft.discardZ();
		Vector2 testpoint = v0.discardZ();

		boolean leftOfRightRay = RenderUtils.isLeftOfRay(origin, rightEndp, testpoint);
		boolean rightOfLeftRay = !RenderUtils.isLeftOfRay(origin, leftEndp, testpoint);

		return leftOfRightRay && rightOfLeftRay;
	}
	
	private Rasterizer2 rasterizer2 = new Rasterizer2();
	private GPURasterizer gpuRasterizer = new GPURasterizer();
	
	// x and y correspond to world map axes; z is vertical axis; positive z points
	// up
	// (right handed system)
	private void renderAllVisibleModelsWithoutMatrices() {
		Map<Vector3, Vector3> renderedVertices = new HashMap<Vector3, Vector3>();
		
		// These three Vectors define the plane through which the player views the
		// world, in world space
		Vector3 plane0 = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x,
				camera.pos.y + camera.dir.y + camera.plane.y, 0.0f);
		Vector3 plane1 = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x,
				camera.pos.y + camera.dir.y + camera.plane.y, 1.0f);
		Vector3 plane2 = new Vector3(camera.pos.x + camera.dir.x - camera.plane.x,
				camera.pos.y + camera.dir.y - camera.plane.y, 0.0f);
		
		// plane2 is the left edge of the screen; plane0 is the right edge of the screen
		
		Vector3 cameraPos = new Vector3(camera.pos.x, camera.pos.y, 0.5f);
		Vector3 cameraLeft = new Vector3(camera.pos.x - camera.plane.x, camera.pos.y - camera.plane.y, 0);

		Vector3 leftFoV = new Vector3(camera.pos.x + camera.dir.x - camera.plane.x,
				camera.pos.y + camera.dir.y - camera.plane.y, 0.5f);
		Vector3 rightFoV = new Vector3(camera.pos.x + camera.dir.x + camera.plane.x,
				camera.pos.y + camera.dir.y + camera.plane.y, 0.5f);
		
		Triangle viewPlane = new Triangle(plane0, plane1, plane2);
		
		float zTranslate = (FINAL_ASPECT - 1) / 2;
		
		for (Vector3 location : modelQueue) {
			Block block = world.getBlockAt((int) location.x, (int) location.y);
			
			for (Triangle t : block.model.triangles) {
				
				// These three Vectors are the triangle's vertices in model space
				Vector3 _v0 = new Vector3(t.v0.x, t.v0.y, t.v0.z);
				Vector3 _v1 = new Vector3(t.v1.x, t.v1.y, t.v1.z);
				Vector3 _v2 = new Vector3(t.v2.x, t.v2.y, t.v2.z);
				
				// The vertices are scaled to fit with the final aspect ratio
				_v0.z -= zTranslate;
				_v1.z -= zTranslate;
				_v2.z -= zTranslate;
				
				// These three Vectors are the triangle's vertices, translated to world space
				Vector3 v0 = location.plus(_v0);
				Vector3 v1 = location.plus(_v1);
				Vector3 v2 = location.plus(_v2);

				Triangle translatedTriangle = new Triangle(v0, v1, v2, t.color, t.specular, t.bv0, t.bv1, t.d00, t.d01, t.d11,
						t.invDenom, t.shadeVal, t.xWeight, t.darkenBy).setUVCoords(t.uv0, t.uv1, t.uv2).setTexture(t.texture);
				
				if (!(isInFoV(v0, leftFoV, rightFoV, cameraPos) || isInFoV(v1, leftFoV, rightFoV, cameraPos)
						|| isInFoV(v2, leftFoV, rightFoV, cameraPos))) {
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
				
				
				rasterizer2.set(s0, s1, s2, translatedTriangle, v0, v1, v2, cameraPos);
				rasterizer2.perspectiveCorrectScanlineRaster();
				
				
				//Rasterization with the GPU
				/*
				int minX = (int) Math.min(s0.x, Math.min(s1.x, s2.x));
				int maxX = (int) Math.max(s0.x, Math.max(s1.x, s2.x));
				
				int minY = (int) Math.min(s0.y, Math.min(s1.y, s2.y));
				int maxY = (int) Math.max(s0.y, Math.max(s1.y, s2.y));
				
				if (minY < 0) {
					minY = 0;
				} else if (minY >= HUD_TRUE_HEIGHT) {
					return;
				}
				
				if (maxY >= HUD_TRUE_HEIGHT) {
					maxY = HUD_TRUE_HEIGHT-1;
				} else if (maxY < 0) {
					return;
				}
				
				if (minX < 0) {
					minX = 0;
				} else if (minX >= WIDTH) {
					return;
				}
				
				if (maxX >= WIDTH) {
					maxX = WIDTH-1;
				} else if (maxX < 0) {
					return;
				}
				
				int startY = minY;
				//int _HUD_TRUE_HEIGHT, int _WIDTH, int _startY, int _minX, int _maxX
				int _shadeType = 0;
				
				switch (shadeType) {
					case LINEAR:
						_shadeType = 1;
						break;
					case QUADRATIC:
						_shadeType = 2;
						break;
				}
				
				gpuRasterizer.set(s0, s1, s2, translatedTriangle, v0, v1, v2, cameraPos, camera.plane, camera.dir, HUD_TRUE_HEIGHT, WIDTH, HEIGHT, startY, minX, maxX, true3DTexturesEnabled, _shadeType, FULL_FOG_DISTANCE, SHADE_THRESHOLD, screen);
				Range range = Range.create(maxY - minY);
				gpuRasterizer.execute(range);
				*/
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
		boolean third = RenderUtils.isLeftOfRay(plane1, plane0, v2);
		
		return first && second && third;
	}
	
	private Vector2 locateOnScreen(Vector3 i, Vector3 plane0, Vector3 plane1) {
		int y = (int) ((1 - i.z) * HEIGHT);
		
		Vector2 p0 = new Vector2(plane0.x, plane0.y);
		Vector2 p1 = new Vector2(plane1.x, plane1.y);
		
		Wall wall = new Wall(p0, p1);
		Vector2 i2 = new Vector2(i.x, i.y);
		
		float norm = wall.getNorm(i2);
		
		Vector2 leftWallEndp = new Vector2(p0.x + camera.dir.x, p0.y + camera.dir.y);
		
		if (RenderUtils.isLeftOfRay(p0, leftWallEndp, i2)) {
			norm = -norm;
		}
		
		int x = (int) (norm * WIDTH);
		
		return new Vector2(x, y);
	}
	
	private Vector3 getOrCalculateAndStore(Map<Vector3, Vector3> renderedVertices, Vector3 cameraPos, Vector3 v,
			Triangle viewPlane) {
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
		// x -> x
		// z -> y
		Vector3 pos3D = new Vector3(camera.pos.x, 0.0f, camera.pos.y);
		Vector2 plane = camera.pos.add(camera.dir).add(camera.plane);
		Vector2 negplane = camera.pos.add(camera.dir).add(new Vector2(-camera.plane.x, -camera.plane.y));
		
		Vector3 plane0 = new Vector3(plane.x, 0.0f, plane.y);
		Vector3 plane1 = new Vector3(negplane.x, 0.0f, negplane.y);
		
		float y = v.y * 1000;
		float x = (Vector3.distance(v, plane0)) * 1000;
		
		// System.out.println(Vector3.distance(v,plane0) + Vector3.distance(v,plane1));
		// System.out.println(Vector3.distance(plane0, plane1));
		
		return new Vector2(x, y);
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
		
		private void perspectiveCorrectRaster(Vector2 v0, Vector2 v1, Vector2 v2, Triangle triangle, Vector3 v03,
				Vector3 v13, Vector3 v23, Vector3 cameraPos) {
			float v03Dist = Vector3.distance(v03, cameraPos);
			float v13Dist = Vector3.distance(v13, cameraPos);
			float v23Dist = Vector3.distance(v23, cameraPos);
			
			VectorAssociate a0 = new VectorAssociate(v0, v03);
			VectorAssociate a1 = new VectorAssociate(v1, v13);
			VectorAssociate a2 = new VectorAssociate(v2, v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = -8126703518881412438L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			ySorted.sort((a, b) -> (int) (a.v2.y - b.v2.y));
			
			List<VectorAssociate> xSorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 501740040442375390L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			xSorted.sort((a, b) -> (int) (a.v2.x - b.v2.x));
			
			Matrix2 T = new Matrix2(new float[] {v0.x - v2.x, v1.x - v2.x, v0.y - v2.y, v1.y - v2.y});
			
			float det = T.determinant();
			
			int startX = (int) xSorted.get(0).v2.x;
			int endX = (int) xSorted.get(2).v2.x;
			
			int startY = (int) ySorted.get(0).v2.y;
			int endY = (int) ySorted.get(2).v2.y;

			Triangle plane = new Triangle(v23, v13, v03);
			
			for (int y = startY; y <= endY; y++) {
				
				if (y >= 0 && y < HEIGHT) {
					for (int x = startX; x <= endX; x++) {
						if (x >= 0 && x < WIDTH && pointInTriangle(new Vector2(x, y), v0, v1, v2, det)) {
							float w0;
							float w1;
							float w2;
							
							float xNorm = 2 * x / (float) WIDTH - 1.0f;

							float rdirx = camera.pos.x + camera.dir.x + camera.plane.x * xNorm;
							float rdiry = camera.pos.y + camera.dir.y + camera.plane.y * xNorm;
							
							float rdirz = (1 - y) / (float) HEIGHT;
							
							Vector3 endp = new Vector3(rdirx, rdiry, rdirz);

							Vector3 f = RenderUtils.linePlaneIntersection(new Line(cameraPos, endp), plane);
							if (f != null) {

								Vector3 p1 = v03;
								Vector3 p2 = v13;
								Vector3 p3 = v23;

								Vector3 f1 = p1.minus(f);
								Vector3 f2 = p2.minus(f);
								Vector3 f3 = p3.minus(f);

								float a = Vector3.cross(p1.minus(p2), p1.minus(p3)).length;
								w0 = Vector3.cross(f2, f3).length / a;
								w1 = Vector3.cross(f3, f1).length / a;
								w2 = Vector3.cross(f1, f2).length / a;

								float distance = (w0 * v03Dist) + (w1 * v13Dist) + (w2 * v23Dist);

								if (distance < zbuf2[x + y * WIDTH]) {
									zbuf2[x + y * WIDTH] = distance;

									int color = triangle.color;

									if (triangle.uv0 != null && true3DTexturesEnabled) {

										Vector2 uv0 = triangle.uv0.scale(w0);
										Vector2 uv1 = triangle.uv1.scale(w1);
										Vector2 uv2 = triangle.uv2.scale(w2);
										
										Vector2 uv = uv0.add(uv1).add(uv2);

										GeneralTexture texture = triangle.texture;

										int texX = (int) (uv.x * texture.width);
										int texY = (int) (uv.y * texture.height);

										if (texX >= 0 && texX < texture.width && texY >= 0 && texY < texture.height) {
											color = texture.pixels[texX + texY * texture.width];
										}
									}

									//img.setRGB(x, y, shade(distance, color));
									screen[x + y * WIDTH] = shade(distance,color);
								}
							}
						}
					}
				}
			}
		}
		
		private boolean pointInTriangle(Vector2 p, Vector2 v0, Vector2 v1, Vector2 v2, float det) {
			float w0;
			float w1;
			float w2;
			
			w0 = (((v1.y - v2.y) * (p.x - v2.x)) + ((v2.x - v1.x) * (p.y - v2.y))) / det;
			
			if (w0 < 0) {
				return false;
			}
			
			w1 = (((v2.y - v0.y) * (p.x - v2.x)) + ((v0.x - v2.x) * (p.y - v2.y))) / det;
			
			if (w1 < 0) {
				return false;
			}
			
			w2 = 1 - w0 - w1;
			
			if (w2 < 0) {
				return false;
			}
			
			return true;
		}
		
		private void affineRaster(Vector2 v0, Vector2 v1, Vector2 v2, Triangle triangle, Vector3 v03, Vector3 v13,
				Vector3 v23, Vector3 camera) {
			float v03Dist = Vector3.distance(v03, camera);
			float v13Dist = Vector3.distance(v13, camera);
			float v23Dist = Vector3.distance(v23, camera);
			
			VectorAssociate a0 = new VectorAssociate(v0, v03);
			VectorAssociate a1 = new VectorAssociate(v1, v13);
			VectorAssociate a2 = new VectorAssociate(v2, v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 8563060661357225947L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			ySorted.sort((a, b) -> (int) (a.v2.y - b.v2.y));
			
			List<VectorAssociate> xSorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 1240112325831393490L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			xSorted.sort((a, b) -> (int) (a.v2.x - b.v2.x));
			
			Matrix2 T = new Matrix2(new float[] {v0.x - v2.x, v1.x - v2.x, v0.y - v2.y, v1.y - v2.y});
			
			float det = T.determinant();
			
			int startX = (int) xSorted.get(0).v2.x;
			int endX = (int) xSorted.get(2).v2.x;
			
			int startY = (int) ySorted.get(0).v2.y;
			int endY = (int) ySorted.get(2).v2.y;
			
			for (int y = startY; y <= endY; y++) {
				if (y >= 0 && y < HEIGHT) {
					for (int x = startX; x <= endX; x++) {
						if (x >= 0 && x < WIDTH) {
							float w0;
							float w1;
							float w2;
							
							w0 = (((v1.y - v2.y) * (x - v2.x)) + ((v2.x - v1.x) * (y - v2.y))) / det;
							
							if (w0 < 0) {
								continue;
							}
							
							w1 = (((v2.y - v0.y) * (x - v2.x)) + ((v0.x - v2.x) * (y - v2.y))) / det;
							
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
								
								if (triangle.uv0 != null && true3DTexturesEnabled) {
									
									Vector2 uv0 = triangle.uv0.scale(w0);
									Vector2 uv1 = triangle.uv1.scale(w1);
									Vector2 uv2 = triangle.uv2.scale(w2);
									
									Vector2 uv = uv0.add(uv1).add(uv2);
									
									GeneralTexture texture = triangle.texture;
									
									int texX = (int) (uv.x * texture.width);
									int texY = (int) (uv.y * texture.height);
									
									if (texX >= 0 && texX < texture.width && texY >= 0 && texY < texture.height) {
										color = texture.pixels[texX + texY * texture.width];
									}
								}
								
								//img.setRGB(x, y, shade(distance, color));
								screen[x + y * WIDTH] = shade(distance,color);
							}
						}
					}
				}
			}
		}
		
		@Deprecated
		private void rasterizeTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, Vector3 v03, Vector3 v13,
				Vector3 v23, Vector3 camera) {
			VectorAssociate a0 = new VectorAssociate(v0, v03);
			VectorAssociate a1 = new VectorAssociate(v1, v13);
			VectorAssociate a2 = new VectorAssociate(v2, v23);
			
			List<VectorAssociate> ySorted = new ArrayList<VectorAssociate>() {

				/**
				*
				*/
				private static final long serialVersionUID = 7196721569798511646L;

				{
					add(a0);
					add(a1);
					add(a2);
				}
			};
			ySorted.sort((a, b) -> (int) (a.v2.y - b.v2.y));
			
			Vector2 _v1 = ySorted.get(0).v2;
			Vector2 _v2 = ySorted.get(1).v2;
			Vector2 _v3 = ySorted.get(2).v2;
			
			Vector3 _v13 = ySorted.get(0).v3;
			Vector3 _v23 = ySorted.get(1).v3;
			Vector3 _v33 = ySorted.get(2).v3;
			
			float _v13Dist = Vector3.distance(camera, _v13);
			float _v23Dist = Vector3.distance(camera, _v23);
			float _v33Dist = Vector3.distance(camera, _v33);
			
			if ((int) _v2.y == (int) _v3.y) {
				rasterizeFlatBottomTriangle(_v1, _v2, _v3, color, _v13Dist, _v23Dist, _v33Dist);
			} else if ((int) _v1.y == (int) _v2.y) {
				rasterizeFlatTopTriangle(_v1, _v2, _v3, color, _v13Dist, _v23Dist, _v33Dist);
			} else {
				Vector2 _v4 = new Vector2((int) (_v1.x + ((_v2.y - _v1.y) / (_v3.y - _v1.y)) * (_v3.x - _v1.x)), _v2.y);
				
				float _v43Dist = (_v13Dist + _v33Dist) / 2.0f;
				
				rasterizeFlatBottomTriangle(_v1, _v2, _v4, color, _v13Dist, _v23Dist, _v43Dist);
				rasterizeFlatTopTriangle(_v2, _v4, _v3, color, _v23Dist, _v43Dist, _v33Dist);
			}
		}
		
		private void rasterizeFlatBottomTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, float topVertexDist,
				float leftVertexDist, float rightVertexDist) {
			float invslope1 = (v1.x - v0.x) / (v1.y - v0.y);
			float invslope2 = (v2.x - v0.x) / (v2.y - v0.y);
			
			float curx1 = v0.x;
			float curx2 = v0.x;
			
			float yDiff = v1.y - v0.y;
			float leftDistDiff = topVertexDist - leftVertexDist;
			float rightDistDiff = topVertexDist - rightVertexDist;
			
			for (int scanlineY = (int) v0.y; scanlineY <= v1.y; scanlineY++) {
				float leftSideDistance = (leftDistDiff * (scanlineY / yDiff)) + leftVertexDist;
				float rightSideDistance = (rightDistDiff * (scanlineY / yDiff) + rightVertexDist);
				
				drawHorizontalLine((int) curx1, (int) curx2, scanlineY, color, leftSideDistance, rightSideDistance);
				curx1 += invslope1;
				curx2 += invslope2;
			}
		}
		
		private void rasterizeFlatTopTriangle(Vector2 v0, Vector2 v1, Vector2 v2, int color, float bottomVertexDist,
				float leftVertexDist, float rightVertexDist) {
			float invslope1 = (v2.x - v0.x) / (v2.y - v0.y);
			float invslope2 = (v2.x - v1.x) / (v2.y - v1.y);
			
			float curx1 = v2.x;
			float curx2 = v2.x;
			
			float yDiff = v2.y - v1.y;
			float leftDistDiff = bottomVertexDist - leftVertexDist;
			float rightDistDiff = bottomVertexDist - rightVertexDist;
			
			for (int scanlineY = (int) v2.y; scanlineY > v1.y; scanlineY--) {
				float leftSideDistance = (leftDistDiff * (scanlineY / yDiff)) + leftVertexDist;
				float rightSideDistance = (rightDistDiff * (scanlineY / yDiff) + rightVertexDist);
				
				drawHorizontalLine((int) curx1, (int) curx2, scanlineY, color, leftSideDistance, rightSideDistance);
				curx1 -= invslope1;
				curx2 -= invslope2;
			}
		}
		
		private void drawHorizontalLine(int x0, int x1, int y, int color, float leftDist, float rightDist) {
			int xBegin = Math.min(x0, x1);
			int xEnd = Math.max(x0, x1);
			
			float distDiff = rightDist - leftDist;
			float xDiff = xEnd - xBegin;
			
			if (y >= 0 && y < HEIGHT) {
				for (int x = xBegin; x < xEnd; x++) {
					if (x >= 0 && x < WIDTH) {
						float distance = (distDiff * (x / xDiff)) + leftDist;
						if (zbuf2[x + y * WIDTH] > distance) {
							zbuf2[x + y * WIDTH] = distance;
							
							//img.setRGB(x, y, shade(distance, color));
							screen[x + y * WIDTH] = shade(distance,color);
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
			widthLUT[x] = x / (float) WIDTH;
		}
		
		heightLUT = new float[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			heightLUT[y] = y / (float) HEIGHT;
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

	private int HUD_TRUE_HEIGHT;

	private void drawHUDOnBottom() {
		int startAt = (int) (hud.beginAt * HEIGHT);
		HUD_TRUE_HEIGHT = startAt;
		
		for (int y = startAt; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int hudY = (int) (heightLUT[y] * hud.HEIGHT);
				int hudX = (int) (widthLUT[x] * hud.WIDTH);
				
				int color = hud.pixels[hudX + hudY * hud.WIDTH];
				
				if (color == HUD.HEALTH_COLOR || color == HUD.COFFEE_COLOR) {
					color = 0x6E6E6E;
				}
				
				//img.setRGB(x, y, color);
				screen[x + y * WIDTH] = color;
			}
		}
		
		drawHealthAndCoffeeBars();
	}
	
	private void drawHealthAndCoffeeBars() {
		Rectangle healthBar = hud.healthBar();
		
		float normHealthWidth = ((hud.getHealth() / 100.0f) * healthBar.width) / hud.WIDTH;
		normHealthWidth = (normHealthWidth < 0) ? 0 : (normHealthWidth > 1.0f) ? 1.0f : normHealthWidth;
		
		int healthWidth = (int) (normHealthWidth * WIDTH);
		int healthHeight = (int) ((healthBar.height / (float) hud.HEIGHT) * HEIGHT);
		int healthX = (int) ((healthBar.x / (float) hud.WIDTH) * WIDTH);
		int healthY = (int) ((healthBar.y / (float) hud.HEIGHT) * HEIGHT);
		
		Rectangle coffeeBar = hud.coffeeBar();
		float normCoffeeWidth = ((hud.getCoffee() / 100.0f) * coffeeBar.width) / hud.WIDTH;
		normCoffeeWidth = (normCoffeeWidth < 0) ? 0 : (normCoffeeWidth > 1.0f) ? 1.0f : normCoffeeWidth;
		
		int coffeeWidth = (int) (normCoffeeWidth * WIDTH);
		int coffeeHeight = (int) ((coffeeBar.height / (float) hud.HEIGHT) * HEIGHT);
		int coffeeX = (int) ((coffeeBar.x / (float) hud.WIDTH) * WIDTH);
		int coffeeY = (int) ((coffeeBar.y / (float) hud.HEIGHT) * HEIGHT);
		
		for (int y = healthY; y < healthHeight + healthY; y++) {
			for (int x = healthX; x < healthWidth + healthX; x++) {
				//img.setRGB(x, y, HUD.HEALTH_COLOR);
				screen[x + y * WIDTH] = HUD.HEALTH_COLOR;
			}
		}
		
		for (int y = coffeeY; y < coffeeHeight + coffeeY; y++) {
			for (int x = coffeeX; x < coffeeWidth + coffeeX; x++) {
				//img.setRGB(x, y, HUD.COFFEE_COLOR);
				screen[x + y * WIDTH] = HUD.COFFEE_COLOR;
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

			int halfModelSize = modelSize / 2;
			
			int startX = (WIDTH / 2) - halfModelSize;
			int startY = workableHeight - modelSize;
			
			if (startX < 0) {
				startX = 0;
			}
			
			if (startY < 0) {
				startY = 0;
			}
			
			for (int y = startY; y < startY + modelSize; y++) {
				
				for (int x = startX; x < startX + modelSize; x++) {
					int imgX = (int) (frame.SIZE * ((x - startX) / (float) modelSize));
					int imgY = (int) (frame.SIZE * ((y - startY) / (float) modelSize));
					
					int color = frame.pixels[imgX + frame.SIZE * imgY];
					
					if (color != Texture.ALPHA) {
						//img.setRGB(x, y, color);
						screen[x + y * WIDTH] = color;
					}
				}
			}
		}
	}

	public void enableTrue3DTextures() {
		true3DTexturesEnabled = true;
	}

	public void disableTrue3DTextures() {
		true3DTexturesEnabled = false;
	}
}
