package main;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import message_loop.Messenger;
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
	private Raycaster raycaster;
	public Container pane;
	public final AtomicBoolean isRunning = new AtomicBoolean(false);
	public final boolean[] keys = new boolean[256];
	public final MouseRobot mouse;
	
	{
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	public Game(int resolutionWidth, int resolutionHeight) {
		pack();
		setSize(resolutionWidth,resolutionHeight);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		pane = getContentPane();
		mouse = new MouseRobot(resolutionWidth, resolutionHeight, pane);
	}
	
	public Game setRaycaster(Raycaster _raycaster) {
		raycaster = _raycaster;
		return this;
	}
	
	/**
	 * Creates a new Thread and starts this Game on it.
	 * 
	 * @return the Thread running this Game
	 */
	public Thread startAndRun() {
		Thread gameThread = new Thread(this,"Coffee Bean Game Thread");
		gameThread.start();
		
		return gameThread;
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
			System.out.println(Thread.currentThread().getName());
			
			while (delta >= 1) {
				update();
				delta--;
			}
			
			if (raycaster != null) {
				raycaster.setDelta(delta);
				messageLoop();
			}
			repaint();
			frames++;
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frames = 0;
			}
		}
	}
	
	private void messageLoop() {
		for (String s : Messenger.getMessages()) {
			
			switch (s) {
			case "RENDER_ENABLE":
				raycaster.start();
				break;
			case "RENDER_DISABLE":
				raycaster.stop();
				break;
			}
		}
	}
	
	private void update() {
		
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
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse.x((int) e.getX());
		mouse.y((int) e.getY());		
	}
}
