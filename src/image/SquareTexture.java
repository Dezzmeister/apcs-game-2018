package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * A square texture.
 *
 * @author Joe Desmond
 */
public class SquareTexture extends Texture {
	
	/**
	 * The height and width of the texture.
	 */
	public final int SIZE;
	private GeneralTexture generalVersion = null;

	/**
	 * Loads an image at the specified path with the specified size as a
	 * SquareTexture.
	 *
	 * @param _path
	 *            relative path of image
	 * @param _width
	 *            width of image
	 * @param _height
	 *            height of image
	 */
	public SquareTexture(String _path, int _size) {
		path = _path;
		SIZE = _size;
		pixels = new int[SIZE * SIZE];
		load();
	}

	public SquareTexture(int[] _pixels, int _size) {
		pixels = _pixels;
		SIZE = _size;
	}
	
	public SquareTexture(String _path) {
		path = _path;
		load();
		int size = -1;
		
		try {
			BufferedImage image = ImageIO.read(new File(path));
			int w = image.getWidth();
			int h = image.getHeight();
			size = w;
			
			if (w != h) {
				System.out.println("Image at " + path + " is not square!");
			}
			pixels = new int[w * h];
			
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SIZE = size;
	}

	public GeneralTexture asGeneralTexture() {
		if (generalVersion != null) {
			return generalVersion;
		} else {
			return new GeneralTexture(pixels, SIZE, SIZE);
		}
	}
}
