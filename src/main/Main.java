package main;

import static render.core.Block.SPACE;

import image.SquareTexture;
import render.core.Block;
import render.core.Camera;
import render.core.Raycaster;
import render.core.Wall;
import render.core.WorldMap;
import render.math.Vector2;

public class Main {

	public static void main(String[] args) {
		int width = 1000;
		int height = 1000;
		
		Game game = new Game(width,height).noCursor();
		Camera camera = new Camera()
			      .setPos(new Vector2(2,2))
			      .setDir(new Vector2(-0.75f,0))
			      .setPlane(new Vector2(0,0.5f));
		
		SquareTexture customTexture = new SquareTexture("assets/textures/sos1024.png",1024);
		Block block = new Block("test");
		Block custom = new Block("custom test").applyTexture(customTexture).customize(new Wall(0.25f,0.25f,0.75f,0.25f),
				   										  							  new Wall(0.75f,0.25f,0.75f,0.75f),
				   										  							  new Wall(0.75f,0.75f,0.25f,0.75f),
				   										  							  new Wall(0.25f,0.75f,0.25f,0.25f));
		
		Block[][] worldArray = {
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,custom,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
				{SPACE,SPACE,SPACE,SPACE,SPACE,SPACE},
		};
		
		SquareTexture blue = new SquareTexture("assets/textures/blue32.png",32);
		SquareTexture gray = new SquareTexture("assets/textures/gray32.png",32);
		SquareTexture red = new SquareTexture("assets/textures/red32.png",32);
		
		SquareTexture[][] floorMap = {
				{gray,gray,blue,gray,gray,gray},
				{gray,gray,blue,red,red,gray},
				{blue,blue,blue,red,red,gray},
				{gray,red,red,blue,blue,blue},
				{gray,red,red,blue,gray,gray},
				{gray,gray,gray,blue,gray,gray},
		};
		
		WorldMap world = new WorldMap(worldArray).setFloorMap(floorMap).setBorder(block);
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, 500, 500, 4);
		
		game.setRaycaster(raycaster);
		raycaster.start();
		//Messenger.post("RENDER_ENABLE");
		//game.run();
		game.startAndRun();
	}

}
