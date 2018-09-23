package render.math;


public class Triangle {
	public Vector3 v0;
	public Vector3 v1;
	public Vector3 v2;
	
	public Vector2 uv0;
	public Vector2 uv1;
	public Vector2 uv2;
	
	public int debug_color = 0x0;
	
	public Triangle(Vector3 _v0, Vector3 _v1, Vector3 _v2) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
	}
	
	public Triangle(Vector3 _v0, Vector3 _v1, Vector3 _v2, int color) {
		this(_v0, _v1, _v2);
		debug_color = color;
	}
	
	public Triangle setColor(int _color) {
		debug_color = _color;
		return this;
	}
	
	public Triangle setUVCoords(Vector2 _uv0, Vector2 _uv1, Vector2 _uv2) {
		uv0 = _uv0;
		uv1 = _uv1;
		uv2 = _uv2;
		
		return this;
	}
	
	public Vector3 getNormal() {
		return Vector3.cross(v1.minus(v0), v2.minus(v0)).normalize();
	}
}
