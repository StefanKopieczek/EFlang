package eflang.hammer;

import java.util.ArrayList;

/**
 * A base ef test class.
 * @author rynor_000
 *
 */
public class HammerTest {
    /**
     * The earfuck code to use for this test.
     */
    String efCode;

    /**
     * The HammerFramework object to use to control the parser.
     */
    private HammerFramework mHammer;

    /**
     * A list of IO tasks to be performed in this test.
     * These tasks will be performed in the order they appear in the list.
     * i.e. the order they are added in.
     */
    private ArrayList<TestTask> mTasks;

    /**
     * The name of the test.
     */
    private String mName;

    /**
     * Indicates if this test failed to prepare.
     * If true, run() will immediately return false.
     */
    boolean setupFailed = false;

    /**
     * A message indicating why setup failed.
     */
    String failureMessage = "";

    public HammerTest(String name, String code, HammerFramework hammer) {
        efCode = code;
        mHammer = hammer;
        mTasks = new ArrayList<>();
        mName = name;
    }

    public HammerTest(String name, String code) {
        this(name, code, new HammerFramework());
    }

    public String getName() {
        return mName;
    }

    /**
     * Runs the test.
     * @return Whether the test passed or not (true/false)
     */
    public boolean run() {
        boolean testPassed = true;

        if (setupFailed) {
            // We failed to set up the test, so return failure.
            HammerLog.error(
                    "== Test: " + mName + " failed to prepare ==");
            HammerLog.error(failureMessage + "\n");
            return false;
        }

        HammerLog.info("== Running test: " + mName + " ==");

        // Set the piece playing.
        mHammer.setPiece(efCode);
        mHammer.startPlaying();

        // For each IO task we have, execute it (either give input,
        // or check output).
        // If it fails (i.e. the output doesn't match expected) return
        // failure.
        for (TestTask task : mTasks) {
            try {
                task.execute(mHammer);
            } catch (Exception e) {
                HammerLog.info("Test Failed!");
                throw new HammerException("Test failed", e);
            }
        }

        mHammer.tearDown();

        // If we got this far, the test must have passed
        if (testPassed) {
            HammerLog.info("Test Passed!");
        }
        HammerLog.debug("");
        return testPassed;
    }

    public HammerTest giveInput(int input) {
        mTasks.add(new InputTask(input));
        return this;
    }

    public HammerTest expectOutput(int output) {
        mTasks.add(new OutputTask(output));
        return this;
    }

    public HammerTest reset() {
        mTasks.add(new RestartTask());
        return this;
    }

    /**
     * Adds a task to the queue.
     * @param task - the task to add.
     */
    public void addTask(TestTask task) {
        mTasks.add(task);
    }

    /**
     * A class representing a single task for a test to execute.
     * @author rynor_000
     *
     */
    public interface TestTask {
        void execute(HammerFramework hammer) throws Exception;
    }

    /**
     * Wait for output from the parser.
     * Compare the output with the expected value.
     * Fail if they don't match.
     * @author rynor_000
     *
     */
    public static class OutputTask implements TestTask {
        private int expected;

        OutputTask(int expected) {
            this.expected = expected;
        }

        public void execute(HammerFramework hammer) throws Exception {
            int output = hammer.waitAndGetOutput();
            hammer.hammerAssert(
                    "Expected: " + String.valueOf(expected) +
                    "  Got: " + String.valueOf(output),
                    output == expected);
        }
    }

    /**
     * Send some input to the parser.
     * @author rynor_000
     *
     */
    public static class InputTask implements TestTask {
        private int value;

        InputTask(int value) {
            this.value = value;
        }

        public void execute(HammerFramework hammer) throws Exception{
            hammer.waitAndSendInput(value);
        }
    }

    /**
     * Restart the piece from the start.
     * @author rynor_000
     *
     */
    public static class RestartTask implements TestTask {

        RestartTask() {
        }

        public void execute(HammerFramework hammer) {
            hammer.tearDown();
            hammer.resetParser();
            hammer.startPlaying();
        }
    }
}
