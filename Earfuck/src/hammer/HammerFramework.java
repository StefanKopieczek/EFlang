package hammer;

import java.util.concurrent.SynchronousQueue;

import earfuck.IoManager;
import earfuck.NullPerformer;
import earfuck.Parser;

/**
 * HammerFramework - The main class for interfacing with the earfuck
 * parser programmatically.
 * @author rynor_000
 *
 */
public class HammerFramework implements IoManager {
	private Object mLock = new Object();
	private Parser mParser;
	private ParserThread mThread;
	
	/**
	 * We use this SynchronousQueue for receiving output
	 * from the earfuck parser.
	 * It nicely handles blocking the threads for us so we never
	 * run into a situation where the parser sends us two loads of
	 * output before we have a chance to respond.
	 */
	private SynchronousQueue<Integer> mOutput;
	
	/**
	 * Indicates if we are currently waiting to give input to
	 * the parser.
	 */
	private boolean mWaitingToGiveInput = false;
	
	/**
	 * Indicates if the parser has requested input from us.
	 */
	private boolean mInputRequested = false;
	
	public HammerFramework() {
		mParser = new Parser(new NullPerformer());
		mParser.setIoManager(this);
		mOutput = new SynchronousQueue<Integer>();
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
		}
	}
	
	/**
	 * Blocks until a value is output by the parser.
	 * @return The output value
	 */
	public int waitandGetOutput() {
		HammerLog.log("Waiting to get output... ", 
					  HammerLog.LogLevel.DEBUG, 
					  false);
		
		int output = 0;
		
		try {
			output = mOutput.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HammerLog.debug("Got " + String.valueOf(output));
		
		return output;
	}
	
	/**
	 * Waits until the parser requests input, and then sends it.
	 * @param value - The value to send as input.
	 */
	public void waitAndSendInput(int value) {
		HammerLog.log("Waiting to send input... ", 
				  HammerLog.LogLevel.DEBUG, 
				  false);
		
		while (!mInputRequested) {
			block();
		}
		
		mInputRequested = false;
		
		mParser.giveInput(value);
		
		HammerLog.debug("Sent " + String.valueOf(value));
	} 

	@Override
	public void requestInput(Parser parser) {
		mInputRequested = true;
		synchronized (mLock) {
			mLock.notify();
		}
	}

	@Override
	public void output(int value) {
		try {
			// This call will block the parsing thread until HAMMER
			// has collected the output from the queue.
			// This prevents HAMMER getting flooded with output.
			mOutput.put(value);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private void block() {
		try {
			synchronized (mLock) {
				mLock.wait();
			}
		} catch (InterruptedException e) {
			// Do Nothing.
		}
	}

	/**
	 * The thread that drives the EF parser.
	 * We implement it by iterating over stepFoward() so that
	 * we are able to stop it at any point if necessary.
	 * @author rynor_000
	 *
	 */
	private static class ParserThread extends Thread{
		public boolean isRunning = false;
		private Parser mParser;
		
		public void setParser(Parser parser) {
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
		
		public void stopRunning() {
			isRunning = false;
		}
	}
}
