package main;

import static render.core.Block.SPACE;

import render.core.Block;
import render.core.Camera;
import render.core.Raycaster;
import render.core.WorldMap;
import render.math.Vector2;

public class Main {

	public static void main(String[] args) {
		int width = 1000;
		int height = 1000;
		
		Game game = new Game(width,height);
		Camera camera = new Camera()
			      .setPos(new Vector2(2,2))
			      .setDir(new Vector2(-0.75f,0))
			      .setPlane(new Vector2(0,0.5f));
		
		Block block = new Block("test");
		Block[][] worldArray = {
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
		};
		
		WorldMap world = new WorldMap(worldArray).setBorder(block);
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, 300, 300, 4);
		
		game.setRaycaster(raycaster);
		
		game.startAndRun();
	}

}
