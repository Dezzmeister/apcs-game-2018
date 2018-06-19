package image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Actor implements Entity {
	
		private GeneralTexture [] enemies;
		
		/*public BufferedImage getImage() {
		
		try {
		   
		   return ImageIO.read(getClass().getResource("dwight.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
		
	}
		public void draw() {
	        drawImage(enemies);
		}
		*/
		/*public void displayImage() {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					setVisible(true);
				}
			});*/

		//the actors in the array must be in a certain order, which will be set TBD
		public Actor (GeneralTexture [] actors) {
			enemies = actors;
		}
		public GeneralTexture getTexture(int input) {
			return enemies[input];
		}
}