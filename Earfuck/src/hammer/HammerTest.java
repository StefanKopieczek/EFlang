package hammer;

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
	protected String efCode;
	
	/**
	 * The HammerFramework object to use to control the parser.
	 */
	private HammerFramework mHammer;
	
	/**
	 * A list of IO tasks to be performed in this test.
	 * These tasks will be performed in the order they appear in the list.
	 * i.e. the order they are added in.
	 */
	private ArrayList<IoTask> mTasks;
	
	/**
	 * The name of the test.
	 */
	private String mName;
	
	/**
	 * Indicates if this test failed to prepare.
	 * If true, run() will immediately return false.
	 */
	protected boolean setupFailed = false;
	
	/**
	 * A message indicating why setup failed.
	 */
	protected String failureMessage = "";
	
	public HammerTest(String name, String code, HammerFramework hammer) {
		efCode = code;
		mHammer = hammer;
		mTasks = new ArrayList<IoTask>();
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
		if (setupFailed) {
			HammerLog.error(
					"== Test: " + mName + " failed to prepare ==");
			HammerLog.error(failureMessage + "\n");
			return false;
		}
		
		HammerLog.info("== Running test: " + mName + " ==");
		mHammer.setPiece(efCode);
		mHammer.startPlaying();
		
		for (IoTask task : mTasks) {
			if (!task.execute()) {
				HammerLog.info("Test Failed!\n");
				return false;
			}
		}
		
		HammerLog.info("Test Passed!\n");
		return true;
	}
	
	/**
	 * Adds an input task to the queue.
	 * @param value - the value to input.
	 */
	public void addInputTask(int value) {
		mTasks.add(new IoTask(true, value));
	}
	
	/**
	 * Adds an output task to the queue.
	 * @param value - the value to expect.
	 */
	public void addOutputTask(int value) {
		mTasks.add(new IoTask(false, value));
	}
	
	/**
	 * A class representing a single task of either sending input to the
	 * parser, or receiving output from it.
	 * @author rynor_000
	 *
	 */
	private class IoTask {
		private boolean isInput = true;
		private int value;
		
		public IoTask(boolean input, int value) {
			isInput = input;
			this.value = value;
		}
		
		public boolean execute() {
			if (isInput) {
				mHammer.waitAndSendInput(value);
				return true;
			}
			else {
				int output = mHammer.waitandGetOutput();
				return mHammer.hammerAssert(
						"Expected: " + String.valueOf(value) +
						"  Got: " + String.valueOf(output), 
						output == value);
			}
		}
	}
}
