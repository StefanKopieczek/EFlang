package eflang.ear.composer;

import eflang.ear.core.Scale;

import java.util.List;

public class OnlyRunsComposer extends AbstractScaleComposer {

    public OnlyRunsComposer(Scale scale) {
        super(scale);
    }

    @Override
    public String getStartingNode() {
        List<String> notes = scale.notes();
        return notes.get(notes.size() / 2);
    }

    @Override
    public String higherNote(String note) {
        return nextNote(note);
    }

    @Override
    public String lowerNote(String note) {
        return prevNote(note);
    }

    public String toString() {
        return String.format("OnlyRunsComposer (%s)", scale);
    }
}
