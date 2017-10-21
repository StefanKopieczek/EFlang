package eflang.hammer;

import eflang.core.MusicSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A base ef test class.
 * @author rynor_000
 *
 */
public class HammerTest {
    /**
     * The earfuck code to use for this test.
     */
    private Supplier<MusicSource> codeSupplier;

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

    public HammerTest(String name, Supplier<MusicSource> code, HammerFramework hammer) {
        codeSupplier = code;
        mTasks = new ArrayList<>();
        mName = name;
    }

    public HammerTest(String name, Supplier<MusicSource> code) {
        this(name, code, new HammerFramework());
    }

    public String getName() {
        return mName;
    }

    public MusicSource getCode() {
        return codeSupplier.get();
    }

    public List<TestTask> getTasks() {
        return mTasks;
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
