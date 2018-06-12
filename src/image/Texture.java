package image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Texture {
	/**
	 * Some weird purple color
	 * (242,35,248)
	 * <p>
	 * The alpha mask for any transparency in a texture. If a texture has a pixel of this color
	 * that pixel will not be rendered and whatever lies behind that pixel will remain. This only works
	 * for sprites.
	 */
	public static final int ALPHA = 0xFFF223F8;
	
	public int[] pixels;
	public String path;
	public final int width;
	public final int height;
	
	/**
	 * Loads an image at the specified path with the specified width and height as a Texture.
	 * 
	 * @param _path relative path of image
	 * @param _width width of image
	 * @param _height height of image
	 */
	public Texture(String _path, int _width, int _height) {
		path = _path;
		width = _width;
		height = _height;
		pixels = new int[width * height];
		load();
	}
	
	public Texture(int[] _pixels, int _width, int _height) {
		pixels = _pixels;
		width = _width;
		height = _height;
	}
	
	private void load() {
		try {
			BufferedImage image = ImageIO.read(new File(path));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
