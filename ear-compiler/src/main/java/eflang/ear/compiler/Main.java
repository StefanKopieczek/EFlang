package eflang.ear.compiler;

import eflang.ear.core.EARException;

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

        String efCode;
        EARCompiler compiler = new EARCompiler();
        try {
            efCode = compiler.compile(earCode);
        } catch (EARException e) {
            throw new RuntimeException(e);
        }

        System.out.print(efCode);
    }
}
