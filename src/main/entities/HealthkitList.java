package main.entities;

import java.util.ArrayList;
import java.util.List;

import audio.soundjunk.Wav;
import game.BoundedStat;
import main.GameConstants;
import render.core.Block;
import render.core.Camera;
import render.core.WorldMap;
import render.math.Vector2;

public class HealthkitList {
	private List<Healthkit> list = new ArrayList<Healthkit>();
	private final Camera player;
	private final WorldMap world;
	private final BoundedStat health;
	
	public HealthkitList(Camera _player, WorldMap _world, BoundedStat _health) {
		player = _player;
		world = _world;
		health = _health;
	}
	
	public void update(double delta) {
		while (list.size() <= GameConstants.MAX_HEALTHKITS) {
			addRandomHealthkit();
		}
		
		for (int i = list.size() - 1; i >= 0; i--) {
			Healthkit kit = list.get(i);
			
			Vector2 kitCenter = kit.boundary.pos;
			Vector2 playerPos = player.pos;
			
			float xDiff = playerPos.x - kitCenter.x;
			float yDiff = playerPos.y - kitCenter.y;
			
			float distanceSquared = (xDiff * xDiff) + (yDiff * yDiff);
			
			if (distanceSquared <= kit.cylinderRadiusSquared) {
				list.remove(i);
				removeFromWorld(kit);
				health.gain(GameConstants.HEALTHKIT_HEALTH_VALUE);
				Wav.playSound("assets/soundfx/blip.wav");
			} else if (distanceSquared >= GameConstants.HEALTHKIT_DESPAWN_DISTANCE * GameConstants.HEALTHKIT_DESPAWN_DISTANCE) {
				list.remove(i);
				removeFromWorld(kit);
			} else {
				list.get(i).animateFrame(delta);
			}
		}
	}
	
	private void removeFromWorld(Healthkit kit) {
		Vector2 worldPos = kit.getWorldSpaceBlockCoords();
		world.setBlockAt((int)worldPos.x, (int)worldPos.y, Block.SPACE);
	}
	
	private void addRandomHealthkit() {	
		int xLoc = getRandomCoord((int)player.pos.x, world.width);
		int yLoc = getRandomCoord((int)player.pos.y, world.height);
		
		Block block = world.getBlockAt(xLoc, yLoc);
		
		while (block.isSolid() || block.isVisible() || block.isModel() || xLoc == (int)player.pos.x || yLoc == (int)player.pos.y) {
			xLoc = getRandomCoord((int)player.pos.x, world.width);
			yLoc = getRandomCoord((int)player.pos.y, world.height);
			
			block = world.getBlockAt(xLoc, yLoc);
		}
		
		Healthkit kit = new Healthkit(new Vector2(xLoc + 0.5f, yLoc + 0.5f));
		list.add(kit);
		
		Block newBlock = new Block("health kit block").defineAsModel(kit.model()).fakeBlock();
		world.setBlockAt(xLoc, yLoc, newBlock);
	}
	
	private int getRandomCoord(int offset, int max) {
		int coord = (int)((Math.random() * GameConstants.MAX_HEALTHKIT_SPAWN_RANGE) - GameConstants.MAX_HEALTHKIT_SPAWN_RANGE/2 + offset);
		
		if (coord < 0) {
			coord = 0;
		} else if (coord >= max) {
			coord = max - 1;
		}
		
		return coord;
	}
	
	public void reset() {
		list = new ArrayList<Healthkit>();
	}
}
