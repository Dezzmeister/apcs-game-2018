package audio;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Enum for sound effects (.wav) Play using play()
 *
 * @author Dan Hagborg //not Austin
 * @version 1.0
 * @since 2018-6-18
 */
public enum SoundEffect {
	// TODO: add file names
	THROW_COFFEE(null), COLLECT_COFFEE_BEAN(null), GET_HIT(null), PAUSE(null);

	private Clip clip;

	private SoundEffect(String fileName) {
		try {
			URL url = this.getClass().getClassLoader().getResource(fileName);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (clip.isRunning()) {
			clip.stop();
		}
		clip.setFramePosition(0);
		clip.start();
		
	}
}
