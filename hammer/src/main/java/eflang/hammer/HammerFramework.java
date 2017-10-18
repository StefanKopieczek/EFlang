package eflang.hammer;

import eflang.core.IoManager;
import eflang.core.NullPerformer;
import eflang.core.Parser;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * HammerFramework - The main class for interfacing with the earfuck
 * parser programmatically.
 * @author rynor_000
 *
 */
public class HammerFramework implements IoManager {
    private Parser mParser;
    private ParserThread mThread;
    private static final int DEFAULT_TIMEOUT = 5;

    /**
     * We use this SynchronousQueue for receiving output
     * from the earfuck parser.
     * It nicely handles blocking the threads for us so we never
     * run into a situation where the parser sends us two loads of
     * output before we have a chance to respond.
     */
    private SynchronousQueue<Integer> mOutput;

    /**
     * Also for receiving input.
     * This is not technically necessary here, since the
     * parser will wait for us to return input anyway, but
     * it makes our code cleaner.
     */
    private SynchronousQueue<Integer> mInput;

    public HammerFramework() {
        mParser = new Parser(new NullPerformer());
        mParser.setIoManager(this);
        mOutput = new SynchronousQueue<Integer>();
        mInput = new SynchronousQueue<Integer>();
    }

    public void resetParser() {
        HammerLog.debug("Resetting Parser...");
        mParser.refreshState();
    }

    /**
     * Set the piece we'll be playing.
     * @param efCode - A valid ef piece as a String.
     */
    public void setPiece(String efCode) {
        mParser.giveMusic(efCode);
    }

    /**
     * Kick off the parser. It runs in a separate thread.
     */
    public void startPlaying() {
        HammerLog.debug("Starting piece...");
        mThread = new ParserThread();
        mThread.setParser(mParser);
        mThread.start();
    }

    /**
     * Make sure all child threads are stopped before exiting.
     */
    public void tearDown() {
        if (mThread != null && mThread.isRunning) {
            mThread.stopRunning();
            //Wake the thread up if it was blocking for input.
            mThread.interrupt();
            mThread = null;
        }
    }

    /**
     * Blocks until a value is output by the parser.
     * @param timeoutInSeconds - length of time to wait before failing
     * @return The output value
     */
    public int waitAndGetOutput(long timeoutInSeconds) throws IOException {
        HammerLog.log("Waiting to get output... ",
                      HammerLog.LogLevel.DEBUG,
                      false);

        Integer output = null;

        try {
            output = mOutput.poll(timeoutInSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            // Do nothing
        }

        if (output == null) {
            throw new IOException("Timed out waiting for output.");
        }
        else {
            HammerLog.debug("Got " + String.valueOf(output));
        }

        return output;
    }

    /**
     * Blocks until a value is output by the parser.
     * @return The output value
     */
    public int waitAndGetOutput() throws IOException {
        return waitAndGetOutput(DEFAULT_TIMEOUT);
    }

    /**
     * Waits until the parser requests input, and then sends it.
     * @param value - The value to send as input.
     * @param timeoutInSeconds - the length of time to wait before failing
     * @throws IOException
     */
    public void waitAndSendInput(int value, int timeoutInSeconds) throws IOException {
        HammerLog.log("Waiting to send input... ",
                      HammerLog.LogLevel.DEBUG,
                      false);
        boolean success = false;

        try {
            success = mInput.offer(value,
                                   timeoutInSeconds,
                                   TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            // Do Nothing
        }

        if (!success) {
            throw new IOException("Timed out waiting to send input.");
        }

        HammerLog.debug("Sent " + String.valueOf(value));
    }

    /**
     * Waits until the parser requests input, and then sends it.
     * @param value - The value to send as input.
     * @throws IOException
     */
    public void waitAndSendInput(int value) throws IOException {
        waitAndSendInput(value, DEFAULT_TIMEOUT);
    }

    @Override
    public void requestInput(Parser parser) {
        int input = 0;
        try {
            // This call will block the parsing thread until HAMMER
            // provides input.
            // This is semi-unnecessary since the EF parser will wait
            // for input anyway, however this way gives a nice symmetry
            // to our input and output.
            input = mInput.take();
        }
        catch (InterruptedException e) {
            // Do nothing
        }

        mParser.giveInput(input);
    }

    @Override
    public void output(int value) {
        try {
            // This call will block the parsing thread until HAMMER
            // has collected the output from the queue.
            // This prevents HAMMER getting flooded with output.
            mOutput.put(value);
        }
        catch (InterruptedException e) {
            // Do nothing
        }
    }

    /**
     * Checks a statement, and prints a warning message and stacktrace
     * if it is false.
     * @param descriptor - A description of what was asserted.
     * @param condition - A boolean condition which should be true.
     * @return Whether the assertion was true/false.
     */
    public boolean hammerAssert(String descriptor, boolean condition) {
        if (!condition) {
            HammerLog.error("Assertion Failed: " + descriptor);
            printStackTrace();
        }
        return condition;
    }

    private void printStackTrace() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();

        HammerLog.error("Call Stack:");
        for (StackTraceElement ele : stack) {
            HammerLog.error(ele.toString());
        }
        HammerLog.error("");
    }

    /**
     * The thread that drives the EF parser.
     * We implement it by iterating over stepFoward() so that
     * we are able to stop it at any point if necessary.
     * @author rynor_000
     *
     */
    private static class ParserThread extends Thread{
        boolean isRunning = false;
        private Parser mParser;

        void setParser(Parser parser) {
            mParser = parser;
        }

        @Override
        public void run() {
            isRunning = true;
            while (isRunning) {
                if (mParser.getPlace() < mParser.getPiece().length) {
                    mParser.stepForward();
                }
                else {
                    isRunning = false;
                }
            }
        }

        void stopRunning() {
            isRunning = false;
        }
    }
}
