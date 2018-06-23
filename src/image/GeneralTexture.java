package image;

/**
 * Represents a Texture with differing width and height.
 *
 * @author Joe Desmond
 */
public class GeneralTexture extends Texture {

	public int width;
	public int height;
	
	/**
	 * Loads an image at the specified path with the specified width and height as a
	 * GeneralTexture.
	 *
	 * @param _path
	 *            relative path of image
	 * @param _width
	 *            width of image
	 * @param _height
	 *            height of image
	 */
	public GeneralTexture(String _path, int _width, int _height) {
		path = _path;
		width = _width;
		height = _height;
		pixels = new int[width * height];
		load();
	}
	
	public GeneralTexture(int[] _pixels, int _width, int _height) {
		pixels = _pixels;
		width = _width;
		height = _height;
	}
}
