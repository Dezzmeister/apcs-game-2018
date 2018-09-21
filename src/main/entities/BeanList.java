package main.entities;

import java.util.ArrayList;
import java.util.List;

import audio.soundjunk.Wav;
import game.BoundedStat;
import main.GameConstants;
import render.core.Camera;
import render.core.WorldMap;
import render.math.Vector2;

public class BeanList {
	private List<Bean> beans = new ArrayList<Bean>();
	private Camera player;
	private WorldMap world;
	private BoundedStat coffee;
	
	public BeanList(Camera _player, WorldMap _world, BoundedStat _coffee) {
		player = _player;
		world = _world;
		coffee = _coffee;
	}
	
	public void setCoffeeStat(BoundedStat _coffee) {
		coffee = _coffee;
	}
	
	private float pickupDistance = GameConstants.PLAYER_PICKUP_DISTANCE;
	private int despawnDistance = GameConstants.BEAN_DESPAWN_DISTANCE;
	private int maxSpawnRadius = GameConstants.MAX_BEAN_SPAWN_RADIUS;
	private int minSpawnRadius = GameConstants.MIN_BEAN_SPAWN_RADIUS;
	private int beanSpawnChance = GameConstants.BEAN_SPAWN_CHANCE;
	private int maxBeans = GameConstants.MAX_BEANS;
	
	public void update() {
		for (int i = beans.size() - 1; i >= 0; i--) {
			Bean bean = beans.get(i);
			
			float d = Vector2.distance(player.pos, bean.pos);
			
			if (d > despawnDistance) {
				beans.remove(i);
			}
			if (d <= pickupDistance) {
				coffee.gain(8);
				beans.remove(i);
				Wav.playSound("assets/soundfx/click.wav");
			}
		}
		
		if (beans.size() < maxBeans - (maxBeans/4)) {
			addRandomBeans();
		}
	}
	
	public List<Bean> getBeans() {
		return beans;
	}
	
	private void addRandomBeans() {
		int px = (int)player.pos.x;
		int py = (int)player.pos.y;
		
		for (int y = py - maxSpawnRadius/2; y < py + maxSpawnRadius/2; y++) {
			if (y >= 0 && y < world.height && Math.abs(py - y) > minSpawnRadius/2) {
				for (int x = px - maxSpawnRadius/2; x < px + maxSpawnRadius/2; x++) {
					if (x >= 0 && x < world.width && Math.abs(px - x) > minSpawnRadius/2) {
						
						if (!world.getBlockAt(x, y).isSolid() && !beanExistsAt(x,y)) {
							int rand = (int)(Math.random() * beanSpawnChance);
							
							if (rand == beanSpawnChance - 1) {
								beans.add(new Bean(new Vector2(x + 0.5f, y + 0.5f), player));
							}
							
							if (beans.size() >= maxBeans) {
								return;
							}
						}
					}
				}
			}
		}
	}
	
	private boolean beanExistsAt(int x, int y) {
		for (Bean b : beans) {
			Vector2 vec = b.pos;
			if (vec.x == x + 0.5f && vec.y == y + 0.5f) {
				return true;
			}
		}
		
		return false;
	}
}
