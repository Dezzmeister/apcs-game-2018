package render.math.geometry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import image.GeneralTexture;
import render.core.true3D.Model;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

public class OBJModel extends Model {
	private final Path path;
	private final Path directory;
	private Path mtlName;
	
	/**
	 * Creates a model from an OBJ file at the specified path.
	 * 
	 * @param _path Must be defined with single forward slashes instead of backslashes
	 */
	public OBJModel(String _path) {
		path = Paths.get(_path);
		
		if (Files.exists(path)) {
			directory = path.getParent();
			
			try {
				loadModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			directory = null;
			mtlName = null;
			
			System.err.println("Path to .obj file " + path + " must be a valid path to a valid .obj file!");
		}		
	}
	
	private void loadModel() throws IOException {
		List<String> objLines = Files.readAllLines(path);
		List<String> mtlLines;
		List<Vector3> vertices = new ArrayList<Vector3>();
		List<Vector2> textureVertices = new ArrayList<Vector2>();
		List<Triangle> faces = new ArrayList<Triangle>();
		Map<String, Material> mtlLib = null;
		Material currentMaterial = null;
		
		for (String s : objLines) {
			if (s.length() >= 1 && s.charAt(0) == '#') {
				continue;
			}
			
			if (s.indexOf("mtllib ") == 0) {
				String name = s.substring(s.indexOf(" ") + 1);
				mtlName = Paths.get(name);
				mtlLines = Files.readAllLines(Paths.get(directory.toString(), mtlName.toString()));
				mtlLib = loadMaterialLibrary(mtlLines);
				continue;
			}
			
			if (s.indexOf("usemtl ") == 0) {
				String name = s.substring(s.indexOf(" ") + 1);
				
				if (mtlLib != null) {
					currentMaterial = mtlLib.get(name);
				}
				
				continue;
			}
			
			if (s.indexOf("v ") == 0) {
				Vector3 vertex = getFloatVector(s);
				vertices.add(vertex);
				
				continue;
			}
			
			if (s.indexOf("vt ") == 0) {
				String[] elements = s.split(" ");
				
				if (elements.length >= 3) {
					float u = Float.parseFloat(elements[1]);
					float v = Float.parseFloat(elements[2]);
					
					Vector2 uv = new Vector2(u,v);
					textureVertices.add(uv);
				}
				
				continue;
			}
			
			if (s.indexOf("f ") == 0) {
				if (s.indexOf("/") == -1) {
					Vector3 fVertices = getFloatVector(s);
				
					Vector3 v0 = vertices.get((int)(fVertices.x - 1));
					Vector3 v1 = vertices.get((int)(fVertices.y - 1));
					Vector3 v2 = vertices.get((int)(fVertices.z - 1));
					
					if (currentMaterial != null) {
						int color = currentMaterial.Kd();
					
						Triangle triangle = new Triangle(v0,v1,v2,color);
						faces.add(triangle);
					}
				} else {
					String[] elements = s.split(" ");
					
					if (elements.length >= 4) {
						String e0 = elements[1];
						String e1 = elements[2];
						String e2 = elements[3];
						
						int v0index = Integer.parseInt(e0.substring(0, e0.indexOf("/"))) - 1;
						int v1index = Integer.parseInt(e1.substring(0, e1.indexOf("/"))) - 1;
						int v2index = Integer.parseInt(e2.substring(0, e2.indexOf("/"))) - 1;
						
						int uv0index = Integer.parseInt(e0.substring(e0.indexOf("/") + 1)) - 1;
						int uv1index = Integer.parseInt(e1.substring(e1.indexOf("/") + 1)) - 1;
						int uv2index = Integer.parseInt(e2.substring(e2.indexOf("/") + 1)) - 1;
						
						Vector3 v0 = vertices.get(v0index);
						Vector3 v1 = vertices.get(v1index);
						Vector3 v2 = vertices.get(v2index);
						
						Vector2 uv0 = textureVertices.get(uv0index);
						Vector2 uv1 = textureVertices.get(uv1index);
						Vector2 uv2 = textureVertices.get(uv2index);
						
						GeneralTexture texture = currentMaterial.map_Kd;
						int color = currentMaterial.Kd();
						
						Triangle triangle = new Triangle(v0,v1,v2,color).setUVCoords(uv0,uv1,uv2).setTexture(texture);
						faces.add(triangle);
					}
				}
				
				continue;
			}
		}
		
		super.triangles = faces;
	}
	
	private Map<String, Material> loadMaterialLibrary(List<String> mtlLines) {
		Map<String, Material> map = new HashMap<String, Material>();
		Material activeMaterial = null;
		
		for (String s : mtlLines) {
			
			if (s.length() >= 1 && s.charAt(0) == '#') {
				continue;
			}
			
			if (s.indexOf("newmtl ") == 0) {
				if (activeMaterial != null) {
					map.put(activeMaterial.name, activeMaterial);
				}
				String name = s.substring(s.indexOf(" ") + 1);
				activeMaterial = new Material();
				activeMaterial.name = name;
				continue;
			}
			
			if (s.indexOf("Kd ") == 0) {
				activeMaterial.Kd = getFloatVector(s);
				activeMaterial.Kd = activeMaterial.Kd.scale(255);
				
				continue;
			}
			
			if (s.indexOf("map_Kd ") == 0) {
				String filename = s.substring(s.indexOf(" ") + 1);
				
				activeMaterial.map_Kd = new GeneralTexture(directory.toString() + "/" + filename);
				continue;
			}
		}
		
		map.put(activeMaterial.name, activeMaterial);
		
		return map;
	}
	
	private Vector3 getFloatVector(String line) {
		String[] elements = line.split(" ");
		
		if (elements.length >= 4) {
			float x = Float.parseFloat(elements[1]);
			float y = Float.parseFloat(elements[2]);
			float z = Float.parseFloat(elements[3]);
		
			return new Vector3(x,y,z);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private class Material {
		String name;
		Vector3 Ka = new Vector3(51, 51, 51);
		Vector3 Kd = new Vector3(204, 204, 204);
		Vector3 Ks = new Vector3(255, 255, 255);
		float d = 255;
		float Tr = 0;
		float Ns = 0;
		float illum = 1;
		GeneralTexture map_Kd = null;
		
		int Kd() {
			return ((int)Kd.z) | ((int)Kd.y << 8) | ((int)Kd.x << 16); 
		}
	}
}
