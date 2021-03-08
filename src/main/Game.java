package main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import audio.soundjunk.Repeater;
import audio.soundjunk.SoundManager;
import audio.soundjunk.Wav;
import game.BoundedStat;
import game.ViewModels;
import image.ViewModel;
import main.entities.BeanList;
import main.entities.DwightList;
import main.entities.HealthkitList;
import mapGen.MapGenerator;
import message_loop.Messenger;
import render.core.Block;
import render.core.Raycaster;
import render.core.WorldMap;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.Vector2;

/**
 * Represents a Dwight Game. Manages timing and player input and links other crucial game functions such as rendering,
 * audio, and logic.
 *
 * @author Joe Desmond
 */
public class Game extends JFrame implements Runnable, MouseMotionListener, KeyListener {
	
	/**
	 *
	 */
	private static final long serialVersionUID = -16764364630071584L;
	public static final BufferedImage CURSOR_IMG = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	public static final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(CURSOR_IMG,
			new Point(0, 0), "blank cursor");

	private MouseListener mouseListener = new MouseListener();

	private Raycaster raycaster;
	private SoundManager soundManager;
	public Container pane;
	public final AtomicBoolean isRunning = new AtomicBoolean(false);
	public final boolean[] keys = new boolean[256];
	public final MouseRobot mouse;
	public final Controls controls = new Controls();
	private static final DateFormat SCREENSHOT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private DwightList dwightList;
	private BeanList beanList;
	private Repeater stepper;
	private Thread stepperThread;
	private final AtomicInteger stepRate = new AtomicInteger(0);
	private BoundedStat health;
	private BoundedStat coffee;
	private HealthkitList healthkitList;

	private Messenger messenger = new Messenger();

	private ViewModel currentViewModel = null;
	
	private Vector2 goalPos = new Vector2(1,1);
	
	private List<String> logfile = new ArrayList<String>();

