package eflang.ear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scale {
    private List<String> notes;

    Scale(String... notes) {
        this.notes = Arrays.asList(notes);
    }

    public String nextNote(String note) {
        int index = notes.indexOf(note) + 1;
        if (index == notes.size()) {
            return notes.get(0);
        } else {
            return notes.get(index);
        }
    }

    public String prevNote(String note) {
        int index = notes.indexOf(note) - 1;
        if (index == -1) {
            return notes.get(notes.size() - 1);
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
}
