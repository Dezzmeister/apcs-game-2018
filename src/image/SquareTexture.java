package image;

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
	
	/**
	 * Loads an image at the specified path with the specified size as a BlockTexture.
	 * 
	 * @param _path relative path of image
	 * @param _width width of image
	 * @param _height height of image
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
}
