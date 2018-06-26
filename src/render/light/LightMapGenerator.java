package render.light;

import render.light.cellmaps.CellMap;
import render.math.Triangle;
import render.math.Vector3;

public class LightMapGenerator {
	
	public static CellMap[][] calculateLightMap(CellMap[][] map, Light[] lights) {
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				for (int i = 0; i < map[row][col].getLightPlanes().length; i++) {
					LightPlane plane = map[row][col].getLightPlanes()[i];
					
					for (int j = 0; j < plane.getTriangles().length; j++) {
						Triangle triangle = plane.getTriangles()[j];
						
						Vector3 normal = triangle.getNormal();
						int flag;
						
						if (Math.abs(normal.x) > Math.abs(normal.y) && 
						    Math.abs(normal.x) > Math.abs(normal.z)) {
							
							flag = 1;
							//triangle.v0.x = 
						} else if (Math.abs(normal.y) > Math.abs(normal.x) &&
								   Math.abs(normal.y) > Math.abs(normal.z)) {
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
