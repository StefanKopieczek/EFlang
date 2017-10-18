package eflang.core;
import org.jfugue.Player;

import java.io.File;
import java.io.IOException;


public class MidiFilePerformer implements Performer {
    private Player mPlayer;
    private String queue;
    private byte instrument;
    private String tempo = "120";
    private static String control_params = "";

    public MidiFilePerformer(byte instrumentCode) {
        instrument = instrumentCode;
        mPlayer = new Player();
        refreshQueue();
    }

    private void outputQueueToFile() {
        try {
            mPlayer.saveMidi(queue, new File("output.mid"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshQueue() {
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

    @Override
    public int getTempo() {
        return Integer.parseInt(tempo);
    }

    @Override
    public void onPieceStart() {

    }

    @Override
    public void changeInstrument(byte instrumentCode) {
        instrument = instrumentCode;
        queue += "I" + instrument + " ";
    }
}
