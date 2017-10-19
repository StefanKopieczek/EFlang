package eflang.hammer;

import eflang.ear.EARCompiler;
import eflang.lobe.LOBECompiler;

import java.util.function.Supplier;

/**
 * A test on LOBE code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class LobeCodeSupplier implements Supplier<String> {
    private String lobeCode;

    public LobeCodeSupplier(String code) {
        this.lobeCode = code;
    }

    public String get() {
        LOBECompiler lobeCompiler = new LOBECompiler();

        try {
            String earCode = lobeCompiler.compile(lobeCode);
            return new EarCodeSupplier(earCode).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile LOBE code. \n" + lobeCode, e);
        }
    }

}
