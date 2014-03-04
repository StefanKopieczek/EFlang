package hammer;

import lobecompiler.LOBECompiler;
import lobecompiler.LobeCompilationException;
import earcompiler.EARCompiler;
import earcompiler.EARException;

/**
 * A test on LOBE code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class LobeTest extends HammerTest {

	public LobeTest(String name, String code) {
		super(name, code);
		LOBECompiler lobeCompiler = new LOBECompiler();
		EARCompiler earCompiler = new EARCompiler();

		try {
			efCode = lobeCompiler.compile(efCode);
		} catch (Exception e) {
			System.out.println("Failed to compile LOBE code.");
			setupFailed = true;
		}
		
		try {
			efCode = earCompiler.compile(efCode);
		} catch (Exception e) {
			System.out.println("Failed to compile EAR code.");
			setupFailed = true;
		}
	}

}
