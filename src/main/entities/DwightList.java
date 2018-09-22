package main.entities;

import java.util.ArrayList;
import java.util.List;

import audio.soundjunk.Wav;
import image.Entity;
import main.GameConstants;
import render.core.Block;
import render.core.Camera;
import render.core.WorldMap;
import render.math.Vector2;

public class DwightList {
	public int maxDwights = GameConstants.MAX_DWIGHTS;
	
	private final int despawnDistance = GameConstants.DWIGHT_DESPAWN_DISTANCE;
	
	private int initialSpawnRadius = GameConstants.INITIAL_DWIGHT_SPAWN_RADIUS;
	private int maxSpawnRadius = GameConstants.MAX_DWIGHT_SPAWN_RADIUS;
	
	private final float TWO_PI = (float) (Math.PI * 2.0f);
	private float[] sineTable;
	private float[] cosineTable;
	
	private int dwightSpawnInterval = GameConstants.DWIGHT_SPAWN_INTERVAL;
	
	private List<Dwight> dwights = new ArrayList<Dwight>();
	private Camera player;
	private WorldMap world;
	
	private int dwightsKilled = 0;
	
	public DwightList(Camera _player, WorldMap _world) {
		player = _player;
		world = _world;
		
		generateLUTs();
	}
	
	public void updateDwights(double delta) {
		moveDwights(delta);
		
		for (int i = dwights.size()-1; i >= 0; i--) {
			Dwight dwight = dwights.get(i);
			
			if (Vector2.distance(player.pos, dwight.pos) > despawnDistance) {
				dwights.remove(i);
			}
			
			if (dwight.health.get() <= 0) {
				dwightsKilled++;
				Dwight removed = dwights.remove(i);
				if (world.getFloorAt((int)removed.pos.x, (int)removed.pos.y) == Block.DwightElements.STANDARD_HALL_FLOOR) {
					world.setFloorAt((int)removed.pos.x, (int)removed.pos.y, Block.DwightElements.BLOOD_FLOOR);
				}
				Wav.playSound("assets/soundfx/scream.wav");
			}
		}
		
		while (dwights.size() < maxDwights && spawnRandomDwight());
	}
	
	public static final float COFFEE_POUR_RANGE = 2.5f;
	
	public void coffeePour(List<Entity> hitDwights, float closestWall) {
		Vector2 pos = player.pos;
		
		float distance = closestWall;
		Dwight hitDwight = null;
		
		for (int i = 0; i < hitDwights.size(); i++) {
			
			if (hitDwights.get(i) instanceof Dwight) {
				Dwight dwight = (Dwight) hitDwights.get(i);
			
				float d = Vector2.distance(pos, dwight.pos);
			
				if (d < distance && d <= COFFEE_POUR_RANGE) {
					distance = d;
					hitDwight = dwight;
				}
			}
		}
		
		if (hitDwight != null && distance != closestWall) {
			hitDwight.health.lose(50);
		}
	}
	
	public static final float DWIGHT_ATTACK_RANGE = GameConstants.DWIGHT_ATTACK_RANGE;
	
	public boolean playerIsHit() {
		for (int i = 0; i < dwights.size(); i++) {
			Dwight dwight = dwights.get(i);
			
			if (Vector2.distance(player.pos,dwight.pos) <= DWIGHT_ATTACK_RANGE) {
				return true;
			}
		}
		
		return false;
	}
	
	public void coffeeCannon(List<Entity> hitDwights, float closestWall) {
		Vector2 pos = player.pos;
		
		for (int i = 0; i < hitDwights.size(); i++) {
			
			if (hitDwights.get(i) instanceof Dwight) {
				Dwight dwight = (Dwight) hitDwights.get(i);
			
				float d = Vector2.distance(pos, dwight.pos);
			
				if (d < closestWall) {
					dwight.health.lose(50);
				}
			}
		}
	}
	
	public int getDwightsKilled() {
		return dwightsKilled;
	}
	
