import eflang.core.MusicSource;
import eflang.ear.composer.Composer;
import eflang.ear.core.Command;
import eflang.ear.core.Instruction;
import eflang.ear.core.InstructionParser;
import eflang.ear.core.StatefulInstructionCompiler;
import eflang.ear.jit.EARInstructionMusicSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EarJitCodeSupplier implements Supplier<MusicSource> {
    private Composer composer;
    private String earCode;

    public EarJitCodeSupplier(Composer composer, String earCode) {
        this.composer = composer;
        this.earCode = earCode;
    }

    @Override
    public MusicSource get() {
        List<Instruction> instructions = Arrays.asList(earCode.split("(\\r?\\n)+")).stream()
                .map(InstructionParser::parseLine)
                .map(Command::compile)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        StatefulInstructionCompiler instructionCompiler = new StatefulInstructionCompiler(composer);
        return new EARInstructionMusicSource(instructionCompiler, instructions);
    }
}
