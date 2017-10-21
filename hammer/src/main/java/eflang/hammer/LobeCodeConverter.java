package eflang.hammer;

import eflang.core.MusicSource;
import eflang.lobe.LOBECompiler;

/**
 * A test on LOBE code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class LobeCodeConverter implements CodeConverter {
    public MusicSource apply(String code) {
        LOBECompiler lobeCompiler = new LOBECompiler();

        try {
            String earCode = lobeCompiler.compile(code);
            return new EarCodeConverter().apply(earCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile LOBE code. \n" + code, e);
        }
    }

}
