package eflang.core;

import eflang.hammer.HammerTest;
import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void testInputOutput() {
        new HammerTest("Input and Output", "e4 d4 r c4 d4 r")
                .giveInput(12)
                .expectOutput(12)
                .run();
    }

    @Test
    void testIncrement() {
        new HammerTest("Increment a cell once", "c4 d4 d4 r")
                .expectOutput(1)
                .run();
    }

    @Test
    void testIncrementThenDecrement() {
        new HammerTest("Increment, then decrement a cell", "c4 d4 d4 d4 r e4 d4 d4 c4 d4 r")
                .expectOutput(2)
                .expectOutput(1)
                .run();
    }

    @Test
    void testLoop() {
        new HammerTest("Loop to multiply input by 3", "c4 d4 c4 r ( d4 d4 d4 d4 c4 c4 ) d4 r")
                .giveInput(4)
                .expectOutput(12)
                .run();
    }

    @Test
    void testSkipLoop() {
        new HammerTest("Loop gets skipped if initial cell is 0", "c4 d4 ( d4 d4 d4 d4 ) r")
                .expectOutput(0)
                .run();
    }
}
