package main.entities;

import java.util.ArrayList;
import java.util.List;

import image.SquareTexture;
import render.core.Camera;
import render.core.WorldMap;
import render.math.Vector2;

public class DwightList {
	private final int maxDwights = 10;
	private final int despawnDistance = 250;
	
	private List<Dwight> dwights = new ArrayList<Dwight>();
	private SquareTexture dwightTexture = new SquareTexture("assets/textures/dwight_purple.png",200);
	private Camera player;
	private WorldMap map;
	
	public DwightList(Camera _player, WorldMap _map) {
		player = _player;
		map = _map;
	}
	
	public void updateDwights() {
		for (int i = dwights.size()-1; i >= 0; i--) {
			Dwight dwight = dwights.get(i);
			if (Vector2.distance(player.pos, dwight.position) > despawnDistance) {
				dwights.remove(i);
			}
		}
		
		while (dwights.size() < maxDwights) {
			spawnRandomDwight();
		}
	}
	
	private void spawnRandomDwight() {
		
	}
}