	public void regeneratePaths() {
		for (int i = dwights.size() - 1; i >= 0; i--) {
			dwights.get(i).generatePath(player.pos, world);
		}
	}
	
	public void moveDwights(double delta) {
		for (int i = dwights.size() - 1; i >= 0; i--) {
			dwights.get(i).moveToPlayer(delta);
		}
	}
	
	public List<Dwight> getDwights() {
		return dwights;
	}
	
	private void generateLUTs() {
		sineTable = new float[dwightSpawnInterval];
		cosineTable = new float[dwightSpawnInterval];
		
		float theta = 0;
		float interval = TWO_PI/dwightSpawnInterval;
		for (int i = 0; i < dwightSpawnInterval; i++) {
			sineTable[i] = (float) Math.sin(theta);
			cosineTable[i] = (float) Math.cos(theta);
			
			theta += interval;
		}
	}
	
	private int lastInterval = 0;
	private int spawnTestRadius = 5;
	private boolean spawnRandomDwight() {
		int px = (int)player.pos.x;
		int py = (int)player.pos.y;
		
		/*
		for (int radius = initialSpawnRadius; radius < maxSpawnRadius; radius++) {
			for (int x = px - radius; x <= px; x++) {
				for (int y = py - radius; y <= py; y++) {
					int clause = ((x - px) * (x - px)) + ((y - py) * (y - py));
					
					if (clause <= radius * radius && clause >= (radius - 1) * (radius - 1)) {
						int xSym = px - (x - px);
						int ySym = py - (y - py);
						//(x,y) (x,ySym) (xSym, y) (xSym, ySym) are in circle
						
					}
				}
			}
		}
		*/
		for (int radius = initialSpawnRadius; radius < maxSpawnRadius; radius+=2) {
			
			for (int i = lastInterval; i < dwightSpawnInterval; i++) {
				lastInterval++;
				if (lastInterval >= dwightSpawnInterval) {
					lastInterval = 0;
				}
				
				int x = (int)((radius * cosineTable[i]) + px);
				int y = (int)((radius * sineTable[i]) + py);
				
				
				if (!world.getBlockAt(x,y).isSolid() && !dwightExistsAt(x,y)) {
					addDwight(x + 0.5f, y + 0.5f);
					return true;
				} else {					
					for (int tx = x - spawnTestRadius; tx <= x; tx++) {
						for (int ty = y - spawnTestRadius; ty <= y; ty++) {
							int clause = ((tx - x) * (tx - x)) + ((ty - y) * (ty - y));
							
							if (clause <= spawnTestRadius * spawnTestRadius) {
								int xSym = x - (tx - x);
								int ySym = y - (ty - y);
								//(tx,ty) (tx,ySym) (xSym, ty) (xSym, ySym) are in circle
								
								if (!world.getBlockAt(tx, ty).isSolid() && !dwightExistsAt(tx,ty)) {
									addDwight(tx + 0.5f, ty + 0.5f);
									return true;
								} else if (!world.getBlockAt(tx, ySym).isSolid() && !dwightExistsAt(tx,ySym)) {
									addDwight(tx + 0.5f, ySym + 0.5f);
									return true;
								} else if (!world.getBlockAt(xSym, ty).isSolid() && !dwightExistsAt(xSym,ty)) {
									addDwight(xSym + 0.5f, ty + 0.5f);
									return true;
								} else if (!world.getBlockAt(xSym, ySym).isSolid() && !dwightExistsAt(xSym,ySym)) {
									addDwight(xSym + 0.5f, ySym + 0.5f);
									return true;
								}
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	private boolean dwightExistsAt(int x, int y) {
		for (Dwight d : dwights) {
			Vector2 vec = d.pos;
			if (vec.x == x + 0.5f && vec.y == y + 0.5f) {
				return true;
			}
		}
		
		return false;
	}
	
	private void addDwight(float x, float y) {
		Dwight dwight = new Dwight(new Vector2(x, y), player);
		dwight.setCamera(player);
		dwight.generatePath(player.pos, world);
		
		dwights.add(dwight);
	}
}
