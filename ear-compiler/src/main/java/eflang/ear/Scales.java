package eflang.ear;

public class Scales {

    public static Scale CMajor = new Scale(
            "c3", "d3", "e3", "f3", "g3", "a3", "b3",
            "c4", "d4", "e4", "f4", "g4", "a4", "b4",
            "c5", "d5", "e5", "f5", "g5", "a5", "b5"
    );

    public static Scale CMinor = new Scale(
            "c3", "d3", "eb3", "f3", "g3", "ab3", "bb3",
            "c4", "d4", "eb4", "f4", "g4", "ab4", "bb4",
            "c5", "d5", "eb5", "f5", "g5", "ab5", "bb5"
    );

    public static Scale CMajorPentatonic = new Scale(
            "c3", "d3", "e3", "g3", "a3",
            "c4", "d4", "e4", "g4", "a4",
            "c5", "d5", "e5", "g5", "a5"
    );

    // Prevent instantiation.
    private Scales() {
    }
}
