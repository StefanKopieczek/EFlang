package eflang.ear;

import eflang.hammer.EarCodeSupplier;
import eflang.hammer.HammerTest;
import org.junit.jupiter.api.Test;

class EARCompilerTest {

    @Test
    void testMov() {
        earTest("Basic MOV test", code(
                "MOV 5 0",
                "OUT 0"))
                .expectOutput(5)
                .run();
    }

    private HammerTest earTest(String name, String code) {
        return new HammerTest(name, new EarCodeSupplier(code));
    }

    private String code(CharSequence... lines) {
        return String.join(System.getProperty("line.separator"), lines);
    }
}
