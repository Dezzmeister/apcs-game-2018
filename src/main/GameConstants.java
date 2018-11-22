package main;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import render.core.Raycaster;
import render.core.true3D.Transformer;
import render.math.Matrix4;

public final class GameConstants {
	
	public static int RENDER_SIZE = 300;
	public static int SCREEN_WIDTH = 1500;
	public static int SCREEN_HEIGHT = 1000;
	public static int RAYCAST_THREADS = 4;
	public static Raycaster.ShadeType SHADE_TYPE = Raycaster.ShadeType.QUADRATIC;

	public static int MAP_SIZE = 5000;
	/*
	 * A seed of -1 tells the game to generate a new map
	 */
	public static long MAP_SEED = -1;
	
	public static int MAX_HEALTH = 100;
	public static int MAX_COFFEE = 100;
	
	public static int MAX_BEANS = 30;
	public static int BEAN_SPAWN_CHANCE = 15;
	public static int MAX_BEAN_SPAWN_RADIUS = 22;
	public static int MIN_BEAN_SPAWN_RADIUS = 14;
	public static int BEAN_DESPAWN_DISTANCE = 25;
	public static float PLAYER_PICKUP_DISTANCE = 0.5f;
	
	public static int MAX_DWIGHTS = 5;
	public static int DWIGHT_DESPAWN_DISTANCE = 26;
	public static int INITIAL_DWIGHT_SPAWN_RADIUS = 20;
	public static int MAX_DWIGHT_SPAWN_RADIUS = 24;
	public static int DWIGHT_SPAWN_INTERVAL = 10;
	public static float DWIGHT_ATTACK_RANGE = 0.8f;
	public static float DWIGHT_SPEED = 0.03f;
	
	public static int MAX_HEALTHKITS = 4;
	public static int HEALTHKIT_DESPAWN_DISTANCE = 26;
	public static float DEFAULT_HEALTHKIT_CYLINDER_RADIUS = 0.45f;
	public static int HEALTHKIT_HEALTH_VALUE = 25;
	public static int MAX_HEALTHKIT_SPAWN_RANGE = 250;
	
	public static int MAX_GOAL_BLOCK_DISTANCE = 500;
	public static int MIN_GOAL_BLOCK_DISTANCE = 300;

	public static float TRUE_3D_MAX_RENDER_DISTANCE = 1000;

	private static Map<Class<?>, Class<?>> typeMap = new HashMap<Class<?>, Class<?>>();

	static {
		fillMap();
		loadConfig();
	}

	private static void fillMap() {
		typeMap.put(int.class, Integer.class);
		typeMap.put(float.class, Float.class);
		typeMap.put(long.class, Long.class);
	}

	public static Matrix4 getAspectScaleMatrix() {
		final float ASPECT = SCREEN_WIDTH / (float) SCREEN_HEIGHT;

		return Transformer.createScaleMatrix(1, 1, ASPECT);
	}

	/**
	 * insane reflection
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void loadConfig() {
		String path = "config/config.txt";

		try {
			List<String> file = Files.readAllLines(Paths.get(path));

			if (!file.get(0).equals("joj")) {
				System.err.println("first line of config file must be \"joj\" you idiot");
				System.exit(0);
			}

			for (String s : file) {
				if (s.contains("=")) {
					String name = s.substring(0, s.indexOf("="));
					String stringValue = s.substring(s.indexOf("=") + 1);
					
					Field field = GameConstants.class.getDeclaredField(name);

					Class<?> type = typeMap.get(field.getType());

					if (type == null) {
						if (Enum.class.isAssignableFrom(field.getType())) {
							field.set(null, Enum.valueOf((Class<Enum>) field.getType(), stringValue));
						}
					} else {
						String conversionMethodName = "parse"
								+ field.getType().getSimpleName().substring(0, 1).toUpperCase()
								+ field.getType().getSimpleName().substring(1);
						Object value = type.getDeclaredMethod(conversionMethodName, String.class).invoke(null,
								stringValue);
						field.set(null, value);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
