package audio.soundjunk;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Wav {
	public static void playSound(String path) {
		File url = new File(path);
		try {
			Clip clip = AudioSystem.getClip();
			AudioInputStream ais = AudioSystem.getAudioInputStream(url);
			clip.open(ais);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
