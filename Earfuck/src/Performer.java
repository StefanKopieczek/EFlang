import java.io.File;
import java.io.IOException;

import org.jfugue.MusicStringParser;
import org.jfugue.Pattern;
import org.jfugue.Player;

class Performer {
	Player mPlayer;
	String queue;
	String instrument = "[GLOCKENSPIEL]";
	String tempo = "120";
	String control_params = "";
	
	public Performer() {
		mPlayer = new Player();
		refreshQueue();
	}
	
	public void play(String note, float frac) {
		Pattern pattern = new Pattern("I" + instrument + " " +
				                      "T" + tempo + " " +
				                      control_params + " " +
				                      note + "/" + frac);
		mPlayer.play(pattern);
		
	}
	
	public void pause() {
		mPlayer.pause();
	}
	
	public void resume() {
		mPlayer.resume();
	}
	
	public void outputQueueToFile() {
		try {
			mPlayer.saveMidi(queue, new File("output.mid"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	public void refreshQueue() {
		queue = "I" + instrument + " " +
                "T" + tempo + " " +
                control_params + " ";
	}
	
	public void enqueue(String note, float frac) {
		queue += note + "/" + frac + " ";
	}
	
	public void enqueue(String note, float frac, int velocity) {
		queue += note + "/" + frac + "a" + velocity + " ";
	}
	
	public void playQueue() {
		Pattern pattern = new Pattern(queue);
		mPlayer.play(pattern);
		refreshQueue();
	}
	
	public void setTempo(int t) {
		tempo = String.valueOf(t);
	}
	
	int getNoteValue(String a) {
		return MusicStringParser.getNote(a).getValue();
	}
}