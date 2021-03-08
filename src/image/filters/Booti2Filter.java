package image.filters;

import image.Filter;

public class Booti2Filter implements Filter {
	private final int divs;
	
	public Booti2Filter(final int _divs) {
		divs = _divs;
	}

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = (int)((pixels[i] % divs) * (255.0/divs));
		}
		
		return pixels;
	}

}
