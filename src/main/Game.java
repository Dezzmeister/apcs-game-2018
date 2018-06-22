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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import audio.soundjunk.SoundManager;
import render.core.Raycaster;

/**
 * Represents a Game object. Has a Raycaster, mouse data, and keyboard data.
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
	private Raycaster raycaster;
	private SoundManager soundManager;
	public Container pane;
	public final AtomicBoolean isRunning = new AtomicBoolean(false);
	public final boolean[] keys = new boolean[256];
	public final MouseRobot mouse;
	private static final DateFormat SCREENSHOT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	{
		addMouseMotionListener(this);
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
		setSize(resolutionWidth, resolutionHeight);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pane = getContentPane();
		mouse = new MouseRobot(resolutionWidth, resolutionHeight, pane);
	}

	public Game setRaycaster(Raycaster _raycaster) {
		raycaster = _raycaster;
		pane.add(raycaster, BorderLayout.CENTER);
		setVisible(true);
		raycaster.setDoubleBuffered(true);
		return this;
	}
	
	public Game setSoundManager(SoundManager _manager) {
		soundManager = _manager;
		return this;
	}

	/**
	 * Creates a new Thread and starts this Game on it.
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

	@Override
	public void run() {
		requestFocus();
		long last = System.nanoTime();
		double ticks = 60;
		double ns = 1000000000 / ticks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		isRunning.set(true);

		while (isRunning.get()) {
			long now = System.nanoTime();
			delta += (now - last) / ns;
			last = now;

			while (delta >= 1 && raycaster.finished) {
				if (raycaster != null) {
					handleKeyboardInput(delta);
				}
				delta--;
			}

			repaint();

			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;

				frames = 0;
			}
		}
	}

	private void handleKeyboardInput(double delta) {

		// Movement
		float sprintfactor = (float) (delta * ((keys[KeyEvent.VK_SHIFT]) ? 2 : 1));
		
		if (keys['W'] || keys[KeyEvent.VK_UP]) {
			raycaster.camera.moveForward(raycaster.world, sprintfactor);
		}
		if (keys['S'] || keys[KeyEvent.VK_DOWN]) {
			raycaster.camera.moveBackward(raycaster.world, sprintfactor);
		}
		if (keys['A'] || keys[KeyEvent.VK_LEFT]) {
			raycaster.camera.moveLeft(raycaster.world, sprintfactor);
		}
		if (keys['D'] || keys[KeyEvent.VK_RIGHT]) {
			raycaster.camera.moveRight(raycaster.world, sprintfactor);
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
		if (e.getKeyCode() < 256) {
			keys[e.getKeyCode()] = false;
		}

		if (e.getKeyChar() == 'p') {
			saveScreenShot();
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
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
