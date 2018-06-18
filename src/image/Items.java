package image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Items implements Entity{
	public BufferedImage getImage(){
		img = ImageIO.read(new File("strawberry.jpg"));

	}
	
}
/*	
public class BeanThing extends Item {
	public BufferedImage getImage(){
	img = ImageIO.read(new File("strawberry.jpg"));
}

} 

public class SpeedUpThing extends Item {
	public BufferedImage getImage(){
		img = ImageIO.read(new File("strawberry.jpg"));

}       

public class ImmunityThing extends Item {
	public BufferedImage getImage(){
	img = ImageIO.read(new File("strawberry.jpg"));


}
public class ExtraBeanThing extends Item {
	public BufferedImage getImage(){
		img = ImageIO.read(new File("strawberry.jpg"));
	}

}
*/
