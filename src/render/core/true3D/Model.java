package render.core.true3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import render.math.Triangle;

public class Model {
	public List<Triangle> triangles = new ArrayList<Triangle>();
	
	public Model(Triangle ... _triangles) {
		Arrays.stream(_triangles).forEach(triangles::add);
	}
	
	public void addTriangle(Triangle t) {
		triangles.add(t);
	}
}
