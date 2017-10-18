package eflang.ear.composer;

import eflang.ear.Scale;

public class OnlyRunsComposer extends AbstractScaleComposer {

    public OnlyRunsComposer(Scale scale) {
        super(scale);
    }

    @Override
    public String getStartingNode() {
        return scale.getNoteAt(scale.size() / 2);
    }

    @Override
    public String higherNote(String note) {
        return nextNote(note);
    }

    @Override
    public String lowerNote(String note) {
        return prevNote(note);
    }

}
