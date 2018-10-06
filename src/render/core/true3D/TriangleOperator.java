package render.core.true3D;

import render.math.Triangle;

@FunctionalInterface
public interface TriangleOperator {
	
	void operate(Triangle t);
}
