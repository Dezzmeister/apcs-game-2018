package image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Items implements Entity{
	private BufferedImage bean = null;
	private BufferedImage speedUp = null;
	private BufferedImage immunity = null;
	private BufferedImage extraBean = null;
	
	
	public BufferedImage getImage() { //returns a bean
		
		try {		   
		  bean = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));		 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return bean;
		
	}
	
	public BufferedImage getSpeedUpImage() {
		
		try {
		  speedUp = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));
		  immunity = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));
		  extraBean = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return speedUp;
		
	}
	public BufferedImage getImmunityImage() {
		
		try {
		  immunity = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return immunity;
		
	}
	public BufferedImage getExtraBeanImage() {
		
		try {
		  extraBean = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return extraBean;
		
	}
	
	
		public void draw() { 
	        drawImage(bean);
	        drawImage(speedUp);
	        drawImage(immunity);
	        drawImage(extraBean);
	        
		}
		

		private void drawImage(BufferedImage image2) {
			// TODO Auto-generated method stub
			
		}

}
	
