package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;
import image.Filter;

public abstract class SingleChannelFilter implements Filter {
	protected final int width;
	protected final int height;
	
	protected final double[][] grays;
	protected final Matrix grayMatrix;
	
	protected final int[] out;
	
	public SingleChannelFilter(final int _width, final int _height) {
		width = _width;
		height = _height;
		grays = new double[height][width];
		out = new int[height * width];
		grayMatrix = new Matrix(grays);
	}
	
	public void copyToChannelBuffer(final int[] pixels) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				final int pixel = pixels[(row * width) + col];
				
				grays[row][col] = pixel;
			}
		}
	}
	
	protected void recombine(final Matrix grs) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				final int grp = (int) grs.get(row, col);
				
				out[(row * width) + col] = grp;
			}
		}
	}
}
