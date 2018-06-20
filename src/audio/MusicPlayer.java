package audio;

import java.io.FileInputStream;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import javazoom.jl.player.advanced.AdvancedPlayer;

/**
 * Used with Song for playing music (.mid). Can load .mp3 as well,
 * by means of the static <code>loadAndGetMP3()</code> method.
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
	
	public static AdvancedPlayer loadAndGetMP3(String path) {
		AdvancedPlayer player = null;
		
		try {
			FileInputStream file = new FileInputStream(path);
			player = new AdvancedPlayer(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return player;
	}
	
	public static MusicPlayer m = new MusicPlayer();
	
}
