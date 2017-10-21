package eflang.core;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.print("Loading new earfuck parser...");
        Parser efParser = new Parser(new NullPerformer());
        System.out.println("Done!");

        System.out.print("Handing music to performer...");
        efParser.giveMusic(new FileMusicSource(file));
        System.out.println("Done!");

        System.out.println("Performing...");
        efParser.perform();
        System.out.println("Piece complete.");
    }
}
