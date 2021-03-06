package render.math;

import image.GeneralTexture;
import render.math.geometry.Geometric;

public final class Triangle implements Geometric {
	
	private static final Vector3 X_AXIS = new Vector3(1, 0, 0);
	private static final Vector3 Z_AXIS = new Vector3(0, 0, 1);

	public Vector3 v0;
	public Vector3 v1;
	public Vector3 v2;

	public Vector2 uv0;
	public Vector2 uv1;
	public Vector2 uv2;

	public Vector3 bv0;
	public Vector3 bv1;
	public float d00;
	public float d01;
	public float d11;
	public float invDenom;
	public float xWeight = 0.5f;
	public int shadeThreshold = 75;
	public int darkenBy = 0;
	public float specular = 0.0f;

	public GeneralTexture texture = null;

	public int color = 0x0;
	public float shadeVal = 0;

	public Triangle(Vector3 _v0, Vector3 _v1, Vector3 _v2) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
		precomputeBarycentricInfo();
	}

	public Triangle(Vector3 _v0, Vector3 _v1, Vector3 _v2, int _color) {
		this(_v0, _v1, _v2);
		color = _color;
	}

	public Triangle(Vector3 _v0, Vector3 _v1, Vector3 _v2, int _color, float _specular, Vector3 _bv0, Vector3 _bv1, float _d00,
			float _d01, float _d11, float _invDenom, float _shadeVal, float _xWeight, int _darkenBy) {
		v0 = _v0;
		v1 = _v1;
		v2 = _v2;
		color = _color;
		specular = _specular;
		bv0 = _bv0;
		bv1 = _bv1;
		d00 = _d00;
		d01 = _d01;
		d11 = _d11;
		invDenom = _invDenom;
		shadeVal = _shadeVal;
		xWeight = _xWeight;
		darkenBy = _darkenBy;
	}
	
	public Triangle setShadeThreshold(int threshold) {
		shadeThreshold = threshold;
		
		darkenBy = (int)(shadeVal * shadeThreshold);
		
		return this;
	}
	
	public Triangle setXWeight(float _xWeight) {
		xWeight = _xWeight;
		
		return this;
	}

	public Triangle computeShadeValue() {
		Vector3 norm = getNormal().normalize();
		
		float xCos = Vector3.dot(norm, X_AXIS);
		float zCos = Vector3.dot(norm, Z_AXIS);
		xCos += RenderUtils.HALF_PI;
		zCos += RenderUtils.HALF_PI;
		
		float zWeight = 1 - xWeight;
		
		float normVal = ((xWeight * xCos) + (zWeight * zCos))/(float)Math.PI;
		shadeVal = normVal;
		
		darkenBy = (int)(shadeVal * shadeThreshold);
		
		return this;
	}

	public Triangle setColor(int _color) {
		color = _color;
		return this;
	}

	public Triangle setTexture(GeneralTexture _texture) {
		texture = _texture;
		return this;
	}

	public Triangle setUVCoords(Vector2 _uv0, Vector2 _uv1, Vector2 _uv2) {
		uv0 = _uv0;
		uv1 = _uv1;
		uv2 = _uv2;

		return this;
	}

	public Vector3 getNormal() {
		return Vector3.cross(v1.minus(v0), v2.minus(v0));
	}

	@Override
	public Triangle[] getTriangles() {
		return new Triangle[] {this};
	}

	private void precomputeBarycentricInfo() {
		bv0 = v1.minus(v0);
		bv1 = v2.minus(v0);

		d00 = Vector3.dot(bv0, bv0);
		d01 = Vector3.dot(bv0, bv1);
		d11 = Vector3.dot(bv1, bv1);

		invDenom = 1.0f / (d00 * d11 - d01 * d01);
	}

	public void transform(Matrix4 m) {
		v0 = m.transform(v0);
		v1 = m.transform(v1);
		v2 = m.transform(v2);
	}
}