	{
		addMouseMotionListener(this);
		addMouseListener(mouseListener);
		addKeyListener(this);
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Window closing!");
				
				if (raycaster != null) {
					raycaster.shutdown();
				}
				
				if (soundManager != null) {
					soundManager.shutdown();
				}
				
				System.out.println("Saving latest map log file");
				
				try {
					Path out = Paths.get("latest map log file.txt");
					Files.write(out, logfile, Charset.defaultCharset());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	public Game(int resolutionWidth, int resolutionHeight) {
		pack();
		setSize(resolutionWidth + getInsets().left + getInsets().right,
				resolutionHeight + getInsets().bottom + getInsets().top);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pane = getContentPane();
		mouse = new MouseRobot(resolutionWidth, resolutionHeight, pane);
	}

	public Game setRaycaster(Raycaster _raycaster) {
		raycaster = _raycaster;
		dwightList = new DwightList(raycaster.camera, raycaster.world);
		beanList = new BeanList(raycaster.camera, raycaster.world, coffee);
		healthkitList = new HealthkitList(raycaster.camera, raycaster.world, health);
		
		pane.add(raycaster, BorderLayout.CENTER);

		if (currentViewModel != null) {
			raycaster.setCurrentViewModel(currentViewModel);
		}
		
		if (goalPos != null) {
			raycaster.setGoalPos(goalPos);
		}

		setVisible(true);
		raycaster.setDoubleBuffered(true);
		return this;
	}
	
	public Game setSoundManager(SoundManager _manager) {
		soundManager = _manager;
		return this;
	}

	public Game setMessenger(Messenger _messenger) {
		messenger = _messenger;
		return this;
	}

	public void setCurrentViewModel(ViewModel _currentViewModel) {
		currentViewModel = _currentViewModel;
		if (raycaster != null) {
			raycaster.setCurrentViewModel(currentViewModel);
		}
	}

	public void setHealthStat(BoundedStat _health) {
		health = _health;
	}

	public void setCoffeeStat(BoundedStat _coffee) {
		coffee = _coffee;

		if (beanList != null) {
			beanList.setCoffeeStat(coffee);
		}
	}

	/**
	 * Creates a new Thread and starts this Game on it, then returns the new Thread.
	 *
	 * @return the Thread running this Game
	 */
	public Thread startAndRun() {
		Thread gameThread = new Thread(this, "Coffee Bean Game Thread");
		gameThread.start();

		return gameThread;
	}

	public Game noCursor() {
		pane.setCursor(BLANK_CURSOR);
		return this;
	}
	
	public Game setGoalPos(Vector2 _goalPos) {
		goalPos = _goalPos;
		
		if (raycaster != null) {
			raycaster.setGoalPos(goalPos);
		}
		return this;
	}
	
	public void setStepPaths(String... paths) {
		stepper = new Repeater(stepRate, 500, paths);
		stepper.enable();
		stepperThread = new Thread(stepper);
		stepperThread.start();
	}

	@Override
	public void run() {
		requestFocus();
		long last = System.nanoTime();
		double ticks = 60;
		double ns = 1000000000 / ticks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		int lastFrames = 0;
		int targetFrames = 60;
		float deltaF = (1000 / targetFrames);
		long lastFrameTime = 0;
		isRunning.set(true);

		boolean secondPassed = false;
		boolean deadInit = false;

		while (isRunning.get()) {
			if (this.isFocusOwner() || health.get() <= 0) {
				mouse.enable();
			} else {
				mouse.disable();
			}

			if (coffee.get() <= 0 || health.get() <= 0) {
				if (currentViewModel == ViewModels.CUP_VIEWMODEL) {
					currentViewModel.setDefaultState("empty");
					currentViewModel.activateState("empty");
					currentViewModel.lock();
				}
			} else {
				if (currentViewModel == ViewModels.CUP_VIEWMODEL) {
					currentViewModel.setDefaultState("idle");
					currentViewModel.unlock();
				}
			}

			if (health.get() > 0) {
				long now = System.nanoTime();
				delta += (now - last) / ns;
				last = now;

				if (raycaster != null) {
					updateStepper();

					// System.out.println(entities.size());

					// raycaster.setEntities(dwightList.getDwights());
					raycaster.setEntities(dwightList.getDwights(), beanList.getBeans());
					raycaster.setDwightsKilled(dwightList.getDwightsKilled());
				}
				if (secondPassed) {
					dwightList.regeneratePaths();
					secondPassed = false;
				}
				
				if (raycaster.wonGame) {
					newMap();
				}
				
				while (delta >= 1 && raycaster.finished) {
					if (raycaster != null) {
						handleKeyboardInput(delta);
						dwightList.updateDwights(delta);
						beanList.update();
						healthkitList.update(delta);
					}

					if (currentViewModel != null) {
						currentViewModel.update();
					}
					
					if (dwightList.playerIsHit()) {
						health.lose((float) (delta * 0.195f));
					}
					
					if (soundManager != null) {
						if (raycaster != null) {
							soundManager.getLocalizer().setListener(raycaster.camera.pos);
							soundManager.getLocalizer()
									.setListenerDirection(raycaster.camera.dir.add(raycaster.camera.pos));
						}
						soundManager.update();
					}
					
					delta--;
				}
			} else {
				deadInit = true;
				
				if (!deadInit) {
					deadInit = false;
					// play music
				}
				raycaster.addDeath();
				repaint();
				
				stepRate.set(0);
				newMap();
			}
			
			if (Vector2.distance(raycaster.camera.pos, goalPos) <= 1.2) {
				raycaster.wonGame = true;
				raycaster.addWin();
				stepRate.set(0);
			}
			
			if (System.currentTimeMillis() - lastFrameTime > deltaF) {
				lastFrameTime = System.currentTimeMillis();
				repaint();
				frames++;
			}

			if (System.currentTimeMillis() - timer > 1000) {
				timer = System.currentTimeMillis();
				secondPassed = true;
				
				lastFrames = frames;
				frames = 0;
			}
		}
	}
	
	private void newMap() {
		
		MapGenerator map = new MapGenerator(GameConstants.MAP_SIZE, GameConstants.MAP_SIZE, Block.DwightElements.DWIGHTSPEC);
		map.generate();

		WorldMap newWorld = map.getFinalWorldMap();
		Vector2 newStartPos = map.getRandomStartPos();
		Vector2 newGoalPos = map.getGoalPos();
		
		raycaster.world.initializeInPlace(newWorld);
		raycaster.camera.pos.x = newStartPos.x;
		raycaster.camera.pos.y = newStartPos.y;
		goalPos = newGoalPos;
		
		raycaster.setGoalPos(newGoalPos);
		
		dwightList.reset();
		beanList.reset();
		healthkitList.reset();
		
		health.toMax();
		coffee.toMax();
		
		raycaster.wonGame = false;
	}
	
	private static final float BASE_PSI = 0.01f;
	private static final Matrix4 TRANSLATE_IN = Transformer.createTranslationMatrix(-0.5f, -0.5f, 0);
	private static final Matrix4 TRANSLATE_OUT = Transformer.createTranslationMatrix(0.5f, 0.5f, 0);
	
	public void animateFrame(float delta) {
		Matrix4 rotator = Transformer.createZRotationMatrix(BASE_PSI * delta);
		
		Matrix4 transformer = TRANSLATE_IN.multiply(rotator).multiply(TRANSLATE_OUT);
		Block.DwightElements.HEALTHKIT_MODEL.transformAndApply(transformer);
	}

	private void updateStepper() {
		if (health.get() > 0) {
			if (!keys[controls.moveForward] && !keys[controls.moveBackward] && !keys[controls.moveLeft]
					&& !keys[controls.moveRight]) {
				stepRate.set(0);
			} else {
				if (keys[controls.sprint]) {
					stepRate.set(2);
				} else {
					stepRate.set(1);
				}
			}
		}
	}

	private void handleKeyboardInput(double delta) {

		// Movement
		float sprintfactor = (float) (delta * ((keys[GameConstants.CT_SPRINT]) ? 2 : 1));
		stepRate.set(0);

		if (health.get() > 0) {
			if (keys[GameConstants.CT_FORWARD]) {
				raycaster.camera.moveForward(raycaster.world, sprintfactor);
			}
			if (keys[GameConstants.CT_BACKWARD]) {
				raycaster.camera.moveBackward(raycaster.world, sprintfactor);
			}
			if (keys[GameConstants.CT_LEFT]) {
				raycaster.camera.moveLeft(raycaster.world, sprintfactor);
			}
			if (keys[GameConstants.CT_RIGHT]) {
				raycaster.camera.moveRight(raycaster.world, sprintfactor);
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() < 256) {
			keys[e.getKeyCode()] = true;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		if (keyCode < 256) {
			keys[keyCode] = false;
		}
		
		if (GameConstants.DEBUG_MODE) {
			if (keyCode == GameConstants.CT_DEBUG_EXPLOSION) {
				// soundManager.quickPlayAt("assets/soundfx/boom.ogg", 6, 7);
				// soundManager.play("boom");
				Wav.playSound("assets/soundfx/boom.wav");
			}
			
			if (keyCode == GameConstants.CT_DEBUG_WIN) {
				raycaster.wonGame = true;
				raycaster.addWin();
				stepRate.set(0);
			}
			
			if (keyCode == GameConstants.CT_DEBUG_DIE) {
				health.toMin();
				stepRate.set(0);
			}
		}

		if (keyCode == GameConstants.CT_SCREENSHOT) {
			saveScreenShot();
		}
		
		if (keyCode == GameConstants.CT_NEWLEVEL) {
			newMap();
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = new Point(e.getX(), e.getY());
		SwingUtilities.convertPointFromScreen(p, pane);
		mouse.x(p.x);
		mouse.y(p.y);
	}

	public void saveScreenShot() {
		Rectangle screen = getBounds();
		Date date = new Date();
		String dateString = SCREENSHOT_DATE_FORMAT.format(date);
		String fileName = "screenshots/screenshot-" + dateString + ".png";
		File file = new File(fileName);

		int copyIndex = 0;
		while (file.exists()) {
			fileName = "screenshots/screenshot-" + dateString + "(" + copyIndex + ")" + ".png";
			file = new File(fileName);
			copyIndex++;
		}

		try {
			BufferedImage image = new Robot().createScreenCapture(screen);
			ImageIO.write(image, "png", file);
			System.out.println("Screenshot saved as " + fileName);
			log("Screenshot saved as " + fileName);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void log(String s) {
		logfile.add(s);
	}

	private class MouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (currentViewModel == ViewModels.CUP_VIEWMODEL && health.get() > 0) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (currentViewModel.primaryFire()) {
						coffee.lose(Main.COFFEE_MELEE_COST);
						dwightList.coffeePour(raycaster.getHitEntities(), raycaster.getClosestWallToCenter());
						Wav.playSound("assets/soundfx/sizzle.wav");
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					if (currentViewModel.secondaryFire()) {
						coffee.lose(Main.COFFEE_CANNON_COST);
						dwightList.coffeeCannon(raycaster.getHitEntities(), raycaster.getClosestWallToCenter());
						Wav.playSound("assets/soundfx/coffee_rifle.wav");
					}
				}
			}
		}
	}
}
