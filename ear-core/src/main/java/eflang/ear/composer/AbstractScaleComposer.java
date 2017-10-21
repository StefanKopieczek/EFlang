package eflang.ear.composer;

import eflang.ear.core.Scale;

public abstract class AbstractScaleComposer implements Composer {
    protected Scale scale;

    public AbstractScaleComposer(Scale scale) {
        this.scale = scale;
    }

    @Override
    public String nextNote(String note) {
        return scale.nextNote(note);
    }

    @Override
    public String prevNote(String note) {
        return scale.prevNote(note);
    }

    @Override
    public String bottomNote() {
        return scale.bottomNote();
    }

    @Override
    public String topNote() {
        return scale.topNote();
    }

    @Override
    public int compareNotes(String left, String right) {
        return scale.compareNotes(left, right);
    }
}
