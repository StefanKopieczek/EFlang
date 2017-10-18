package eflang.ear;

import java.util.Arrays;
import java.util.List;

public class Scale {
    private List<String> notes;

    Scale(String... notes) {
        this.notes = Arrays.asList(notes);
    }

    String nextNote(String note) {
        int index = notes.indexOf(note) + 1;
        if (index == notes.size()) {
            return notes.get(0);
        } else {
            return notes.get(index);
        }
    }

    String prevNote(String note) {
        int index = notes.indexOf(note) - 1;
        if (index == -1) {
            return notes.get(notes.size() - 1);
        } else {
            return notes.get(index);
        }
    }

    String topNote() {
        return notes.get(notes.size() - 1);
    }

    String bottomNote() {
        return notes.get(0);
    }

    int compareNotes(String left, String right) {
        return Integer.compare(notes.indexOf(left), notes.indexOf(right));
    }

    int size() {
        return notes.size();
    }

    String getNoteAt(int index) {
        return notes.get(index);
    }
}
