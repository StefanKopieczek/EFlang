package eflang.hammer;

import eflang.core.MusicSource;
import eflang.core.StringMusicSource;
import eflang.ear.compiler.EARCompiler;
import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.core.Scales;

/**
 * A test on EAR code.
 * Simply compiles to ef code, and runs as a usual HammerTest.
 * @author rynor_000
 *
 */
public class EarCodeConverter implements CodeConverter {
    private Composer composer;

    public EarCodeConverter() {
        this(new OnlyRunsComposer(Scales.CMajor));
    }

    public EarCodeConverter(Composer composer) {
        this.composer = composer;
    }

    public MusicSource apply(String code) {
        EARCompiler compiler = new EARCompiler(composer);
        try {
            return new StringMusicSource(compiler.compile(code));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile EAR code with error:\n" + e.getMessage());
        }
    }
}
