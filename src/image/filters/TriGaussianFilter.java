package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;

public class TriGaussianFilter extends TriChannelFilter {

	public TriGaussianFilter(int _width, int _height) {
		super(_width, _height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffers(pixels);
		
		final Matrix kernel = new Matrix(new double[][]{
			{2, 4, 5, 4, 2},
			{4, 9, 12, 9, 4},
			{5, 12, 15, 12, 5},
			{4, 9, 12, 9, 4},
			{2, 4, 5, 4, 2}
		});
		
		final Matrix r = redMatrix.convolve(kernel, 1, d -> d/159.0).pad(4, 4, 0);
		final Matrix g = greenMatrix.convolve(kernel, 1, d -> d/159.0).pad(4, 4, 0);
		final Matrix b = blueMatrix.convolve(kernel, 1, d -> d/159.0).pad(4, 4, 0);
		
		super.recombine(r, g, b);
		return super.out;
	}

}
