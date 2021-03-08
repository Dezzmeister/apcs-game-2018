package image.filters;

public class BenisFilter extends TriChannelFilter {
	final int divs;

	public BenisFilter(final int _width, final int _height, final int _divs) {
		super(_width, _height);
		divs = _divs;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		super.copyToChannelBuffers(pixels);
		
		destroy(super.red);
		destroy(super.green);
		destroy(super.blue);
		
		super.recombineDirect();
		
		return super.out;
	}
	
	private void destroy(final double[][] aids) {
		for (int row = 0; row < aids.length; row++) {
			for (int col = 0; col < aids[row].length; col++) {
				aids[row][col] = destroy(aids[row][col]);
			}
		}
	}

	
	private double destroy(final double aids) {
		return (aids % divs) * (255.0 / divs);
	}
}
