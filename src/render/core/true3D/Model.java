package render.core.true3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import render.math.Matrix4;
import render.math.Triangle;
import render.math.geometry.Geometric;

public class Model {
	
	public List<Triangle> triangles = new ArrayList<Triangle>();

	public Model(Triangle... _triangles) {
		Arrays.stream(_triangles).forEach(triangles::add);
	}

	public Model(List<Triangle> _triangles) {
		triangles = _triangles;
	}

	public Model add(Geometric g) {
		Arrays.stream(g.getTriangles()).forEach(triangles::add);
		return this;
	}

	public Model add(Model m) {
		triangles.addAll(m.triangles);
		return this;
	}

	public Model transform(Matrix4 m) {
		List<Triangle> temp = new ArrayList<Triangle>();

		for (Triangle t : triangles) {
			Triangle transformed = m.transform(t);
			temp.add(transformed);
		}

		return new Model(temp);
	}
}
