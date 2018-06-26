package render.light.cellmaps;

import render.light.LightPlane;

/**
 * For any Cell (1x1x1) space in a WorldMap, a CellMap should contain {@link LightPlane LightPlanes} describing lighting data
 * for that Cell. <code>instanceof</code> and {@link render.core.Block#isCustom() Block.isCustom()} along with
 * {@link render.core.Block#isSolid() Block.isSolid()} can be used to determine which position in the LightPlane
 * array corresponds to which LightPlane.
 * <p>
 * This class is abstract for good reason; create instances of its subclasses which are defined for specific Cell
 * configurations.
 * <p>
 * This class and its children are separated from Block because their instances are dependent on position, and instances of Block are not.
 *
 * @author Joe Desmond
 */
public abstract class CellMap {
	protected final LightPlane[] lightplanes;
	
	public CellMap(LightPlane ... _lightplanes) {
		lightplanes = _lightplanes;
	}
	
	protected CellMap(int length) {
		lightplanes = new LightPlane[length];
	}
	
	public LightPlane[] getLightPlanes() {
		return lightplanes;
	}
}
