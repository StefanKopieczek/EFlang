package eflang;

public interface Performer {
	public void onPieceStart();
	public void onPieceEnd();
	public void onRest();
	public void addNote(String note, float frac);
	public void addNote(String note, float frac, int excitement);
	public void changeInstrument(byte instrumentCode);
	public void setTempo(int tempoInt);
	public int getTempo();
}
