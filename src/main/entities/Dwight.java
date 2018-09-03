package main.entities;

import image.SquareTexture;
import render.math.Vector2;

public class Dwight {
	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/dwight_purple.png",200);
	public Vector2 position;
	public SquareTexture texture = DEFAULT_TEXTURE;
	
	public Dwight(Vector2 _position) {
		position = _position;
	}
}
