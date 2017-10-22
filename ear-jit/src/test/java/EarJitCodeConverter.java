import eflang.core.MusicSource;
import eflang.ear.composer.Composer;
import eflang.ear.core.Command;
import eflang.ear.core.Instruction;
import eflang.ear.core.CommandParser;
import eflang.ear.core.StatefulInstructionCompiler;
import eflang.ear.jit.EARInstructionMusicSource;
import eflang.hammer.CodeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EarJitCodeConverter implements CodeConverter {
    private Composer composer;

    public EarJitCodeConverter(Composer composer) {
        this.composer = composer;
    }

    @Override
    public MusicSource apply(String code) {
        List<Instruction> instructions = Arrays.asList(code.split("(\\r?\\n)+")).stream()
                .map(CommandParser::parseCommand)
                .map(Command::compile)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        StatefulInstructionCompiler instructionCompiler = new StatefulInstructionCompiler(composer);
        return new EARInstructionMusicSource(instructionCompiler, instructions);
    }
}
