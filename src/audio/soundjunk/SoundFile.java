package audio.soundjunk;

import java.util.Optional;

/**
 * Represents a sound file of any format. All classes that implement this must
 * follow the naming convention:
 * <p>
 * <i>(File Extension)</i><code>File</code>
 * <p>
 * For example, a class supporting .mp3 files would be titled
 * <code>MP3File</code>, case insensitive.
 * <p>
 * Furthermore, the Class object of the format class should be added to
 * SoundManager via either SoundManager's static intializer or its
 * <code>addSupportedType()</code> method.
 *
 * @author Joe Desmond
 */
public interface SoundFile extends Runnable {

	void play();

	/**
	 * Stops the audio completely.
	 */
	void end();

	void pause();

	void resume();

	void setGain(float norm);

	Optional<Float> maxGain();

	Optional<Float> minGain();

	Optional<Float> maxVolume();

	Optional<Float> minVolume();

	void setPan(float panValue);

	@Override
	default void run() {
		play();
	}
}
