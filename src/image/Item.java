package image;

import render.core.Camera;
import render.math.Vector2;

/*
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
*/
public class Item extends Entity {

	private SquareTexture texture;

	public Item(SquareTexture _texture, Vector2 initialPosition, Camera _player) {
		super(initialPosition);
		texture = _texture;
		player = _player;
		xDrawEnd = texture.SIZE;
		yDrawEnd = texture.SIZE;
	}

	@Override
	public SquareTexture getActiveTexture() {
		return texture;
	}
}