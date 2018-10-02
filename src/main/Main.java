package main;

import static render.core.Block.SPACE;
import static render.core.Block.DwightElements.CUBICLE_X;
import static render.core.Block.DwightElements.CUBICLE_Y;
import static render.core.Block.DwightElements.STANDARD_HALL_CEILING;
import static render.core.Block.DwightElements.STANDARD_HALL_FLOOR;
import static render.core.Block.DwightElements.STANDARD_ROOM_FLOOR;
import static render.core.Block.DwightElements.STANDARD_WALL_BLOCK;
import static render.core.WorldMap.DEFAULT_CEILING;

import audio.soundjunk.SoundManager;
import audio.soundjunk.localized.Speaker;
import game.BoundedStat;
import game.ViewModels;
import image.Entity;
import image.GeneralTexture;
import image.HUD;
import image.Item;
import image.SquareTexture;
import mapGen.MapGenerator;
import mapGen.MapGenerator.MapSpecification;
import render.core.Block;
import render.core.Camera;
import render.core.Raycaster;
import render.core.Wall;
import render.core.WorldMap;
import render.core.true3D.Model;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.RenderUtils;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

public class Main {
	//use Covington font for plaques
	
	public static final int COFFEE_MELEE_COST = 5;
	public static final int COFFEE_CANNON_COST = 10;

	public static void main(String[] args) {
		// test();
		mapGenerationTest();
		// cubicleTest();
		// Arrays.toString(map.getIntMap());
		// vectorTest();
		// matrixTest();
		// angleTest();
	}
	
