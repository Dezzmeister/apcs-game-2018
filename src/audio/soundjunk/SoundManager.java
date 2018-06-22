package audio.soundjunk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Manages different sounds that may be playing in the World. Uses a ThreadPool to 
 * play sound concurrently.
 *
 * @author Joe Desmond
 */
public class SoundManager {
	private ThreadPoolExecutor executor;
	private Map<String,SoundFile> sounds = new HashMap<String,SoundFile>();
	private static final List<Class<? extends SoundFile>> supportedTypes = new ArrayList<Class<? extends SoundFile>>();
	
	static {
		supportedTypes.add(OggFile.class);
	}
	
	public SoundManager(int threads) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
	}
	
	public void addSound(String name, String path) {
		try {
			if (sounds.size() <= executor.getMaximumPoolSize()) {
				sounds.put(name, createSoundFile(path));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Uses reflection!
	 * 
	 * @param path
	 * @return
	 * @throws UnsupportedAudioFileException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private SoundFile createSoundFile(String path) throws UnsupportedAudioFileException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (path.lastIndexOf(".") != -1) {
			String extension = path.substring(path.lastIndexOf(".")).toLowerCase();
			
			Class<? extends SoundFile> soundClass = null;
			for (Class<? extends SoundFile> c : supportedTypes) {
				String className = c.getSimpleName();
				String supportedExtension = "."+className.substring(0,className.lastIndexOf("File")).toLowerCase();
				
				if (supportedExtension.equals(extension)) {
					soundClass = c;
					break;
				}
			}
			
			if (soundClass != null) {
				Constructor<? extends SoundFile> constructor = soundClass.getDeclaredConstructor(String.class);
				SoundFile file = constructor.newInstance(path);
				return file;
			} else {
				throw new UnsupportedAudioFileException("File extension "+extension+" is not supported!");
			}
			
		} else {
			throw new UnsupportedAudioFileException("File extension not present in file "+path+"!");
		}
	}
	
	public void play(String name) {
		try {
			SoundFile file = sounds.get(name);
			executor.execute(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		executor.shutdown();
	}
	
	/**
	 * Lets the SoundManager know about a Class that can open audio files
	 * of a specific format.
	 * 
	 * @param clazz
	 */
	public static void addSupportedType(Class<? extends SoundFile> clazz) {
		supportedTypes.add(clazz);
	}
}
