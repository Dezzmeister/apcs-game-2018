package render.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import image.SquareTexture;
import main.Game;
import render.math.RenderUtils;
import render.math.Vector2;

/**
 * Uses multiple threads to render a map. Uses a Camera holding player data such as position, direction, and FoV.
 *
 * @author Joe Desmond
 */
public final class Raycaster extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7071993726323961319L;
	private int WIDTH, HEIGHT, HALF_HEIGHT;
	private int actualWidth, actualHeight;
	private double[] zbuf;
	//private Sprite[] sprites;
	private Vector2 pos;
	private Vector2 dir;
	private Vector2 plane;
	private BufferedImage img;
	private Graphics2D g2;
	private Graphics g;
	public Camera camera;
	public WorldMap world;
	
	public static final float FULL_FOG_DISTANCE = 5f;
	public static final int SHADE_THRESHOLD = 100;
	
	private int rendererCount = 4;
	private ThreadRenderer[] renderers;
	private ThreadPoolExecutor executor;
	private LatchRef latchref;
	private double[] wallDistLUT;
	private AtomicBoolean enabled = new AtomicBoolean(true);
	private double delta;
	private Game parentGame;
	
	public Raycaster(Game _parentGame, Camera _camera, WorldMap _worldMap, int resWidth, int resHeight, int renderWidth, int renderHeight, int threads) {
		parentGame = _parentGame;
		camera = _camera;
		world = _worldMap;
		rendererCount = threads;
		WIDTH = renderWidth;
		HEIGHT = renderHeight;
		actualWidth = resWidth;
		actualHeight = resHeight;
		
		init();
	}
	
	private void init() {
		zbuf = new double[WIDTH];
		HALF_HEIGHT = HEIGHT/2;
		
		resetZBuffer();
		populateWallDistLUT();
		createThreadPoolRenderers();
	}
	
	private void preRender() {
		pos = camera.pos;
	    dir = camera.dir;
	    plane = camera.plane;
	    
	    g2 = (Graphics2D) g;
		g2.setBackground(Color.BLACK);
		g2.clearRect(0, 0, actualWidth, actualHeight);
		
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		handleMouseInput();
		//handleKeyboardInput();
	}
	
	private void postRender() {
		resetZBuffer();
		drawCoordinates();
	}
	
	//TODO: Remove this when we have a proper HUD
	private void drawCoordinates() {
		g.setColor(Color.GREEN);
	    g.drawString("x: "+pos.x,5, 20);
	    g.drawString("y: "+pos.y,5, 35);
	}
	
	private void finalizeRender() {
		g2.drawImage(img,  0,  0, actualWidth, actualHeight, null);
	}
	
	/**
	 * Uses multiple threads to render the scene.
	 */
	private void parallelRender() {
		latchref.update(rendererCount);
		for (int i = 0; i < renderers.length; i++) {
			executor.execute(renderers[i]);
		}
		
		try {
			latchref.latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void render() {
		preRender();
		parallelRender();
		finalizeRender();
		postRender();
	}
	
	/**
	 * Render this Raycaster's WorldMap to a Graphics object.
	 * 
	 * @param graphics
	 */
	public void render(Graphics graphics) {
		if (enabled.get()) {
			updateGraphics(graphics);
			render();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		render(g);
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
			wallDistLUT[i] = HEIGHT/((2.0 * y) - HEIGHT);
		}
	}
	
	public synchronized void start() {
		requestFocus();
		enabled.set(true);
	}
	
	public synchronized void stop() {
		enabled.set(false);
	}
	
	private void handleMouseInput() {
		//System.out.println(parentGame.mouse.dx());
	    if (parentGame.mouse.dx() < 0) {
	    	camera.rotateLeft(Math.abs(parentGame.mouse.dx()));
	    } else if (parentGame.mouse.dx() > 0) {
	    	camera.rotateRight(parentGame.mouse.dx());
	    }
	}
	
	private void handleKeyboardInput() {
		
		//Movement
	    float sprintfactor = (float) (delta * ((parentGame.keys[KeyEvent.VK_SHIFT]) ? 2 : 1));
	    
	    if (parentGame.keys['W'] || parentGame.keys[KeyEvent.VK_UP]) {
	    	camera.moveForward(world,sprintfactor);
	    }
	    if (parentGame.keys['S'] || parentGame.keys[KeyEvent.VK_DOWN]) {
	    	camera.moveBackward(world,sprintfactor);
	    }
	    if (parentGame.keys['A'] || parentGame.keys[KeyEvent.VK_LEFT]) {
	    	camera.moveLeft(world,sprintfactor);
	    }
	    if (parentGame.keys['D'] || parentGame.keys[KeyEvent.VK_RIGHT]) {
	    	camera.moveRight(world,sprintfactor);
	    }
	}
	
	/**
	 * Set the number used to synchronize game speed to constant real time.
	 * 
	 * @param _delta
	 */
	public void setDelta(double _delta) {
		delta = _delta;
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
			this.render();
			latchref.latch.countDown();
		}
		
		private void render() {
			for (int x = startX; x < endX; x++) {
		    	Block element = null;
		    	
		    	double camX = 2 * x/(double)WIDTH - 1.0;
		    	
		        double rdirx = dir.x + plane.x * camX;
		        double rdiry = dir.y + plane.y * camX;
		        
		        int mapX = (int)pos.x;
		        int mapY = (int)pos.y;
		        
		        double sideDistX;
		        double sideDistY;
		        
		        double deltaDistX = Math.sqrt(1 + (rdiry * rdiry)/(rdirx * rdirx));
		        double deltaDistY = Math.sqrt(1 + (rdirx * rdirx)/(rdiry * rdiry));
		        double perpWallDist;
		        
		        int stepX;
		        int stepY;
		        
		        boolean hit = false;
		        boolean side = false;
		        
		        double adjMapX;
		        double adjMapY;
		        int adjStepX;
		        int adjStepY;
		        
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
		        Vector2 customHit = null;
		        Vector2 tested = null;
		        Wall hitWall = null;
		        
		        dda:  while (!hit) {
		        	if (sideDistX < sideDistY) {
		        	    sideDistX += deltaDistX;
		        		mapX += stepX;
		        		side = false;
		        	} else {
		        		sideDistY += deltaDistY;
		        		mapY += stepY;
		        		side = true;
		        	}
		            element = world.getBlockAt(mapX, mapY);
		        	if (element.isSolid()) {
		        		if (element.isCustom()) {
		        			currentLoc = new Vector2(mapX,mapY);
		        			Vector2 rayDirection = new Vector2((float)rdirx + pos.x,(float)rdiry + pos.y);
		        			for (Wall l : element.walls) {
		        				Wall testing = new Wall(l.v0.add(currentLoc),l.v1.add(currentLoc));
		        				tested = RenderUtils.rayHitSegment(pos, rayDirection, testing);
		        				
		        				if (tested != null) {
		        					double tempDist;
		        					if (!side) {
		        						tempDist = ((tested.x - pos.x + (1 - Math.abs(stepX))/2))/rdirx;
		        					} else {
		        						tempDist = ((tested.y - pos.y + (1 - Math.abs(stepY))/2))/rdiry;
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
	        		perpWallDist = ((adjMapX - pos.x + (1 - adjStepX)/2))/rdirx;
	        	} else {
	        		perpWallDist = ((adjMapY - pos.y + (1 - adjStepY)/2))/rdiry;
	        	} /*
		        if (customHit == null) {
		        	if (!side) {
		        		perpWallDist = ((mapX - pos.x + (1 - stepX)/2))/rdirx;
		        	} else {
		        		perpWallDist = ((mapY - pos.y + (1 - stepY)/2))/rdiry;
		        	}
		        } else {
		        	//perpWallDist = Vector2.distance(pos, customHit);
		        	if (!side) {
		        		perpWallDist = ((customHit.x - pos.x + (1 - Math.abs(stepX))/2))/rdirx;
		        	} else {
		        		perpWallDist = ((customHit.y - pos.y + (1 - Math.abs(stepY))/2))/rdiry;
		        	}
		        	
		        }
		        */
		        int lineHeight = (int)(HEIGHT/perpWallDist);
		          
		        int drawStart = -(lineHeight >> 1) + HALF_HEIGHT;
		        int trueDrawStart = drawStart;
		        if (drawStart < 0) {
		        	  drawStart = 0;
		        }
		        int drawEnd = (lineHeight >> 1) + HALF_HEIGHT;
		        if (drawEnd >= HEIGHT) {
		        	  drawEnd = HEIGHT -1;
		        }
		        
		        //Texturing
		        double wallX;
		        if (customHit == null) {
		        	if (side) {
		        		wallX = (pos.x + ((adjMapY - pos.y + (1 - adjStepY)/2)/rdiry) * rdirx);
		        	} else {
		        		wallX = (pos.y + ((adjMapX - pos.x + (1 - adjStepX)/2)/rdirx) * rdiry);
		        	}
		        } else {
		        	wallX = hitWall.getNorm(customHit);
		        }
		        
		        wallX -= Math.floor(wallX);
		        
		        int texX;
		        
		        if (customHit == null) {
		        	texX = (int)(wallX * element.frontTexture.SIZE);
		        } else {
		        	//TODO change to wall texture
		        	texX = (int)(element.frontTexture.SIZE * wallX);
		        }
		        if((!side && rdirx > 0) || (side && rdiry < 0)) texX = element.frontTexture.SIZE - texX - 1;
		        
		        for (int y = drawStart; y < drawEnd; y++) {
		        	int texY;
		        	if (customHit == null) {
		        		texY = ((((y << 1) - HEIGHT + lineHeight) * element.frontTexture.SIZE)/lineHeight) >> 1;
		        	} else {
		        		texY = (int) (((y - trueDrawStart)/(float)lineHeight) * element.frontTexture.SIZE);
		        	}
		        	int color;
		        	if ((customHit != null || !side) && (texX + (texY * element.frontTexture.SIZE)) < element.frontTexture.pixels.length && (texX + (texY * element.frontTexture.SIZE)) >= 0) {
		        		color = element.frontTexture.pixels[(int) (texX + (texY * element.frontTexture.SIZE))];
		        	} else if ((texX + (texY * element.sideTexture.SIZE)) < element.sideTexture.pixels.length && (texX + (texY * element.sideTexture.SIZE)) >= 0){
		        		color = (element.sideTexture.pixels[(int) (texX + (texY * element.sideTexture.SIZE))]);
		        	} else {
		        		color = 0;
		        	}
		        	float normValue = (float) (perpWallDist/FULL_FOG_DISTANCE);
					color = RenderUtils.darkenWithThreshold(color,normValue >= 1 ? 1 : normValue,SHADE_THRESHOLD);
		        	img.setRGB(x, y, color);
		        }
		        //img.setRGB(x, drawStart, 0xFFFF0000);
		        //img.setRGB(x, drawEnd, 0xFFFF0000);
		        
		        zbuf[x] = perpWallDist;
		        
		        //Floor casting
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
		        } else if(side && rdiry > 0) {
		        	floorXWall = mapX + wallX;
		        	floorYWall = mapY;
		        } else {
		        	floorXWall = mapX + wallX;
		        	floorYWall = mapY + 1.0;
		        }
		        
		        double currentDist;
		        
		        if (drawEnd < 0) drawEnd = HEIGHT;
		        
		        SquareTexture floortex;
		        SquareTexture ceilingtex;
		        
		        for (int y = drawEnd + 1; y < HEIGHT; y++) {
		        	currentDist = wallDistLUT[y - HALF_HEIGHT];
		        	
		        	double weight = (currentDist)/(perpWallDist);
		        	
		        	double currentFloorX = weight * floorXWall + (1.0 - weight) * pos.x;
		        	double currentFloorY = weight * floorYWall + (1.0 - weight) * pos.y;
		        	
		        	floortex = world.getFloorAt((int)currentFloorX,(int)currentFloorY);
		        	ceilingtex = world.getCeilingAt((int)currentFloorX, (int)currentFloorY);
		        	
		        	int floorTexX;
		        	int floorTexY;
		        	floorTexX = (int)(currentFloorX * floortex.SIZE) % floortex.SIZE;
		        	floorTexY = (int)(currentFloorY * floortex.SIZE) % floortex.SIZE;
		        	
		        	int color = (floortex.pixels[floortex.SIZE * floorTexY + floorTexX]);
		        	//int color = 0xFF323232;
		        	int ceilColor = (ceilingtex.pixels[ceilingtex.SIZE * floorTexY + floorTexX]);
		        	//int ceilColor = 0xFF505050;
		        	float normValue = (float) (currentDist/FULL_FOG_DISTANCE);
					color = RenderUtils.darkenWithThreshold(color,normValue >= 1 ? 1 : normValue,SHADE_THRESHOLD);
					ceilColor = RenderUtils.darkenWithThreshold(ceilColor,normValue >= 1 ? 1 : normValue,SHADE_THRESHOLD);
		        	img.setRGB(x, y, color);
		        	img.setRGB(x, (HEIGHT - y), ceilColor);
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
	
	public void updateGraphics(Graphics graphics) {
		g = graphics;
	}
}
