package render.math.geometry;

import image.GeneralTexture;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

public class Quad implements Geometric {
	public Triangle t0;
	public Triangle t1;
	
	public Quad(Vector3 v0, Vector3 v1, Vector3 v2, Vector3 v3, int color) {
		t0 = new Triangle(v0, v1, v2, color);
		t1 = new Triangle(v2, v3, v0, color);
	}

	public void setUVCoords(Vector2 u0, Vector2 u1, Vector2 u2, Vector2 u3) {
		t0.setUVCoords(u0, u1, u2);
		t1.setUVCoords(u2, u3, u0);
	}
	
	public void setTexture(GeneralTexture texture) {
		t0.setTexture(texture);
		t1.setTexture(texture);
	}
	
	@Override
	public Triangle[] getTriangles() {
		return new Triangle[] {t0,t1};
	}
}
