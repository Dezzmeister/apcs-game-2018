package render.math;

public class Vector3 {
	public int x;
	public int y;
	public int z;
	
	public Vector3(int _x, int _y, int _z) {
		x = _x;
		y = _y;
		z = _z;
	}
	
	public static double distance(Vector3 v0, Vector3 v1) {
		return Math.sqrt(((v0.x - v1.x) * (v0.x - v1.x)) + ((v0.y - v1.y) * (v0.y - v1.y)) + ((v0.z - v1.z) * (v0.z - v1.z)));
	}
	
	public Vector3 add(Vector3 v) {
		return new Vector3(x+v.x,y+v.y,z+v.z);
	}
}
