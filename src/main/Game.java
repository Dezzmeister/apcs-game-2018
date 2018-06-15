package main;

import java.awt.Container;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import render.core.Raycaster;

public class Game extends JFrame implements Runnable {
	private Raycaster raycaster;
	private int renderWidth;
	private int renderHeight;
	public Container pane;
	public final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public Game(int resolutionWidth, int resolutionHeight, int _renderWidth, int _renderHeight) {
		setSize(resolutionWidth,resolutionHeight);
		renderWidth = _renderWidth;
		renderHeight = _renderHeight;
		pane = getContentPane();
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
}
