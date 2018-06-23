package audio.soundjunk;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Represents a Vorbis .ogg file. This class should only be used by
 * SoundManager, therefore it is package-private.
 *
 * @author Joe Desmond
 */
class OggFile implements SoundFile {
	
	private String path;
	private AudioInputStream din;
	private SourceDataLine line;
	private volatile boolean ended = false;
	private volatile AtomicBoolean paused = new AtomicBoolean(false);

	public OggFile(String _path) {
		path = _path;
	}

	@Override
	public synchronized void play() {
		ended = false;
		try {
			File file = new File(path);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);

			if (in != null) {
				AudioFormat baseFormat = in.getFormat();
				AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
						16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
				din = AudioSystem.getAudioInputStream(decodedFormat, in);
				rawPlay(decodedFormat);
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/**
	 * Sets the gain of the audio, taking maximum and minimun gain values into
	 * account. Accepts a normalized value.
	 */
	@Override
	public void setGain(float _gain) {
		FloatControl gain = (FloatControl) line.getControl(Type.MASTER_GAIN);
		float min = gain.getMinimum();
		float max = gain.getMaximum();
		
		if (_gain > min && _gain < max) {
			gain.setValue(_gain);
		} else {
			System.out.println("Gain value " + _gain + " must be between " + max + " and " + min + "!");
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
		if (panValue >= -1.0f && panValue <= 1.0f) {
			((FloatControl) line.getControl(Type.PAN)).setValue(panValue);
		}
	}

	// @Override
	// public float

	private void rawPlay(AudioFormat targetFormat) throws IOException, LineUnavailableException, InterruptedException {
		byte[] data = new byte[4096];
		line = getLine(targetFormat);

		if (line != null) {
			line.start();
			int bytesRead = 0;
			int bytesWritten = 0;

			while (bytesRead != -1 && !ended) {

				if (!paused.get()) {
					bytesRead = din.read(data, 0, data.length);
					if (bytesRead != -1) {
						bytesWritten = line.write(data, 0, bytesRead);
					}
				}
			}

			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(audioFormat);
		return line;
	}
}
