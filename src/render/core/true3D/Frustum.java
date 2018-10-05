package render.core.true3D;

public class Frustum {
	
	public float top;
	public float bottom;

	public float left;
	public float right;

	public float near;
	public float far;

	public Frustum(float _top, float _bottom, float _left, float _right, float _near, float _far) {
		top = _top;
		bottom = _bottom;
		left = _left;
		right = _right;
		near = _near;
		far = _far;
	}
}
