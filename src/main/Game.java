package main;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import render.core.Raycaster;

public class Game extends JFrame implements Runnable, MouseMotionListener, KeyListener {
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
		setSize(resolutionWidth,resolutionHeight);
		pane = getContentPane();
		mouse = new MouseRobot(resolutionWidth, resolutionHeight, pane);
	}
	
	public Game(int resolutionWidth, int resolutionHeight, Raycaster _raycaster) {
		setSize(resolutionWidth,resolutionHeight);
		pane = getContentPane();
		mouse = new MouseRobot(resolutionWidth, resolutionHeight, pane);
		raycaster = _raycaster;
	}
	
	public Game setRaycaster(Raycaster _raycaster) {
		raycaster = _raycaster;
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
		
		while (isRunning.get()) {
			long now = System.nanoTime();
			delta += (now - last) / ns;
			last = now;
			
			while (delta >= 1) {
				update();
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
