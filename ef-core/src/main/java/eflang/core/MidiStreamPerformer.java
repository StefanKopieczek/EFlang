package eflang.core;

import org.jfugue.StreamingPlayer;

public class MidiStreamPerformer implements Performer {
    private StreamingPlayer mPlayer;
    private String queue;
    private byte instrument;
    private String tempo = "120";

    public MidiStreamPerformer(byte instrument) {
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

    @Override
    public void onPieceStart() {

    }

    @Override
    public void changeInstrument(byte instrumentCode) {
        instrument = instrumentCode;
        mPlayer.stream("I" + String.valueOf(instrument) + " ");
    }
}