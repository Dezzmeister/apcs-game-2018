package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;

public class BoobiFilter extends TriChannelFilter {

	public BoobiFilter(int _width, int _height) {
		super(_width, _height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffers(pixels);
		
		final Matrix kernel = new Matrix(new double[][]{
			{0.5, 0.5},
			{0.5, 0.5}
		});
		
		final Matrix r = redMatrix.convolve(kernel, 1, d -> d).pad(1, 1, 0);
		final Matrix g = greenMatrix.convolve(kernel, 1, d -> d).pad(1, 1, 0);
		final Matrix b = blueMatrix.convolve(kernel, 1, d -> d).pad(1, 1, 0);
		
		super.recombine(r, g, b);
		return super.out;
	}

}
