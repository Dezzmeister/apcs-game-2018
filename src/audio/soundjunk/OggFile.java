package audio.soundjunk;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Represents a Vorbis .ogg file. This class should only be used by
 * SoundManager, therefore it is package-private.
 *
 * @author Joe Desmond
 */
final class OggFile extends JavaSoundFile {
	private AudioInputStream din;

	public OggFile(String _path) {
		super(_path);
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

	private void rawPlay(AudioFormat targetFormat) throws IOException, LineUnavailableException, InterruptedException {
		byte[] data = new byte[1024];
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
