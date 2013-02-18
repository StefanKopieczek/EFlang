import java.io.File;
import java.io.IOException;

import org.jfugue.MusicStringParser;
import org.jfugue.Player;


public class MidiFilePerformer implements Performer {
	Player mPlayer;
	String queue;
	String instrument = "[GLOCKENSPIEL]";
	String tempo = "120";
	String control_params = "";
	
	public MidiFilePerformer() {
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
	
	@Override
	public void onPieceEnd() {
		outputQueueToFile();
	}
	
	@Override
	public void onRest() {
		
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
