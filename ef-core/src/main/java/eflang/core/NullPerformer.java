package eflang.core;

public class NullPerformer implements Performer {

    @Override
    public void onPieceStart() {
    }

    @Override
    public void onPieceEnd() {
    }

    @Override
    public void onRest() {
    }

    @Override
    public void addNote(String note, float frac) {
    }

    @Override
    public void addNote(String note, float frac, int excitement) {
    }

    @Override
    public void changeInstrument(byte instrumentCode) {
    }

    @Override
    public void setTempo(int tempoInt) {
    }

    @Override
    public int getTempo() {
        return 0;
    }
}
