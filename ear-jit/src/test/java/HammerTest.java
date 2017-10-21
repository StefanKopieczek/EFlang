import eflang.ear.composer.Composer;
import eflang.ear.composer.GeometricComposer;
import eflang.ear.core.Scales;
import eflang.hammer.HammerLoader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class HammerTest {

    @TestFactory
    Stream<DynamicTest> hammerTests() {
        HammerLoader loader = new HammerLoader();
        Composer composer = new GeometricComposer(Scales.BluesMinor);
        loader.setEarCodeSupplierFactory(code -> new EarJitCodeSupplier(composer, code));
        return loader.loadTestsFromDirectory(new File("tests/ear")).stream()
                .map(hammerTest -> dynamicTest(hammerTest.getName(), hammerTest::run));
    }

}
