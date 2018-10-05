package image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public abstract class Texture {
	
	/**
	 * Some weird purple color (242,35,248)
	 * <p>
	 * The alpha mask for any transparency in a texture. If a texture has a pixel of
	 * this color that pixel will not be rendered and whatever lies behind that
	 * pixel will remain. This only works for sprites.
	 */
	public static final int ALPHA = 0xFFF223F8;

	public int[] pixels;
	public String path;

	public Texture() {

	}

	protected void load() {
		try {
			BufferedImage image = ImageIO.read(new File(path));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
