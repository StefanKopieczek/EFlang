package eflang.hammer;

import eflang.ear.EARCompiler;

/**
 * A test on EAR code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class EarTest extends HammerTest {

	public EarTest(String name, String code) {
		super(name, code);
		EARCompiler compiler = new EARCompiler();
		try {
			efCode = compiler.compile(efCode);
		} catch (Exception e) {
			failureMessage = "Failed to compile EAR code with error:\n" + e.getMessage();
			setupFailed = true;
		}
	}
}
