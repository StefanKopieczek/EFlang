package eflang.ear.jit;

import eflang.core.*;
import eflang.ear.composer.Composer;
import eflang.ear.core.Command;
import eflang.ear.core.Instruction;
import eflang.ear.core.InstructionParser;
import eflang.ear.core.StatefulInstructionCompiler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EARJit {
    private Composer composer;

    public EARJit(Composer composer) {
        this.composer = composer;
    }

    public void run(String earCode) {
        List<Instruction> instructions = Arrays.asList(earCode.split("(\\r?\\n)+")).stream()
                .map(InstructionParser::parseLine)
                .map(Command::compile)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        StatefulInstructionCompiler instructionCompiler = new StatefulInstructionCompiler(composer);
        MusicSource source = new EARInstructionMusicSource(instructionCompiler, instructions);
        Performer performer = new NullPerformer();
        Parser efParser = new Parser(performer);
        efParser.giveMusic(source);
        efParser.perform();
    }
}
