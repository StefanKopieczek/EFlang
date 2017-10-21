package eflang.ear.compiler;

import com.google.common.collect.ImmutableList;
import eflang.ear.composer.Composer;
import eflang.ear.composer.GeometricComposer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.composer.SadisticComposer;
import eflang.ear.core.Scale;
import eflang.ear.core.Scales;
import eflang.hammer.*;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class EarCompilerHammerTest {

    @TestFactory
    Stream<DynamicNode> hammerTests() {
         HammerLoader loader = new HammerLoader();
        List<Composer> composers = ImmutableList.of(
                new OnlyRunsComposer(Scales.CMajor),
                new GeometricComposer(Scales.BluesMinor),
                new SadisticComposer(Scales.CMajorPentatonic),
                new GeometricComposer(new Scale("3 Notes", ImmutableList.of("c4", "e4", "g4")))
        );

        List<HammerTest> tests = loader.loadTestsFromDirectory(new File("../tests/ear"));
        return composers.stream()
                .map(composer -> dynamicContainer(
                        composer.toString(),
                        buildTests(composer, tests)
                ));
    }

    private Stream<DynamicTest> buildTests(Composer composer, List<HammerTest> tests) {
        HammerRunner runner = runnerForComposer(composer);
        return tests.stream().map(test -> dynamicTest(test.getName(), () -> runner.run(test)));
    }

    private HammerRunner runnerForComposer(Composer composer) {
        return new HammerRunner().withCodeConverter(TestType.EAR, new EarCodeConverter(composer));
    }
}
