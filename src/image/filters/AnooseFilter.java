package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;

public class AnooseFilter extends TriChannelFilter {

	public AnooseFilter(int _width, int _height) {
		super(_width, _height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffers(pixels);
		
		final Matrix kernel = new Matrix(new double[][]{
			{0.1, 0.8, 0.1},
			{0.8, 0.1, 0.8},
			{0.1, 0.8, 0.1}
		});
		
		final Matrix b = redMatrix.convolve(kernel, 1, d -> d).pad(2, 2, 0);
		final Matrix g = greenMatrix.convolve(kernel, 1, d -> d).pad(2, 2, 0);
		final Matrix r = blueMatrix.convolve(kernel, 1, d -> d).pad(2, 2, 0);
		
		super.recombine(r, g, b);
		return super.out;
	}

}
