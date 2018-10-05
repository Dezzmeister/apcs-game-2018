package render.core.gpu;

import render.core.Raycaster;

/**
 * work in progress
 *
 * @author Joe Desmond
 */
@SuppressWarnings("all")
public class GPURaycaster extends Raycaster {

	/**
	 *
	 */
	private static final long serialVersionUID = -791367595480024769L;
	
	private GPURaycaster() {
		super(null, null, null, 0, 0, 0, 0, 0);
	}

	/**
	 *
	 * 
	 * private static final long serialVersionUID = -267034786990360375L; private
	 * GPURenderer renderer = new GPURenderer();
	 * 
	 * private float posx; private float posy; private float dirx; private float
	 * diry; private float planex; private float planey;
	 * 
	 * public GPURaycaster(Game _parentGame, Camera _camera, WorldMap _worldMap, int
	 * resWidth, int resHeight, int renderWidth, int renderHeight, int threads) {
	 * super(_parentGame, _camera, _worldMap, resWidth, resHeight, renderWidth,
	 * renderHeight, threads); }
	 * 
	 * @Override public void preRender() { super.preRender();
	 * 
	 *           posx = pos.x; posy = pos.y; dirx = dir.x; diry = dir.y; planex =
	 *           plane.x; planey = plane.y; }
	 * 
	 * @Override protected void render() { preRender(); GPURender();
	 *           finalizeRender(); postRender(); }
	 * 
	 *           private void GPURender() { Range range = Range.create(WIDTH);
	 *           renderer.execute(range); }
	 * 
	 *           private class GPURenderer extends Kernel {
	 * 
	 * @Override public void run() { this.render(); } // private Side sideHit;
	 *           private void render() { double adjMapX; double adjMapY; int
	 *           adjStepX; int adjStepY; boolean side = false; double xNorm; double
	 *           rdirx; double rdiry; int mapX; int mapY; float customHitX; float
	 *           customHitY; boolean customHit = false; int drawStart; int drawEnd;
	 *           int trueDrawStart; int lineHeight; double perpWallDist = 0; Block
	 *           block;
	 * 
	 *           float hitWallv0x; float hitWallv0y; float hitWallv1x; float
	 *           hitWallv1y;
	 * 
	 *           double wallX;
	 * 
	 *           int x = getGlobalId(); block = null;
	 * 
	 *           xNorm = 2 * x / (double) WIDTH - 1.0;
	 * 
	 *           rdirx = dir.x + plane.x * xNorm; rdiry = dir.y + plane.y * xNorm;
	 * 
	 *           mapX = (int) pos.x; mapY = (int) pos.y;
	 * 
	 *           double sideDistX; double sideDistY;
	 * 
	 *           double deltaDistX = Math.sqrt(1 + (rdiry * rdiry) / (rdirx *
	 *           rdirx)); double deltaDistY = Math.sqrt(1 + (rdirx * rdirx) / (rdiry
	 *           * rdiry));
	 * 
	 *           int stepX; int stepY;
	 * 
	 *           boolean hit = false;
	 * 
	 *           if (rdirx < 0) { stepX = -1; sideDistX = (pos.x - mapX) *
	 *           deltaDistX; } else { stepX = 1; sideDistX = (mapX + 1.0 - pos.x) *
	 *           deltaDistX; }
	 * 
	 *           if (rdiry < 0) { stepY = -1; sideDistY = (pos.y - mapY) *
	 *           deltaDistY; } else { stepY = 1; sideDistY = (mapY + 1.0 - pos.y) *
	 *           deltaDistY; } Vector2 currentLoc; customHitX = 0; customHitY = 0;
	 *           Vector2 tested = null; hitWall = null;
	 * 
	 *           dda: while (!hit) { if (sideDistX < sideDistY) { sideDistX +=
	 *           deltaDistX; mapX += stepX; side = false; } else { sideDistY +=
	 *           deltaDistY; mapY += stepY; side = true; } block =
	 *           world.getBlockAt(mapX, mapY); if (block != Block.SPACE) { if
	 *           (block.isCustom()) {
	 * 
	 *           float currentLocx = mapX; float currentLocy = mapY; float
	 *           rayDirectionx = (float) (rdirx + posx); float rayDirectiony =
	 *           (float) (rdiry + posy);
	 * 
	 *           for (Wall l : block.walls) { Wall testing = new
	 *           Wall(l.v0.add(currentLoc), l.v1.add(currentLoc)).tile(l.xTiles,
	 *           l.yTiles); tested = RenderUtils.rayHitSegment(pos, rayDirection,
	 *           testing);
	 * 
	 *           if (tested != null) { double tempDist; if (!side) { tempDist =
	 *           ((tested.x - pos.x + (1 - Math.abs(stepX)) / 2)) / rdirx; } else {
	 *           tempDist = ((tested.y - pos.y + (1 - Math.abs(stepY)) / 2)) /
	 *           rdiry; } if (tempDist < zbuf[x]) { zbuf[x] = tempDist;
	 *           testing.texture = l.texture; hitWall = testing; customHit = tested;
	 *           customHit = true; } } } if (customHit) { break dda; } } else { //
	 *           determineSideHit(stepX, stepY); hit = true; } } }
	 * 
	 *           if (customHit) { adjMapX = customHitX; adjMapY = customHitY;
	 *           adjStepX = Math.abs(stepX); adjStepY = Math.abs(stepY); } else {
	 *           adjMapX = mapX; adjMapY = mapY; adjStepX = stepX; adjStepY = stepY;
	 *           }
	 * 
	 *           if (!side) { perpWallDist = ((adjMapX - pos.x + (1 - adjStepX) /
	 *           2)) / rdirx; } else { perpWallDist = ((adjMapY - pos.y + (1 -
	 *           adjStepY) / 2)) / rdiry; }
	 * 
	 *           lineHeight = (int) (HEIGHT / perpWallDist);
	 * 
	 *           drawStart = -(lineHeight >> 1) + HALF_HEIGHT; trueDrawStart =
	 *           drawStart; if (drawStart < 0) { drawStart = 0; } drawEnd =
	 *           (lineHeight >> 1) + HALF_HEIGHT; if (drawEnd >= HEIGHT) { drawEnd =
	 *           HEIGHT - 1; }
	 * 
	 *           if (!customHit) { if (side) { wallX = (pos.x + ((adjMapY - pos.y +
	 *           (1 - adjStepY) / 2) / rdiry) * rdirx); } else { wallX = (pos.y +
	 *           ((adjMapX - pos.x + (1 - adjStepX) / 2) / rdirx) * rdiry); }
	 * 
	 *           wallX -= Math.floor(wallX);
	 * 
	 *           int texX;
	 * 
	 *           texX = (int) (wallX * block.frontTexture.SIZE);
	 * 
	 *           if (side) { texX = (int) ((texX * block.sideXTiles) %
	 *           block.sideTexture.SIZE); } else { texX = (int) ((texX *
	 *           block.frontXTiles) % block.frontTexture.SIZE); }
	 * 
	 *           if ((!side && rdirx > 0) || (side && rdiry < 0)) { texX =
	 *           block.frontTexture.SIZE - texX - 1; }
	 * 
	 *           for (int y = drawStart; y < drawEnd; y++) { int texY;
	 * 
	 *           texY = ((((y << 1) - HEIGHT + lineHeight) *
	 *           block.frontTexture.SIZE) / lineHeight) >> 1;
	 * 
	 *           if (side) { texY = (int) ((texY * block.sideYTiles) %
	 *           block.sideTexture.SIZE); } else { texY = (int) ((texY *
	 *           block.frontYTiles) % block.frontTexture.SIZE); }
	 * 
	 *           int color; if (!side && (texX + (texY * block.frontTexture.SIZE)) <
	 *           block.frontTexture.pixels.length && (texX + (texY *
	 *           block.frontTexture.SIZE)) >= 0) { color =
	 *           block.frontTexture.pixels[texX + (texY * block.frontTexture.SIZE)];
	 *           } else if ((texX + (texY * block.sideTexture.SIZE)) <
	 *           block.sideTexture.pixels.length && (texX + (texY *
	 *           block.sideTexture.SIZE)) >= 0) { color =
	 *           (block.sideTexture.pixels[texX + (texY * block.sideTexture.SIZE)]);
	 *           } else { color = 0; } float normValue = (float) (perpWallDist /
	 *           FULL_FOG_DISTANCE); color = RenderUtils.darkenWithThreshold(color,
	 *           normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD);
	 * 
	 *           img.setRGB(x, y, color); } } else { wallX =
	 *           hitWall.getNorm(customHit);
	 * 
	 *           wallX -= Math.floor(wallX);
	 * 
	 *           int texX; GeneralTexture texture = hitWall.texture;
	 * 
	 *           texX = (int) (texture.width * wallX * hitWall.xTiles) %
	 *           texture.width;
	 * 
	 *           for (int y = drawStart; y < drawEnd; y++) { int texY; texY = (int)
	 *           ((((y - trueDrawStart) / (float) lineHeight) * texture.height) *
	 *           hitWall.yTiles) % texture.height;
	 * 
	 *           int color = 0;
	 * 
	 *           int index = (texX + texY * texture.width);
	 * 
	 *           if (index >= 0 && index < texture.pixels.length) { color =
	 *           texture.pixels[index]; }
	 * 
	 *           float normValue = (float) (perpWallDist / FULL_FOG_DISTANCE); color
	 *           = RenderUtils.darkenWithThreshold(color, normValue >= 1 ? 1 :
	 *           normValue, SHADE_THRESHOLD); img.setRGB(x, y, color); } }
	 * 
	 *           zbuf[x] = perpWallDist;
	 * 
	 *           double floorXWall; double floorYWall;
	 * 
	 *           if (customHit) { floorXWall = adjMapX; floorYWall = adjMapY; } else
	 *           if (!side && rdirx > 0) { floorXWall = mapX; floorYWall = mapY +
	 *           wallX; } else if (!side && rdirx < 0) { floorXWall = mapX + 1.0;
	 *           floorYWall = mapY + wallX; } else if (side && rdiry > 0) {
	 *           floorXWall = mapX + wallX; floorYWall = mapY; } else { floorXWall =
	 *           mapX + wallX; floorYWall = mapY + 1.0; }
	 * 
	 *           double currentDist;
	 * 
	 *           if (drawEnd < 0) { drawEnd = HEIGHT; }
	 * 
	 *           SquareTexture floortex; SquareTexture ceilingtex;
	 * 
	 *           for (int y = drawEnd + 1; y < HEIGHT; y++) { currentDist =
	 *           wallDistLUT[y - HALF_HEIGHT];
	 * 
	 *           double weight = (currentDist) / (perpWallDist);
	 * 
	 *           double currentFloorX = weight * floorXWall + (1.0 - weight) *
	 *           pos.x; double currentFloorY = weight * floorYWall + (1.0 - weight)
	 *           * pos.y;
	 * 
	 *           floortex = world.getFloorAt((int) currentFloorX, (int)
	 *           currentFloorY); ceilingtex = world.getCeilingAt((int)
	 *           currentFloorX, (int) currentFloorY);
	 * 
	 *           int floorTexX; int floorTexY; floorTexX = (int) (currentFloorX *
	 *           floortex.SIZE) % floortex.SIZE; floorTexY = (int) (currentFloorY *
	 *           floortex.SIZE) % floortex.SIZE;
	 * 
	 *           int ceilTexX; int ceilTexY; if (floortex.SIZE == ceilingtex.SIZE) {
	 *           ceilTexX = floorTexX; ceilTexY = floorTexY; } else { ceilTexX =
	 *           (int) (currentFloorX * ceilingtex.SIZE) % ceilingtex.SIZE; ceilTexY
	 *           = (int) (currentFloorY * ceilingtex.SIZE) % ceilingtex.SIZE; }
	 * 
	 *           int color = (floortex.pixels[floortex.SIZE * floorTexY +
	 *           floorTexX]); // int color = 0xFF323232; int ceilColor =
	 *           (ceilingtex.pixels[ceilingtex.SIZE * ceilTexY + ceilTexX]); // int
	 *           ceilColor = 0xFF505050; float normValue = (float) (currentDist /
	 *           FULL_FOG_DISTANCE); color = RenderUtils.darkenWithThreshold(color,
	 *           normValue >= 1 ? 1 : normValue, SHADE_THRESHOLD); ceilColor =
	 *           RenderUtils.darkenWithThreshold(ceilColor, normValue >= 1 ? 1 :
	 *           normValue, SHADE_THRESHOLD); img.setRGB(x, y, color); img.setRGB(x,
	 *           (HEIGHT - y), ceilColor); } }
	 * 
	 *           }
	 */
}
