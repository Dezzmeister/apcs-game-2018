package render.core.gpu;

import org.jocl.struct.Struct;

import render.math.Vector3;

public class Vec3 extends Struct {
	public float x;
	public float y;
	public float z;
	
	public static final Vec3 of(Vector3 v) {
		Vec3 vec = new Vec3();
		vec.x = v.x;
		vec.y = v.y;
		vec.z = v.z;
		
		return vec;
	}
}
