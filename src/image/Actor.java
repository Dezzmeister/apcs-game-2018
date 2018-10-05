package image;

import game.BoundedStat;
import render.math.Vector2;

public class Actor extends Entity {
	
	public BoundedStat health = new BoundedStat(0, 100);
	
	private SquareTexture[] views;
	
	/*
	 * public BufferedImage getImage() {
	 *
	 * try {
	 *
	 * return ImageIO.read(getClass().getResource("dwight.png"));
	 *
	 * private BufferedImage enemies = null;
	 *
	 * public BufferedImage getImage() {
	 *
	 * try {
	 *
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 *
	 * return null;
	 *
	 * } public void draw() { drawImage(enemies); }
	 */
	/*
	 * public void displayImage() { java.awt.EventQueue.invokeLater(new Runnable() {
	 * public void run() { setVisible(true); } });
	 */
	
	// the actors in the array must be in a certain order, which will be set TBD
	public Actor(SquareTexture[] _views) {
		views = _views;
	}

	public Actor(Vector2 _pos) {
		super(_pos);
	}
	
	@Override
	public SquareTexture getActiveTexture() {
		// TODO Auto-generated method stub
		return null;
	}
}