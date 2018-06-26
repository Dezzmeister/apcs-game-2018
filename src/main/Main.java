package main;

import static render.core.Block.SPACE;
import static render.core.Block.CubicleWalls.CUBICLE_X;
import static render.core.Block.CubicleWalls.CUBICLE_Y;
import static render.core.WorldMap.DEFAULT_CEILING;
import static render.math.Vector3.ORIGIN;

import audio.soundjunk.SoundManager;
import audio.soundjunk.localized.Speaker;
import image.GeneralTexture;
import image.SquareTexture;
import render.core.Block;
import render.core.Camera;
import render.core.Raycaster;
import render.core.Wall;
import render.core.WorldMap;
import render.math.Vector2;
import render.math.Vector3;

public class Main {

	public static void main(String[] args) {
		// test();
		cubicleTest();
		// vectorTest();
	}
	
	static void vectorTest() {
		Vector3 vec = new Vector3(3, 8, 5);
		System.out.println(Vector3.distance(ORIGIN, vec));
		vec = vec.normalize();
		System.out.println(Vector3.distance(ORIGIN, vec));
	}
	
	static void cubicleTest() {
		int width = 1000;
		int height = 1000;

		SoundManager manager = new SoundManager();

		Game game = new Game(width, height).noCursor();
		game.setSoundManager(manager);
		
		manager.addSound("hitman", "assets/music/exploration.ogg", new Speaker(4,4));
		manager.play("hitman");

		Camera camera = new Camera().setPos(new Vector2(2, 2)).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));
		
		SquareTexture rectangles = new SquareTexture("assets/textures/sos1024.png", 1024);
		Block block = new Block("test").applyTexture(rectangles);
		
		Block[][] blocks = {
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, CUBICLE_Y, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, CUBICLE_X, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, block, SPACE, block, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, block, SPACE, block, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}};
		
		WorldMap world = new WorldMap(blocks).setBorder(block);
		
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, 500, 500, 4);
		
		game.setRaycaster(raycaster);
		raycaster.start();

		Thread gameThread = game.startAndRun();

		//sleep(1000);
		//manager.setGain("hitman", -30);

	}
	
	static void test() {
		int width = 1000;
		int height = 1000;
		
		Game game = new Game(width, height).noCursor();
		Camera camera = new Camera().setPos(new Vector2(2, 2)).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));
		
		GeneralTexture customTexture = new GeneralTexture("assets/textures/sos1024.png", 1024, 1024);
		SquareTexture joj = new SquareTexture("assets/textures/unnamed.jpg", 2365);
		SquareTexture michael = new SquareTexture("assets/textures/g-dawg!!!!!!!.jpg", 1633);
		SquareTexture java = new SquareTexture("assets/textures/got java_.png", 100);
		
		Block block = new Block("test").applyTexture(michael).tileFront(10, 10).tileSide(4, 4);
		Block gendron = new Block("gendron").applyTexture(joj).tileFront(7, 3);
		Block fake = new Block("fake block").applyTexture(michael).fakeBlock().tileSide(3, 3);
		Block soup = new Block("soup").applyTexture(java);
		
		Block custom = new Block("custom test").customize(
				new Wall(0.25f, 0.25f, 0.75f, 0.25f).setTexture(michael.asGeneralTexture()).tile(2, 4),
				new Wall(0.75f, 0.25f, 0.75f, 0.75f).setTexture(customTexture),
				new Wall(0.75f, 0.75f, 0.25f, 0.75f).setTexture(customTexture),
				new Wall(0.25f, 0.75f, 0.25f, 0.25f).setTexture(customTexture));
		
		Block[][] worldArray = {{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, soup, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, gendron, SPACE, custom, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},};
		
		SquareTexture blue = new SquareTexture("assets/textures/blue32.png", 32);
		SquareTexture gray = new SquareTexture("assets/textures/gray32.png", 32);
		SquareTexture red = new SquareTexture("assets/textures/red32.png", 32);
		
		SquareTexture[][] floorMap = {{gray, gray, blue, gray, gray, gray}, {gray, gray, blue, red, red, gray},
				{blue, blue, java, red, red, gray}, {gray, red, red, blue, blue, blue},
				{gray, red, red, blue, gray, gray}, {gray, gray, gray, blue, gray, gray},};

		SquareTexture[][] ceilMap = {
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, michael, michael, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, michael, michael, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING}};
		
		WorldMap world = new WorldMap(worldArray, floorMap, ceilMap).setBorder(block);

		Raycaster raycaster = new Raycaster(game, camera, world, width, height, 400, 400, 4);
		
		game.setRaycaster(raycaster);
		raycaster.start();
		// Messenger.post("RENDER_ENABLE");
		// game.run();
		
		// Had trouble with this!!!!
		// Menu start = new Menu();
		// game.pane.add(start.getPanel());
		Thread gameThread = game.startAndRun();
		
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			
		}
	}
}
