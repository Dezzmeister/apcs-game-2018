package main.entities;

import image.Entity;
import image.SquareTexture;
import render.math.Vector2;

public class Dwight extends Entity {
	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/dwight_purple.png",200);
	public SquareTexture texture = DEFAULT_TEXTURE;
	public float speed = 0.01f;
	
	public Dwight(Vector2 _pos) {
		super(_pos);
	}

	@Override
	public SquareTexture getActiveTexture() {
		return texture;
	}
}
