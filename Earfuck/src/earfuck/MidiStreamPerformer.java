package earfuck;

import java.io.File;
import java.io.IOException;

import org.jfugue.MusicStringParser;
import org.jfugue.Pattern;
import org.jfugue.Player;
import org.jfugue.StreamingPlayer;

class MidiStreamPerformer implements Performer {
    StreamingPlayer mPlayer;
    String queue;
    String instrument = "[46]";
    String tempo = "120";
    String control_params = "";

    public MidiStreamPerformer() {
        mPlayer = new StreamingPlayer();
        changeInstrument(instrument);
    }

    @Override
    public void addNote(String note, float frac) {
        int defaultExcitement = 67;
        addNote(note, frac, defaultExcitement);
    }

    @Override
    public void addNote(String note, float frac, int excitement) {
        mPlayer.stream(note + "/" + frac + "a" + excitement + " ");
        int beatsInWholeNote = 4;
        double noteLength = (60000.0f / Integer.parseInt(tempo)) * frac
                * beatsInWholeNote;
        try {
            Thread.sleep((long) noteLength);
        }
        catch (InterruptedException e) {
            // Oh well...
        }
    }

    @Override
    public void onPieceEnd() {
        mPlayer.close();
    }

    @Override
    public void onRest() {

    }

    @Override
    public void setTempo(int tempoInt) {
        tempo = String.valueOf(tempoInt);
        mPlayer.stream("T" + tempo + " ");
    }

    @Override
    public int getTempo() {
        return Integer.parseInt(tempo);
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
        mPlayer.stream("I" + instrument + " ");
    }
}