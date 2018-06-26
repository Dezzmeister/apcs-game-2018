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
 * SoundManager via either SoundManager's static intializer or its static
 * <code>addSupportedType()</code> method.
 *
 * @author Joe Desmond
 */
public interface SoundFile extends Runnable {
	
	/**
	 * Starts the audio from the beginning.
	 */
	void play();

	/**
	 * Stops the audio completely.
	 */
	void end();
	
	void pause();

	void resume();
	
	boolean hasEnded();
	
	SoundFile waitForFirstUpdate();
	
	void registerUpdate();
	
	/**
	 * Attempts to set the gain of the audio, if this control is supported. Gain is
	 * essentially volume, for our purposes.
	 *
	 * @param norm
	 *            gain value from <code>minGain()</code> to <code>maxGain()</code>
	 */
	void setGain(float gain);
	
	/**
	 * Returns the maximum possible gain, if this control is supported. If this
	 * control is not supported, the <code>Optional<</>Float></></code> returned
	 * will be empty.
	 */
	Optional<Float> maxGain();
	
	/**
	 * Returns the minimum possible gain, if this control is supported. If this
	 * control is not supported, the <code>Optional<</>Float></></code> returned
	 * will be empty.
	 */
	Optional<Float> minGain();

	/**
	 * Returns the maximum possible volume, if this control is supported. If this
	 * control is not supported, the <code>Optional<</>Float></></code> returned
	 * will be empty.
	 */
	Optional<Float> maxVolume();
	
	/**
	 * Returns the minimum possible volume, if this control is supported. If this
	 * control is not supported, the <code>Optional<</>Float></></code> returned
	 * will be empty.
	 */
	Optional<Float> minVolume();
	
	/**
	 * Attempts to set the left/right balance of the audio, if this control is
	 * supported.
	 *
	 * @param panValue
	 *            value from -1.0f (left) to 1.0f (right)
	 */
	void setPan(float panValue);
	
	@Override
	default void run() {
		play();
	}
}
