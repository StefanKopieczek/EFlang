package eflang.hammer;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of HAMMER tests to be run in one go.
 * @author rynor_000
 *
 */
public class HammerSuite {
    private String mName;
    private List<HammerTest> mTests;

    public HammerSuite(String name) {
        mName = name;
        mTests = new ArrayList<>();
    }

    /**
     * Adds a test to the test suite.
     * @param test
     */
    public void addTest(HammerTest test) {
        mTests.add(test);
    }

    /**
     * Runs all the tests in the suite in the order they were added.
     */
    public void run() {
        ArrayList<HammerTest> failedTests = new ArrayList<>();
        HammerRunner runner = new HammerRunner();

        HammerLog.info("  === Running HAMMER suite: " + mName + " ===");

        // Silence test output.
        // In future this should only silence output to stdout.
        // The log file should still be written.
        HammerLog.LogLevel prevLevel = HammerLog.getPrintLevel();
        HammerLog.setPrintLevel(HammerLog.LogLevel.NONE);

        mTests.forEach(test -> {
            try {
                runner.run(test);
                System.out.print('.');
            } catch (Exception e) {
                System.out.print('F');
                failedTests.add(test);
                HammerLog.exception(e);
            }
        });

        int numTests = mTests.size();
        int failures = failedTests.size();

        HammerLog.info("");

        // Reset the log level to what it was before.
        HammerLog.setPrintLevel(prevLevel);

        HammerLog.info("\nTest suite complete: out of " +
                        String.valueOf(numTests) + " tests, " +
                        String.valueOf(failures) + " failed.");

        if (failures > 0) {
            HammerLog.info("Failed tests: ");
            for (HammerTest test : failedTests) {
                HammerLog.info("    " + test.getName());
            }
        }
        HammerLog.info("");
    }
}
