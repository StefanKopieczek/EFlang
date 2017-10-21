package eflang.ear;

import eflang.hammer.HammerLoader;
import eflang.hammer.HammerRunner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class EarCompilerHammerTest {

    @TestFactory
    Stream<DynamicTest> hammerTests() {
        HammerLoader loader = new HammerLoader();
        HammerRunner runner = new HammerRunner();
        return loader.loadTestsFromDirectory(new File("../tests/ear")).stream()
                .map(hammerTest -> dynamicTest(hammerTest.getName(), () -> runner.run(hammerTest)));
    }

}
