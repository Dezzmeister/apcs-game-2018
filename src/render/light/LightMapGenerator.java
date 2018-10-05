package render.light;

import render.light.cellmaps.CellMap;
import render.math.Triangle;
import render.math.Vector3;

public class LightMapGenerator {

	public static CellMap[][] calculateLightMap(CellMap[][] map, Light[] lights) {
		for (CellMap[] element : map) {
			for (int col = 0; col < element.length; col++) {
				for (int i = 0; i < element[col].getLightPlanes().length; i++) {
					LightPlane plane = element[col].getLightPlanes()[i];

					for (int j = 0; j < plane.getTriangles().length; j++) {
						Triangle triangle = plane.getTriangles()[j];

						Vector3 normal = triangle.getNormal();
						int flag;

						if (Math.abs(normal.x) > Math.abs(normal.y) && Math.abs(normal.x) > Math.abs(normal.z)) {

							flag = 1;
							// triangle.v0.x =
						} else if (Math.abs(normal.y) > Math.abs(normal.x) && Math.abs(normal.y) > Math.abs(normal.z)) {
							flag = 2;
						} else {
							flag = 3;
						}
					}
				}
			}
		}

		return map;
	}
}
