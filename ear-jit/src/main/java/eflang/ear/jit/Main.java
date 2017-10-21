package eflang.ear.jit;

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

        EARJit jit = new EARJit(new GeometricComposer(Scales.BluesMinor));
        jit.run(earCode);
    }
}
