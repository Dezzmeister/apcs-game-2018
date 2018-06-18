package render.light;

import java.util.HashMap;
import java.util.Map;

public class BlockMap {
	private Map<Side,LightMap> lightmaps = new HashMap<Side,LightMap>();
	
	public BlockMap(LightMap posx, LightMap negx, LightMap posy, LightMap negy) {
		lightmaps.put(Side.POSX, posx);
		lightmaps.put(Side.NEGX, negx);
		lightmaps.put(Side.POSY, posy);
		lightmaps.put(Side.NEGY, negy);
	}
}
