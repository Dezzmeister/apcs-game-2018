package audio.soundjunk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Manages different sounds that may be playing in the World. Uses a ThreadPool
 * to play sound concurrently.
 *
 * @author Joe Desmond
 */
public class SoundManager {
	
	private ThreadPoolExecutor executor;
	private Map<String, SoundFile> sounds = new HashMap<String, SoundFile>();
	private static final List<Class<? extends SoundFile>> supportedTypes = new ArrayList<Class<? extends SoundFile>>();

	static {
		supportedTypes.add(OggFile.class);
	}

	public SoundManager(int threads) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
	}

	/**
	 * Creates a SoundManager with 5 threads, meaning that it will be able to play 5
	 * sounds simultaneously.
	 */
	public SoundManager() {
		this(5);
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
	 * Checks if path's file format is supported, then attempts to load the audio
	 * file at path and wrap it with an object of the class supporting its format.
	 * Uses reflection.
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
	private SoundFile createSoundFile(String path)
			throws UnsupportedAudioFileException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (path.lastIndexOf(".") != -1) {
			String extension = path.substring(path.lastIndexOf(".")).toLowerCase();

			Class<? extends SoundFile> soundClass = null;

			for (Class<? extends SoundFile> c : supportedTypes) {
				String className = c.getSimpleName();
				String supportedExtension = "." + className.substring(0, className.lastIndexOf("File")).toLowerCase();

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
				throw new UnsupportedAudioFileException("File extension " + extension + " is not supported!");
			}

		} else {
			throw new UnsupportedAudioFileException("File extension not present in file " + path + "!");
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

	/**
	 * Stops sound without destroying its thread.
	 *
	 * @param name
	 */
	public synchronized void end(String name) {
		try {
			sounds.get(name).end();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void pause(String name) {
		try {
			sounds.get(name).pause();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void resume(String name) {
		try {
			sounds.get(name).resume();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void setGain(String name, float norm) {
		try {
			sounds.get(name).setGain(norm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized Optional<Float> maxGain(String name) {
		try {
			return sounds.get(name).maxGain();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public synchronized Optional<Float> minGain(String name) {
		try {
			return sounds.get(name).minGain();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public synchronized Optional<Float> maxVolume(String name) {
		try {
			return sounds.get(name).maxVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public synchronized Optional<Float> minVolume(String name) {
		try {
			return sounds.get(name).minVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public synchronized void setPan(String name, float panValue) {
		try {
			sounds.get(name).setPan(panValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops all sound and destroys all threads.
	 */
	public void shutdown() {
		executor.shutdownNow();
		System.out.println("Shutting down all sound threads.");
	}

	/**
	 * Lets the SoundManager know about a Class that can open audio files of a
	 * specific format.
	 *
	 * @param clazz
	 */
	public static void addSupportedType(Class<? extends SoundFile> clazz) {
		supportedTypes.add(clazz);
	}
}
