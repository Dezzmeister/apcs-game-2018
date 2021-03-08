package image.filters;

import image.Filter;

public class GrayFilter implements Filter {

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		for (int i = 0; i < pixels.length; i++) {
			final int pixel = pixels[i];
			
			final int red = (pixel >>> 16) & 0xFF;
			final int green = (pixel >>> 8) & 0xFF;
			final int blue = pixel & 0xFF;
			
			final int gray = (red + green + blue)/3;
			pixels[i] = (gray << 16) | (gray << 8) | gray;
		}
		
		return pixels;
	}

}
