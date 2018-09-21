package main;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameConstants {
	public static int MAP_SIZE = 5000;
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
	
	private static Map<Class<?>, Class<?>> primitivesToWrappers = new HashMap<Class<?>, Class<?>>();
	
	static {
		fillMap();
		loadConfig();
	}
	
	private static void fillMap() {
		primitivesToWrappers.put(int.class, Integer.class);
		primitivesToWrappers.put(float.class, Float.class);
		primitivesToWrappers.put(long.class, Long.class);
	}
	
	/**
	 * insane reflection
	 */
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
					
					Class<?> type = primitivesToWrappers.get(field.getType());
					
					String conversionMethodName = "parse" + field.getType().getSimpleName().substring(0,1).toUpperCase() + field.getType().getSimpleName().substring(1);
					
					Object value = type.getDeclaredMethod(conversionMethodName, String.class).invoke(null, stringValue);
				
					field.set(null, value);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
