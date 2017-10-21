package eflang.ear.core;

import java.util.ArrayList;
import java.util.List;

public class Scale {
    private String name;
    private List<String> notes;

    public Scale(List<String> notes) {
        this("unnamed", notes);
    }

    public Scale(String name, List<String> notes) {
        this.name = name;
        this.notes = notes;
    }

    public String nextNote(String note) {
        int index = notes.indexOf(note) + 1;
        if (index == notes.size()) {
            throw new RuntimeException("This is the highest note");
        } else {
            return notes.get(index);
        }
    }

    public String prevNote(String note) {
        int index = notes.indexOf(note) - 1;
        if (index == -1) {
            throw new RuntimeException("This is the lowest note");
        } else {
            return notes.get(index);
        }
    }

    public String topNote() {
        return notes.get(notes.size() - 1);
    }

    public String bottomNote() {
        return notes.get(0);
    }

    public int compareNotes(String left, String right) {
        return Integer.compare(notes.indexOf(left), notes.indexOf(right));
    }

    public List<String> notes() {
        List<String> copyNotes = new ArrayList<>();
        copyNotes.addAll(notes);
        return copyNotes;
    }

    public String toString() {
        return name;
    }
}
