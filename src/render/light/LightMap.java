package render.light;

/**
 * Represents a surface with a resolution of width*height, for which each value in the map dictates how much
 * and area should be darkened.
 *
 * @author Joe Desmond
 */
public class LightMap {
	public int[] values;
	public final int WIDTH;
	public final int HEIGHT;
	
	public LightMap(int _width, int _height) {
		WIDTH = _width;
		HEIGHT = _height;
		
		values = new int[WIDTH * HEIGHT];
	}
	
	public LightMap(int _width, int _height, int[] _values) {
		WIDTH = _width;
		HEIGHT = _height;
		values = _values;
	}
}
