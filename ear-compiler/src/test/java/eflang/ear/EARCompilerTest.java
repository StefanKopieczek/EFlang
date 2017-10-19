package eflang.ear;

import eflang.hammer.EarTest;
import org.junit.jupiter.api.Test;

class EARCompilerTest {

    @Test
    void testMov() {
        new EarTest("Basic MOV test", code(
                "MOV 5 0",
                "OUT @0"))
                .expectOutput(4)
                .run();
    }

    private String code(CharSequence... lines) {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
