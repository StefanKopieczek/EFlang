package eflang.core;

public interface Performer {
    void onPieceStart();
    void onPieceEnd();
    void onRest();
    void addNote(String note, float frac);
    void addNote(String note, float frac, int excitement);
    void changeInstrument(byte instrumentCode);
    void setTempo(int tempoInt);
    int getTempo();
}
