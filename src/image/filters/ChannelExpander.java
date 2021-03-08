package image.filters;

import image.Filter;

public class ChannelExpander implements Filter {

	@Override
	public int[] transform(int[] pixels, int width, int height) {
		for (int i = 0; i < pixels.length; i++) {
			final int c = pixels[i] & 0xFF;
			pixels[i] = (c << 16) | (c << 8) | c;
		}
		
		return pixels;
	}

}
