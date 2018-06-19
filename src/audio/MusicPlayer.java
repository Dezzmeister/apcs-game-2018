package audio;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

/**
 * Used with Song for playing music (.mid)
 * 
 * @author Dan Hagborg //not Austin
 * @version 1.0
 * @since 2018-6-18
 */
public class MusicPlayer {
	
	private Sequencer player;
	
	public MusicPlayer() {
		try {
			player = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e) {
			player = null;
			e.printStackTrace();
		}
	}
	
	public void setSong(Song s) {
		stop();
		try {
			player.setSequence(s.getSequence());
			player.open();
			player.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void stop() {
		if (player.isRunning()) {
			player.stop();
		}
	}
	
	public static MusicPlayer m = new MusicPlayer();
	
}
