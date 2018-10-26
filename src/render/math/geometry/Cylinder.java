package render.math.geometry;

import render.math.Vector2;

public class Cylinder {
	public float radius;
	public Vector2 pos;
	
	public Cylinder(float _radius) {
		this(_radius, null);
	}
	
	public Cylinder(float _radius, Vector2 _pos) {
		radius = _radius;
		pos = _pos;
	}
	
	public Cylinder at(Vector2 _pos) {
		pos = _pos;
		return this;
	}
}
