package image.filters;

import image.Filter;

public class BootiFilter implements Filter {

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = pixels[i] & 0xFFFF;
		}
		
		return pixels;
	}

}
