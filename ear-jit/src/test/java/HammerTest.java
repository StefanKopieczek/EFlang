import eflang.hammer.HammerLoader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class HammerTest {
    private static String testDirectory = "tests/ear";

    @TestFactory
    Stream<DynamicTest> hammerTests() {
        File testDir = new File(testDirectory);
        if (!testDir.isDirectory()) {
            throw new RuntimeException("Not a directory");
        }
        File[] testFiles = testDir.listFiles((File dir, String name) -> name.endsWith(".test"));

        if (testFiles == null) {
            throw new RuntimeException("Not a directory, or IO Exception");
        }

        return Arrays.stream(testFiles)
                .map(HammerTest::safeLoadTest)
                .map(hammerTest -> dynamicTest(hammerTest.getName(), () -> hammerTest.run()));
    }

    private static eflang.hammer.HammerTest safeLoadTest(File file) {
        try {
            return HammerLoader.loadTest(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
