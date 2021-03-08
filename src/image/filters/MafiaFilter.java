package image.filters;

import dezzy.neuronz2.math.constructs.Matrix;

public class MafiaFilter extends SingleChannelFilter {

	public MafiaFilter(int _width, int _height) {
		super(_width, _height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffer(pixels);
		
		final Matrix kernel = new Matrix(new double[][]{
			{0.5, 0.5},
			{0.5, 0.5}
		});
		
		final Matrix grs = grayMatrix.convolve(kernel, 1, d -> d).pad(1, 1, 0);
		
		super.recombine(grs);
		return super.out;
	}
}
