package eflang.hammer;

import eflang.core.MusicSource;
import eflang.core.StringMusicSource;
import eflang.ear.compiler.EARCompiler;
import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.core.Scales;

import java.util.function.Supplier;

/**
 * A test on EAR code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class EarCodeSupplier implements Supplier<MusicSource> {
    private String earCode;
    private Composer composer;

    public EarCodeSupplier(String code) {
        this(code, new OnlyRunsComposer(Scales.CMajor));
    }

    public EarCodeSupplier(String code, Composer composer) {
        this.earCode = code;
        this.composer = composer;
    }

    public MusicSource get() {
        EARCompiler compiler = new EARCompiler(composer);
        try {
            return new StringMusicSource(compiler.compile(earCode));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile EAR code with error:\n" + e.getMessage());
        }
    }
}
