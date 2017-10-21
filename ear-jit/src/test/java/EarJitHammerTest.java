import eflang.ear.composer.Composer;
import eflang.ear.composer.GeometricComposer;
import eflang.ear.core.Scales;
import eflang.hammer.HammerLoader;
import eflang.hammer.HammerRunner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class EarJitHammerTest {

    @TestFactory
    Stream<DynamicTest> hammerTests() {
        HammerLoader loader = new HammerLoader();
        HammerRunner runner = new HammerRunner();
        Composer composer = new GeometricComposer(Scales.BluesMinor);
        loader.setEarCodeSupplierFactory(code -> new EarJitCodeSupplier(composer, code));
        return loader.loadTestsFromDirectory(new File("../tests/ear")).stream()
                .map(hammerTest -> dynamicTest(hammerTest.getName(), () -> runner.run(hammerTest)));
    }

}
