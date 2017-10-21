package eflang.ear.jit;

import eflang.core.MidiStreamPerformer;
import eflang.core.Performer;
import eflang.ear.composer.Composer;
import eflang.ear.composer.GeometricComposer;
import eflang.ear.core.Scales;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        String earCode;

        try {
            earCode = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Composer composer = new GeometricComposer(Scales.CMajorPentatonic);

        Performer performer = new MidiStreamPerformer((byte)0);
        performer.setTempo(200);

        EARJit jit = new EARJit(composer, performer);
        jit.run(earCode);
    }
}