	static void angleTest() {
		Wall X_AXIS = new Wall(0,0,1,0);
		
		Wall w1 = new Wall(0,0,0,1);
		Wall w2 = new Wall(0,0,0,-1);
		Wall w3 = new Wall(0,0,500,0);
		
		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w1));
		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w2));
		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w3));
	}
	
	static void matrixTest() {
		Vector3 point = new Vector3(10,25,0.5f);
		
		Matrix4 scaler = Transformer.createScaleMatrix(1, 1, 1);
		Matrix4 translator = Transformer.createTranslationMatrix(24, 0, 0);
		Matrix4 rotator = Transformer.createZRotationMatrix(1.5f);
		
		Matrix4 transform = scaler.multiply(rotator).multiply(translator);
		
		System.out.println(transform.transform(point));
	}
	
	static void mapGenerationTest() {
		int width = GameConstants.SCREEN_WIDTH;
		int height = GameConstants.SCREEN_HEIGHT;
		
		final BoundedStat health = new BoundedStat(0,GameConstants.MAX_HEALTH);
		final BoundedStat coffee = new BoundedStat(0,GameConstants.MAX_COFFEE);
		
		MapSpecification spec = new MapSpecification(STANDARD_WALL_BLOCK, STANDARD_HALL_FLOOR, STANDARD_HALL_CEILING, STANDARD_ROOM_FLOOR, STANDARD_HALL_CEILING);
		MapGenerator map = new MapGenerator(GameConstants.MAP_SIZE,GameConstants.MAP_SIZE, spec);
		map.generate();
		
		WorldMap world = map.getFinalWorldMap();
		Vector2 startPos = map.getRandomStartPos();
		
		SoundManager manager = new SoundManager();
		manager.addSound("giorgio", "assets/music/chase.ogg");
		manager.addSound("funeral", "assets/music/funeral.ogg");
		//manager.play("giorgio");
		
		Game game = new Game(width, height).noCursor();
		game.setSoundManager(manager);
		game.setHealthStat(health);
		game.setCoffeeStat(coffee);
		//String testSound = "assets/soundfx/boom.wav";
		String step1 = "assets/soundfx/footsteps/step1.wav";
		String step2 = "assets/soundfx/footsteps/step2.wav";
		String step3 = "assets/soundfx/footsteps/step3.wav";
		//game.setStepPaths(step1, step2, step3);
		
		Camera camera = new Camera().setPos(startPos).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));
		
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, GameConstants.RENDER_SIZE, GameConstants.RENDER_SIZE, GameConstants.RAYCAST_THREADS);
		raycaster.setShadeType(GameConstants.SHADE_TYPE);
		raycaster.enableTrue3DTextures();
		
		game.setCurrentViewModel(ViewModels.CUP_VIEWMODEL);
		
		HUD hud = new HUD("assets/overlays/hud.png", health, coffee)
				.fitTo(HUD.Fit.BOTTOM)
				.autoFindBars()
				.autoFindTransparency();
		
		raycaster.setHUD(hud);
		
		game.setRaycaster(raycaster);
		raycaster.start();

		Thread gameThread = game.startAndRun();
	}
	
	static void cubicleTest() {
		int width = GameConstants.SCREEN_WIDTH;
		int height = GameConstants.SCREEN_HEIGHT;
		
		SoundManager manager = new SoundManager();
		
		Game game = new Game(width, height).noCursor();
		game.setSoundManager(manager);
		
		manager.addSound("hitman", "assets/music/exploration.ogg", new Speaker(4,4));
		//manager.play("hitman");
		
		Camera camera = new Camera().setPos(new Vector2(2, 2)).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));
		
		SquareTexture coffeeBean = new SquareTexture("assets/textures/small bean.png",56);
		Entity bean = new Item(coffeeBean, new Vector2(9,9),camera).setDrawableBounds(0, 0, 50, 50);
		
		SquareTexture joj = new SquareTexture("assets/textures/unnamed.jpg", 2365);
		Block gendron = new Block("gendron").applyTexture(joj).tileFront(7, 3);
		
		GeneralTexture customTexture = new GeneralTexture("assets/textures/sos1024.png", 1024, 1024);
		Block custom = new Block("custom test").customize(
				new Wall(0.25f, 0.25f, 0.75f, 0.25f).setTexture(joj.asGeneralTexture()).tile(2, 4),
				new Wall(0.75f, 0.25f, 0.75f, 0.75f).setTexture(customTexture),
				new Wall(0.75f, 0.75f, 0.25f, 0.75f).setTexture(customTexture),
				new Wall(0.25f, 0.75f, 0.25f, 0.25f).setTexture(customTexture));
		
		SquareTexture rectangles = new SquareTexture("assets/textures/sos1024.png", 1024);
		Block block = new Block("test").applyTexture(rectangles);
		
		Triangle testTriangle = new Triangle(new Vector3(0.2f, 0.2f, 0.2f), 
											 new Vector3(0.4f, 0.5f, 0.4f),
											 new Vector3(0.4f, 0.7f, 0.5f),
											 0xFF00FF00);
		Model model = new Model(testTriangle);
		
		GeneralTexture testTexture = new GeneralTexture("assets/textures/medavoy.png",216,288);
		
		Triangle wall1 = new Triangle(new Vector3(0.25f, 0.25f, 0.0f),
									  new Vector3(0.75f, 0.25f, 0.0f),
									  new Vector3(0.50f, 0.50f, 0.4f),
									  0xFF00FF00)
									  .setUVCoords(new Vector2(0,1), 
											  	   new Vector2(1,1),
											  	   new Vector2(0.5f, 0))
									  .setTexture(testTexture);
		
		
		Triangle wall2 = new Triangle(new Vector3(0.25f,0.25f,0.0f),
									  new Vector3(0.25f,0.75f,0.0f),
									  new Vector3(0.50f,0.50f,0.4f),
									  0xFF0000FF);
		
		Model testWalls = new Model(wall1,wall2).transform(GameConstants.getAspectScaleMatrix());
		
		Block testModel = new Block("testModel").defineAsModel(testWalls);
		
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
						SPACE, testModel, SPACE, SPACE, SPACE, SPACE},
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
						SPACE, SPACE, block, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, Block.DEFLECTOR, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, block, Block.DwightElements.TABLE_BLOCK, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, gendron, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}};
		
		WorldMap world = new WorldMap(blocks).setBorder(block);
		
		final BoundedStat health = new BoundedStat(0,100);
		final BoundedStat coffee = new BoundedStat(0,100);
		
		game.setHealthStat(health);
		game.setCoffeeStat(coffee);
		game.setCurrentViewModel(ViewModels.CUP_VIEWMODEL);
		
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, GameConstants.RENDER_SIZE, GameConstants.RENDER_SIZE, 4);
		raycaster.setShadeType(GameConstants.SHADE_TYPE);
		
		HUD hud = new HUD("assets/overlays/hud.png", health, coffee)
				.fitTo(HUD.Fit.BOTTOM)
				.autoFindBars()
				.autoFindTransparency();
		
		raycaster.setHUD(hud);
		
		//world.addEntity(bean);
		
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
