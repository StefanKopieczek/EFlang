package eflang.ear;

import eflang.hammer.EarCodeSupplier;
import eflang.hammer.HammerTest;
import org.junit.jupiter.api.Test;

class EARCompilerTest {

    @Test
    void testInputOutput() {
        earTest("Input and output", code(
                "IN 3",
                "OUT 3"))
                .giveInput(7)
                .expectOutput(7)
                .run();
    }

    @Test
    void testMov() {
        earTest("Basic MOV test", code(
                "MOV 5 0",
                "OUT 0"))
                .expectOutput(5)
                .run();
    }

    @Test
    void testAddConstant() {
        earTest("ADD constants", code(
                "MOV 5 0",
                "ADD 2 0",
                "OUT 0",
                "ADD 0 0",
                "OUT 0"))
                .expectOutput(7)
                .expectOutput(7)
                .run();
    }

    @Test
    void testAddCell() {
        earTest("Add cells", code(
                "MOV 5 0",
                "MOV 2 1",
                "ADD @0 1",
                "OUT 1",
                "ADD @2 1",
                "OUT 1"))
                .expectOutput(7)
                .expectOutput(7)
                .run();
    }

    @Test
    void testSubtractConstant() {
        earTest("SUB constants", code(
                "MOV 5 0",
                "SUB 2 0",
                "OUT 0",
                "SUB 0 0",
                "OUT 0"))
                .expectOutput(3)
                .expectOutput(3)
                .run();
    }

    @Test
    void testSubtractCells() {
        earTest("SUB cells", code(
                "MOV 5 1",
                "MOV 2 0",
                "SUB @0 1",
                "OUT 1",
                "SUB @2 1",
                "OUT 1"))
                .expectOutput(3)
                .expectOutput(3)
                .run();
    }

    @Test
    void testMultiplyConstantByCell() {
        earTest("MUL constants", code(
                "MOV 3 1",
                "MUL 4 @1 2 3",
                "OUT 2",
                "MUL 0 @2 3 4",
                "OUT 3"))
                .expectOutput(12)
                .expectOutput(0)
                .run();
    }

    @Test
    void testMultiplyCellByCell() {
        earTest("MUL cells", code(
                "MOV 3 1",
                "MOV 4 0",
                "MUL @0 @1 2 3",
                "OUT 2",
                "MUL @4 @2 5 6",
                "OUT 5"))
                .expectOutput(12)
                .expectOutput(0)
                .run();
    }

    @Test
    void testDivideConstant() {
        earTest("DIV constant", code(
                "MOV 7 1",
                "DIV @1 2 2 3 4 5 6 7 8",
                "OUT 2",
                "DIV @2 5 1 3 4 5 6 7 8",
                "OUT 2"))
                .expectOutput(3)
                .expectOutput(0)
                .run();
    }

    @Test
    void testDivideCells() {
        earTest("DIV cells", code(
                "MOV 7 0",
                "MOV 2 1",
                "DIV @0 @1 2 3 4 5 6 7 8",
                "OUT 2"))
                .expectOutput(3)
                .run();
    }

    @Test
    void testZero() {
        earTest("ZERO", code(
                "MOV 5 1",
                "OUT 1",
                "ZERO 1",
                "OUT 1"))
                .expectOutput(5)
                .expectOutput(0)
                .run();
    }

    private HammerTest earTest(String name, String code) {
        return new HammerTest(name, new EarCodeSupplier(code));
    }

    private String code(CharSequence... lines) {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
