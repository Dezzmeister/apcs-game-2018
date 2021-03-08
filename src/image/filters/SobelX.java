package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;

public class SobelX extends SingleChannelFilter {

	public SobelX(int _width, int _height) {
		super(_width, _height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffer(pixels);
		
		final Matrix kernel = new Matrix(new double[][]{
			{-1, -2, -1},
			{0, 0, 0},
			{1, 2, 1}
		});
		
		final Matrix grs = grayMatrix.convolve(kernel, 1, d -> d).pad(2, 2, 0);
		
		super.recombine(grs);
		return super.out;
	}

}