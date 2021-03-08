package image;

/**
 * Applies a filter to a given image.
 * 
 * @author Joe Desmond
 */
public interface Filter {

	public int[] transform(int[] pixels, int width, int height);
}
