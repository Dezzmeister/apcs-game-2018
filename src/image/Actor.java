package image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Actor implements Entity {
	
		private BufferedImage enemies = null;
		
		public BufferedImage getImage() {
		
		try {
		   
		   enemies = ImageIO.read(getClass().getResource("dwight.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return enemies;
		
	}
		public void draw() {
	        drawImage(enemies);
		}
		
		/*public void displayImage() {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					setVisible(true);
				}
			});*/

		private void drawImage(BufferedImage image2) {
			// TODO Auto-generated method stub
			
		}
}