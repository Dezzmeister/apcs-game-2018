package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;
import image.Filter;

public abstract class TriChannelFilter implements Filter {
	protected final int width;
	protected final int height;
	
	protected final double[][] red;
	protected final double[][] green;
	protected final double[][] blue;
	
	protected final Matrix redMatrix;
	protected final Matrix greenMatrix;
	protected final Matrix blueMatrix;
	
	protected final int[] out;
	
	public TriChannelFilter(final int _width, final int _height) {
		width = _width;
		height = _height;
		
		red = new double[height][width];
		green = new double[height][width];
		blue = new double[height][width];
		
		redMatrix = new Matrix(red);
		greenMatrix = new Matrix(green);
		blueMatrix = new Matrix(blue);
		
		out = new int[width * height];
	}
	
	protected void copyToChannelBuffers(final int[] pixels) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				final int pixel = pixels[(row * width) + col];
				final int r = (pixel >>> 16) & 0xFF;
				final int g = (pixel >>> 8) & 0xFF;
				final int b = (pixel & 0xFF);
				
				red[row][col] = r;
				green[row][col] = g;
				blue[row][col] = b;
			}
		}
	}
	
	protected void recombine(final Matrix r, final Matrix g, final Matrix b) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				final int rp = (int) r.get(row, col);
				final int gp = (int) g.get(row, col);
				final int bp = (int) b.get(row, col);
				
				final int pixel = (rp << 16) | (gp << 8) | bp;
				out[(row * width) + col] = pixel;
			}
		}
	}
	
	protected void recombineDirect() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				final int r = (int) red[row][col];
				final int g = (int) green[row][col];
				final int b = (int) blue[row][col];
				
				final int pixel = (r << 16) | (g << 8) | b;
				out[(row * width) + col] = pixel;
			}
		}
	}
}
