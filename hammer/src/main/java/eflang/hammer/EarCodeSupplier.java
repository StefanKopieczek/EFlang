package eflang.hammer;

import eflang.ear.EARCompiler;

import java.util.function.Supplier;

/**
 * A test on EAR code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class EarCodeSupplier implements Supplier<String> {
    private String earCode;

    public EarCodeSupplier(String code) {
        this.earCode = code;
    }

    public String get() {
        EARCompiler compiler = new EARCompiler();
        try {
            return compiler.compile(earCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile EAR code with error:\n" + e.getMessage());
        }
    }
}
