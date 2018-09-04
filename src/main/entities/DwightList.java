package main.entities;

import java.util.ArrayList;
import java.util.List;

import image.Entity;
import render.core.Block;
import render.core.Camera;
import render.core.WorldMap;
import render.math.Vector2;

public class DwightList {
	private final int maxDwights = 10;
	
	private final int despawnDistance = 50;
	
	private List<Dwight> dwights = new ArrayList<Dwight>();
	private Camera player;
	private WorldMap world;
	
	public DwightList(Camera _player, WorldMap _world) {
		player = _player;
		world = _world;
		
		generateLUTs();
	}
	
	private boolean cannotSpawnNewDwight = false;
	
	public void updateDwights() {
		moveDwights();
		
		for (int i = dwights.size()-1; i >= 0; i--) {
			Dwight dwight = dwights.get(i);
			if (Vector2.distance(player.pos, dwight.pos) > despawnDistance) {
				dwights.remove(i);
				cannotSpawnNewDwight = false;
			}
		}
		
		lastInterval = 0;
		lastRadius = initialSpawnRadius;
		while (!cannotSpawnNewDwight && dwights.size() < maxDwights && spawnRandomDwight());
	}
	
	public void moveDwights() {
		for (int i = dwights.size() - 1; i >= 0; i--) {
			
		}
	}
	
	public List<Dwight> getDwights() {
		return dwights;
	}
	
	private int initialSpawnRadius = 10;
	private int maxSpawnRadius = 18;
	
	private final float TWO_PI = (float) (Math.PI * 2.0f);
	private float[] sineTable;
	private float[] cosineTable;
	
	private int dwightSpawnInterval = 10;
	
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
	private int lastRadius = initialSpawnRadius;
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
		for (int radius = lastRadius; radius < maxSpawnRadius; radius+=2) {
			lastRadius += 2;
			
			for (int i = lastInterval; i < dwightSpawnInterval; i++) {
				lastInterval = (i + 1 >= dwightSpawnInterval) ? 0 : i + 1;
				
				int x = (int)(radius * cosineTable[i]) + px;
				int y = (int)(radius * sineTable[i]) + py;
				
				if (!world.getBlockAt(x,y).isSolid()) {
					addDwight(x + 0.5f, y + 0.5f);
					return true;
				}
			}
		}
		
		cannotSpawnNewDwight = true;
		return false;
	}
	
	private void addDwight(float x, float y) {
		Dwight dwight = new Dwight(new Vector2(x, y));
		System.out.println(dwight.pos);
		dwight.setCamera(player);
		
		dwights.add(dwight);
	}
}
