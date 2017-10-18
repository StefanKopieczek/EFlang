package eflang.ear.composer;

import eflang.ear.Scale;

import java.util.List;

/**
 * Composer who always chooses the furthest possible note.
 */
public class SadisticComposer extends AbstractScaleComposer {

    public SadisticComposer(Scale scale) {
        super(scale);
    }

    @Override
    public String getStartingNode() {
        List<String> notes = scale.notes();
        return notes.get(notes.size() / 2);
    }

    @Override
    public String higherNote(String note) {
        if (note.equals(scale.topNote())) {
            return scale.bottomNote();
        }
        return scale.topNote();
    }

    @Override
    public String lowerNote(String note) {
        if (note.equals(scale.bottomNote())) {
            return scale.topNote();
        }
        return scale.bottomNote();
    }
}
