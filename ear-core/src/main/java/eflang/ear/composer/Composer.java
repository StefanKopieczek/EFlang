package eflang.ear.composer;

public interface Composer {
    String getStartingNode();
    String nextNote(String note);
    String prevNote(String note);
    String higherNote(String note);
    String lowerNote(String note);
    String bottomNote();
    String topNote();
    int compareNotes(String left, String right);
}
