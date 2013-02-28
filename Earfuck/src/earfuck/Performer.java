package earfuck;

public interface Performer {
	public void onPieceStart();
	public void onPieceEnd();
	public void onRest();
	public void addNote(String note, float frac);
	public void addNote(String note, float frac, int excitement);
	public void changeInstrument(String instrumentString);
	public void setTempo(int tempoInt);
	public int getTempo();
}
