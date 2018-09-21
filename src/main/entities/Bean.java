package main.entities;

import image.Entity;
import image.SquareTexture;
import render.core.Camera;
import render.math.Vector2;

public class Bean extends Entity {
	private static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/bean.png",40);
	private final SquareTexture texture = DEFAULT_TEXTURE;
	
	public Bean(Vector2 pos, Camera _player) {
		super(pos);
		player = _player;
	}
	
	@Override
	public SquareTexture getActiveTexture() {
		return texture;
	}
}
