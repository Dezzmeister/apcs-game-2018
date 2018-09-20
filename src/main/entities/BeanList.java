package main.entities;

import java.util.ArrayList;
import java.util.List;

import game.BoundedStat;
import image.Entity;
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
	
	private static final float PLAYER_PICKUP_DISTANCE = 0.5f;
	private int despawnDistance = 25;
	private int maxSpawnRadius = 22;
	private int minSpawnRadius = 14;
	private int beanSpawnChance = 30;
	private int maxBeans = 30;
	
	public void update() {
		for (int i = beans.size() - 1; i >= 0; i--) {
			Bean bean = beans.get(i);
			
			float d = Vector2.distance(player.pos, bean.pos);
			
			if (d > despawnDistance) {
				beans.remove(i);
			}
			if (d <= PLAYER_PICKUP_DISTANCE) {
				coffee.gain(8);
				beans.remove(i);
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
			if (y >= 0 && y < world.height && Math.abs(py - y) > minSpawnRadius) {
				for (int x = px - maxSpawnRadius/2; x < px + maxSpawnRadius/2; x++) {
					if (x >= 0 && x < world.width && Math.abs(px - x) > minSpawnRadius) {
						if (!world.getBlockAt(x, y).isSolid() && !beanExistsAt(x,y)) {
							int rand = (int)(Math.random() * beanSpawnChance);
							
							if (rand == beanSpawnChance - 1) {
								beans.add(new Bean(new Vector2(x + 0.5f, y + 0.5f)));
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
