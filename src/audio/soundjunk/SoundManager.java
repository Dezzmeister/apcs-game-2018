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

import audio.soundjunk.localized.Localizer;
import audio.soundjunk.localized.Speaker;

/**
 * Manages different sounds that may be playing in the World. Uses a ThreadPool
 * to play sound concurrently. A SoundManager gives users access to its sound through
 * String identifiers. Users should not directly reference the actual sound objects.
 *
 * @author Joe Desmond
 */
public class SoundManager {
	
	private static final List<Class<? extends SoundFile>> supportedTypes = new ArrayList<Class<? extends SoundFile>>();
	private final ThreadPoolExecutor executor;
	private final Map<String, SoundFile> sounds = new HashMap<String, SoundFile>();
	private final Map<String, List<Speaker>> speakers = new HashMap<String, List<Speaker>>();
	private final Localizer localizer = new Localizer();
	
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
			sounds.put(name, createSoundFile(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addSound(String name, String path, Speaker initial) {
		try {
			sounds.put(name, createSoundFile(path));
			List<Speaker> list = new ArrayList<Speaker>();
			list.add(initial);
			speakers.put(name,list);
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
	
	/**
	 * Starts the audio from the beginning.
	 * 
	 * @param name
	 */
	public void play(String name) {
		try {
			SoundFile file = sounds.get(name);
			executor.execute(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops the audio without destroying its thread.
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
	
	/**
	 * Attempts to set the gain of the audio, if this control is supported. Gain is essentially
	 * volume, for our purposes.
	 * 
	 * @param name
	 * 			Name of the audio
	 * @param norm
	 * 			gain value from <code>minGain()</code> to <code>maxGain()</code>
	 */
	public synchronized void setGain(String name, float norm) {
		try {
			sounds.get(name).setGain(norm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the maximum possible gain, if this control is supported. If this control
	 * is not supported, the <code>Optional<</>Float></></code> returned will be empty.
	 * 
	 * @param name
	 * 			Name of the audio
	 */
	public synchronized Optional<Float> maxGain(String name) {
		try {
			return sounds.get(name).maxGain();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	/**
	 * Returns the minimum possible gain, if this control is supported. If this control
	 * is not supported, the <code>Optional<</>Float></></code> returned will be empty.
	 * 
	 * @param name
	 * 			Name of the audio
	 */
	public synchronized Optional<Float> minGain(String name) {
		try {
			return sounds.get(name).minGain();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	/**
	 * Returns the maximum possible volume, if this control is supported. If this control
	 * is not supported, the <code>Optional<</>Float></></code> returned will be empty.
	 * 
	 * @param name
	 * 			Name of the audio
	 */
	public synchronized Optional<Float> maxVolume(String name) {
		try {
			return sounds.get(name).maxVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	/**
	 * Returns the minimum possible volume, if this control is supported. If this control
	 * is not supported, the <code>Optional<</>Float></></code> returned will be empty.
	 * 
	 * @param name
	 * 			Name of the audio
	 */
	public synchronized Optional<Float> minVolume(String name) {
		try {
			return sounds.get(name).minVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	/**
	 * Attempts to set the left/right balance of the audio, if this control is supported.
	 * 
	 * @param name
	 * 			Name of the audio
	 * @param panValue
	 * 			value from -1.0f (left) to 1.0f (right)
	 */
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
	 * specific format. For example, if you were to write a class to handle MP3 sound files,
	 * you would first pass it to SoundManager via this method before attempting to load any
	 * MP3 files.
	 *
	 * @param clazz
	 * 			Audio class
	 * @see SoundFile
	 */
	public static void addSupportedType(Class<? extends SoundFile> clazz) {
		supportedTypes.add(clazz);
	}
	
	public Localizer getLocalizer() {
		return localizer;
	}
}
