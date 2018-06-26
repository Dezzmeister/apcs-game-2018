package audio.soundjunk;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.SourceDataLine;

/**
 * Represents a general implementation of any sound that is played through a
 * SourceDataLine.
 *
 * @author Joe Desmond
 */
abstract class JavaSoundFile implements SoundFile {

	protected String path;
	protected SourceDataLine line;
	protected boolean ended = false;
	protected final AtomicBoolean paused = new AtomicBoolean(false);
	protected volatile boolean firstUpdate = false;
	protected boolean waitingForFirstUpdate = false;
	
	public JavaSoundFile(String _path) {
		path = _path;
	}
	
	@Override
	public boolean hasEnded() {
		return ended;
	}
	
	@Override
	public void end() {
		ended = true;
	}

	@Override
	public void pause() {
		if (line != null) {
			line.stop();
		}
		paused.set(true);
	}

	@Override
	public void resume() {
		if (line != null) {
			line.start();
		}
		paused.set(false);
	}
	
	@Override
	public SoundFile waitForFirstUpdate() {
		waitingForFirstUpdate = true;
		return this;
	}
	
	@Override
	public void registerUpdate() {
		firstUpdate = true;
	}

	/**
	 * Sets the gain of the audio, taking maximum and minimun gain values into
	 * account. Accepts a normalized value.
	 */
	@Override
	public void setGain(float _gain) {
		if (line != null) {
			FloatControl gain = (FloatControl) line.getControl(Type.MASTER_GAIN);
			float min = gain.getMinimum();
			float max = gain.getMaximum();
		
			if (_gain > min && _gain < max) {
				gain.setValue(_gain);
			} else {
				System.out.println("Gain value " + _gain + " must be between " + max + " and " + min + "!");
			}
		}
	}

	@Override
	public Optional<Float> maxGain() {

		return Optional.ofNullable(((FloatControl) line.getControl(Type.MASTER_GAIN)).getMaximum());
	}

	@Override
	public Optional<Float> minGain() {
		return Optional.ofNullable(((FloatControl) line.getControl(Type.MASTER_GAIN)).getMinimum());
	}
	
	@Override
	public Optional<Float> maxVolume() {
		return Optional.ofNullable(((FloatControl) line.getControl(Type.VOLUME)).getMaximum());
	}
	
	@Override
	public Optional<Float> minVolume() {
		return Optional.ofNullable(((FloatControl) line.getControl(Type.VOLUME)).getMinimum());
	}

	@Override
	public void setPan(float panValue) {
		if (panValue >= -1.0f && panValue <= 1.0f && line != null) {
			((FloatControl) line.getControl(Type.PAN)).setValue(panValue);
		}
	}
}
