package render.core.gpu;

import org.jocl.struct.Struct;

import render.math.Vector2;

public class Vec2 extends Struct {
	float x;
	float y;
	
	public static Vec2 of(Vector2 v) {
		Vec2 vec = new Vec2();
		vec.x = v.x;
		vec.y = v.y;
		
		return vec;
	}
}
