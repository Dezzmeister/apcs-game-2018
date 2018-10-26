package main.entities;

import render.core.true3D.Model;
import render.math.Vector2;
import render.math.geometry.Cylinder;

public class Pickup {
	private Model model;
	private Cylinder boundary;
	
	public Pickup(Model _model, float _radius, Vector2 _pos) {
		model = _model;
		boundary = new Cylinder(_radius, _pos);
	}
	
	public Pickup at(Vector2 _pos) {
		boundary.pos = _pos;
		return this;
	}
	
	public Model model() {
		return model;
	}
	
	public Vector2 getWorldSpaceBlockCoords() {
		int x = (int)Math.floor(boundary.pos.x);
		int y = (int)Math.floor(boundary.pos.y);
		
		return new Vector2(x,y);
	}
	
	public Vector2 getBlockSpaceCoords() {
		float x = boundary.pos.x - (float)Math.floor(boundary.pos.x);
		float y = boundary.pos.y - (float)Math.floor(boundary.pos.y);
		
		return new Vector2(x,y);
	}
}
