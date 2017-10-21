package eflang.ear.composer;

import eflang.ear.core.Scale;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeometricComposer extends AbstractScaleComposer {
    private static double DEFAULT_P = 0.4;

    private double p;
    private Random random;

    public GeometricComposer(Scale scale) {
        this(scale, DEFAULT_P);
    }

    public GeometricComposer(Scale scale, double p) {
        super(scale);
        this.p = p;
        this.random = new Random(1);
    }

    @Override
    public String getStartingNode() {
        List<String> notes = scale.notes();
        return notes.get(notes.size() / 2);
    }

    @Override
    public String higherNote(String note) {
        List<String> notes = scale.notes();

        int index = notes.indexOf(note);
        List<String> higherNotes = notes.subList(index + 1, notes.size());

        if (higherNotes.isEmpty()) {
            throw new RuntimeException("Already at the highest note");
        }

        return selectNote(higherNotes);
    }

    @Override
    public String lowerNote(String note) {
        List<String> notes = scale.notes();

        int index = notes.indexOf(note);
        List<String> lowerNotes = notes.subList(0, index);

        if (lowerNotes.isEmpty()) {
            throw new RuntimeException("Already at the lowest note");
        }

        // Reverse order so we select nearest notes with higher probability.
        Collections.reverse(lowerNotes);

        return selectNote(lowerNotes);
    }

    private String selectNote(List<String> notes) {
        // This isn't really a geometric distribution I think, but it's similar.
        for (String note: notes) {
            if (random.nextDouble() < p) {
                return note;
            }
        }

        // Didn't select any.  Default to a basic step up.
        return notes.get(0);
    }
}
