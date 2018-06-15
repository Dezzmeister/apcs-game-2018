package main;

import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

public class MouseRobot {
	private AtomicInteger x = new AtomicInteger(0);
	private AtomicInteger y = new AtomicInteger(0);
	
	private AtomicInteger px = new AtomicInteger(0);
	private AtomicInteger py = new AtomicInteger(0);
	private Robot robot;
	private Container pane;
	
	private int width;
	private int height;
	
	public MouseRobot(int _width, int _height, Container _pane) {
		width = _width;
		height = _height;
		pane = _pane;
	}
	
	{
		try {
			robot = new Robot();
			robot.setAutoDelay(0);
		} catch (Exception e) {
			
		}
	}
	
	public MouseRobot(int _width, int _height) {
		width = _width;
		height = _height;
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	public int x() {
		return x.get();
	}
	
	public int y() {
		return y.get();
	}
	
	public void x(int _x) {
		px.set(x());
		x.set(_x);
	}
	
	public void y(int _y) {
		py.set(y());
		y.set(_y);
	}
	
	@SuppressWarnings("unused")
	private void forceToCenterX() {
		robot.mouseMove(width/2, y());
	}
	
	@SuppressWarnings("unused")
	private void forceToCenterY() {
		robot.mouseMove(x(), height/2);
	}
	
	private void forceToCenter() {
		Point p = new Point(width/2,height/2);
		SwingUtilities.convertPointToScreen(p, pane);
		robot.mouseMove(p.x,p.y);
	}
	
	public void setWidth(int _width) {
		width = _width;
	}
	
	public void setHeight(int _height) {
		height = _height;
	}
	
	public int px() {
		return px.get();
	}
	
	public int py() {
		return py.get();
	}
	
	public int dx() {
		int ret = x() - (width/2);
		forceToCenter();
		return ret;
	}
	
	public int dy() {
		int ret = y() - (height/2);
		forceToCenter();
		return ret;
	}
}
