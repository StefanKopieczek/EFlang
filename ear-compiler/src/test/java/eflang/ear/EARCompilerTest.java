package eflang.ear;

import eflang.ear.composer.Composer;
import eflang.ear.composer.GeometricComposer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.composer.SadisticComposer;
import eflang.ear.core.Scale;
import eflang.ear.core.Scales;
import eflang.hammer.EarCodeSupplier;
import eflang.hammer.HammerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EARCompilerTest {

    static Stream<Composer> composerProvider() {
        return Stream.of(
                new OnlyRunsComposer(Scales.CMajor),
                new GeometricComposer(Scales.GMajor),
                new SadisticComposer(Scales.CMajorPentatonic),
                new GeometricComposer(new Scale("c3", "c4", "c5"))
        );
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testInputOutput(Composer composer) {
        earTest("Input and output", composer, code(
                "IN 3",
                "OUT 3"))
                .giveInput(7)
                .expectOutput(7)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testMov(Composer composer) {
        earTest("Basic MOV test", composer, code(
                "MOV 5 0",
                "OUT 0"))
                .expectOutput(5)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testAddConstant(Composer composer) {
        earTest("ADD constants", composer, code(
                "MOV 5 0",
                "ADD 2 0",
                "OUT 0",
                "ADD 0 0",
                "OUT 0"))
                .expectOutput(7)
                .expectOutput(7)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testAddCell(Composer composer) {
        earTest("Add cells", composer, code(
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

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testSubtractConstant(Composer composer) {
        earTest("SUB constants", composer, code(
                "MOV 5 0",
                "SUB 2 0",
                "OUT 0",
                "SUB 0 0",
                "OUT 0"))
                .expectOutput(3)
                .expectOutput(3)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testSubtractCells(Composer composer) {
        earTest("SUB cells", composer, code(
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

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testMultiplyConstantByCell(Composer composer) {
        earTest("MUL constants", composer, code(
                "MOV 3 1",
                "MUL 4 @1 2 3",
                "OUT 2",
                "MUL 0 @2 3 4",
                "OUT 3"))
                .expectOutput(12)
                .expectOutput(0)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testMultiplyCellByCell(Composer composer) {
        earTest("MUL cells", composer, code(
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

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testDivideConstant(Composer composer) {
        earTest("DIV constant", composer, code(
                "DIV 7 3 2 3 4 5 6 7 8",
                "OUT 2",
                "MOV 7 0",
                "DIV @0 3 2 3 4 5 6 7 8",
                "OUT 2",
                "DIV @2 5 1 3 4 5 6 7 8",
                "OUT 2"))
                .expectOutput(2)
                .expectOutput(2)
                .expectOutput(0)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testDivideCells(Composer composer) {
        earTest("DIV cells", composer, code(
                "MOV 7 0",
                "MOV 2 1",
                "DIV @0 @1 2 3 4 5 6 7 8",
                "OUT 2"))
                .expectOutput(3)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testZero(Composer composer) {
        earTest("ZERO", composer, code(
                "MOV 5 1",
                "OUT 1",
                "ZERO 1",
                "OUT 1"))
                .expectOutput(5)
                .expectOutput(0)
                .run();
    }

    @ParameterizedTest
    @MethodSource("composerProvider")
    void testComments(Composer composer) {
        earTest("Comments", composer, code(
                "// This is a test.",
                "MOV 5 1",
                "// that comments don't",
                "// break the code",
                "OUT 1",
                "// good luck!"))
                .expectOutput(5)
                .run();
    }

    private HammerTest earTest(String name, Composer composer, String code) {
        return new HammerTest(name, new EarCodeSupplier(code, composer));
    }

    private String code(CharSequence... lines) {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
