
package image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ActorSelf implements Entity {

	private BufferedImage[] movement;
	private BufferedImage self = null;

	public ActorSelf() {
		movement = new BufferedImage[5];

		movement[1] = self;
		/*
		 * movement[2] = self; movement[3] = self; movement[4] = self; movement[5] =
		 * self;
		 */
	}

	public BufferedImage getImage() {

		try {

			self = ImageIO.read(getClass().getResource("E:/choichirlpix.png"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return self;

	}

	public void draw() { // use a loop to draw the images in a continuous motion
		drawImage(self);

	}

	private void drawImage(BufferedImage image2) {
		// TODO Auto-generated method stub

	}

	@Override
	public GeneralTexture getTexture() {
		// TODO Auto-generated method stub
		return null;
	}
}
