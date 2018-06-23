package audio;

import javax.sound.midi.Sequence;

/**
 * Enum for songs (.mid) Play using MusicPlayer.m.setSong(song)
 *
 * @author Dan Hagborg //not Austin
 * @version 1.0
 * @since 2018-6-18
 */
public enum Song {
	GAME, TITLE, WIN, LOSE;
	
	public Sequence getSequence() {
		return SONGS[ordinal()];
	}
	
	private static final Sequence[] SONGS = new Sequence[] {
			// TODO: add file names
			// ex. MidiSystem.getSequence(new File("song.mid"));
			null, // GAME
			null, // TITLE
			null, // WIN
			null // LOSE
	};
}
