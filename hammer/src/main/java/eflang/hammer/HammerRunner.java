package eflang.hammer;

import eflang.core.MusicSource;

import java.util.HashMap;
import java.util.Map;

public class HammerRunner {

    private Map<TestType, CodeConverter> codeConverters = new HashMap<>();

    public HammerRunner() {
        codeConverters.put(TestType.EF, new EfCodeConverter());
        codeConverters.put(TestType.EAR, new EarCodeConverter());
        codeConverters.put(TestType.LOBE, new LobeCodeConverter());
    }

    public HammerRunner withCodeConverter(TestType type, CodeConverter codeConverter) {
        codeConverters.put(type, codeConverter);
        return this;
    }

    public void run(HammerTest test) {
        HammerLog.info("== Running test: " + test.getName() + " ==");

        HammerFramework hammer = new HammerFramework();

        // Set the piece playing.
        MusicSource source = codeConverters.get(test.getType()).apply(test.getCode());
        hammer.setPiece(source);
        hammer.startPlaying();

        // For each IO task we have, execute it (either give input,
        // or check output).
        // If it fails (i.e. the output doesn't match expected) return
        // failure.
        test.getTasks().forEach(task -> {
            try {
                task.execute(hammer);
            } catch (Exception e) {
                HammerLog.info("Test Failed!");
                throw new HammerException("Test failed", e);
            }
        });

        hammer.tearDown();

        // If we got this far, the test must have passed
        HammerLog.info("Test Passed!");
    }
}
