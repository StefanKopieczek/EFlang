import eflang.hammer.HammerLoader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class HammerTest {

    @TestFactory
    Stream<DynamicTest> hammerTests() {
        return HammerLoader.loadTestsFromDirectory(new File("tests/ear")).stream()
                .map(hammerTest -> dynamicTest(hammerTest.getName(), hammerTest::run));
    }

}
