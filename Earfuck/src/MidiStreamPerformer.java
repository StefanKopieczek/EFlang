import java.io.File;
import java.io.IOException;

import org.jfugue.MusicStringParser;
import org.jfugue.Pattern;
import org.jfugue.Player;

class MidiStreamPerformer implements Performer{
	Player mPlayer;
	String queue;
	String instrument = "[GLOCKENSPIEL]";
	String tempo = "120";
	String control_params = "";
	
	public MidiStreamPerformer() {
		mPlayer = new Player();
		refreshQueue();
	}
	
	
	public void outputQueueToFile() {
		try {
			mPlayer.saveMidi(queue, new File("output.mid"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refreshQueue() {
		queue = "I" + instrument + " " +
                "T" + tempo + " " +
                control_params + " ";
	}
	
	@Override
	public void addNote(String note, float frac) {
		queue += note + "/" + frac + " ";
	}
	
	@Override
	public void addNote(String note, float frac, int excitement) {
		queue += note + "/" + frac + "a" + excitement + " ";
	}
	
	private void playQueue() {
		Pattern pattern = new Pattern(queue);
		mPlayer.play(pattern);
		refreshQueue();
	}
	
	@Override
	public void onPieceEnd() {
		playQueue();
	}
	
	@Override
	public void onRest() {
		playQueue();
	}
	
	@Override
	public void setTempo(int tempoInt) {
		tempo = String.valueOf(tempoInt);
		queue += "T" + tempo + " ";
	}
	
	int getNoteValue(String a) {
		return MusicStringParser.getNote(a).getValue();
	}


	@Override
	public void onPieceStart() {

	}


	@Override
	public void changeInstrument(String instrumentString) {
		instrument = instrumentString;
		queue += "I" + instrument + " ";
	}
}