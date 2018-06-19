package image;

import java.awt.image.BufferedImage;

public class Actor implements Entity {
	
	private GeneralTexture[] enemies;
	
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
	public Actor(GeneralTexture[] actors) {
		enemies = actors;
	}
	
	/*
	 * public GeneralTexture getTexture(int input) { return enemies[input]; }
	 */
	
	private void drawImage(BufferedImage image2) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public GeneralTexture getTexture() {
		// TODO Auto-generated method stub
		return null;
	}
}