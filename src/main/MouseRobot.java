package main;

import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

/**
 * Used to hold data about a cursor's position on the screen. Can also move
 * cursor to the center of a Container, with <code>dx()</code> and
 * <code>dy()</code>.
 *
 * @author Joe Desmond
 */
public class MouseRobot {

	private AtomicInteger x = new AtomicInteger(0);
	private AtomicInteger y = new AtomicInteger(0);
	
	private AtomicInteger px = new AtomicInteger(0);
	private AtomicInteger py = new AtomicInteger(0);
	private AtomicBoolean enabled = new AtomicBoolean(true);
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
	
	private void forceToCenter() {
		if (enabled.get()) {
			Point p = new Point(width / 2, height / 2);
			SwingUtilities.convertPointToScreen(p, pane);
			robot.mouseMove(p.x, p.y);
		}
	}
	
	public void setWidth(int _width) {
		width = _width;
	}
	
	public void setHeight(int _height) {
		height = _height;
	}
	
	public void disable() {
		enabled.set(false);
	}
	
	public void enable() {
		enabled.set(true);
	}
	
	/**
	 * Returns the position of the mouse on the x-axis from the previous frame.
	 *
	 * @return previous x-position
	 */
	public int px() {
		return px.get();
	}
	
	/**
	 * Returns the position of the mouse on the y-axis from the previous frame.
	 *
	 * @return previous y-position
	 */
	public int py() {
		return py.get();
	}
	
	/**
	 * Returns the difference in position on the x axis from the center of the
	 * Container, then moves the cursor to the center of the Container.
	 *
	 * @return difference on x axis
	 */
	public int dx() {
		int ret = x() - (width / 2);
		forceToCenter();
		return ret;
	}
	
	/**
	 * Returns the difference in position on the y axis from the center of the
	 * Container, then moves the cursor to the center of the Container.
	 *
	 * @return difference on y axis
	 */
	public int dy() {
		int ret = y() - (height / 2);
		forceToCenter();
		return ret;
	}
}
