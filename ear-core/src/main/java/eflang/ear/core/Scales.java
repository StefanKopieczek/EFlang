package eflang.ear.core;

import com.google.common.collect.ImmutableList;

public class Scales {

    public static Scale CMajor = new Scale("C Major", ImmutableList.of(
            "c3", "d3", "e3", "f3", "g3", "a3", "b3",
            "c4", "d4", "e4", "f4", "g4", "a4", "b4",
            "c5", "d5", "e5", "f5", "g5", "a5", "b5"
    ));

    public static Scale CMinor = new Scale("C Minor", ImmutableList.of(
            "c3", "d3", "eb3", "f3", "g3", "ab3", "bb3",
            "c4", "d4", "eb4", "f4", "g4", "ab4", "bb4",
            "c5", "d5", "eb5", "f5", "g5", "ab5", "bb5"
    ));

    public static Scale CMajorPentatonic = new Scale("C Major Pentatonic", ImmutableList.of(
            "c3", "d3", "e3", "g3", "a3",
            "c4", "d4", "e4", "g4", "a4",
            "c5", "d5", "e5", "g5", "a5"
    ));

    public static Scale FMajor = new Scale("F Major", ImmutableList.of(
            "f3", "g3", "a3", "bb3", "c4", "d4", "e4",
            "f4", "g4", "a4", "bb4", "c5", "d5", "e5",
            "f5", "g5", "a5", "bb5", "c6", "d6", "e6"
    ));

    public static Scale GMajor = new Scale("G Major", ImmutableList.of(
            "g3", "a3", "b3", "c4", "d4", "e4", "f#4",
            "g4", "a4", "b4", "c5", "d5", "e5", "f#5",
            "g5", "a5", "b5", "c6", "d6", "e6", "f#6"
    ));

    public static Scale BluesMajor = new Scale("Major Blues", ImmutableList.of(
            "c3", "d3", "eb3", "e3", "g3", "a3",
            "c4", "d4", "eb4", "e4", "g4", "a4",
            "c5", "d5", "eb5", "e5", "g5", "a5"
    ));

    public static Scale BluesMinor = new Scale("Minor Blues", ImmutableList.of(
            "c3", "eb3", "f3", "f#3", "g3", "bb3",
            "c4", "eb4", "f4", "f#4", "g4", "bb4",
            "c5", "eb5", "f5", "f#5", "g5", "bb5"
    ));

    // Prevent instantiation.
    private Scales() {
    }
}
