package eflang.ear.jit;

import eflang.core.MusicSource;
import eflang.core.Parser;
import eflang.core.Performer;
import eflang.ear.composer.Composer;
import eflang.ear.core.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class EARJit {
    private Composer composer;
    private Performer performer;

    public EARJit(Composer composer, Performer performer) {
        this.composer = composer;
        this.performer = performer;
    }

    public void run(String earCode) {
        CommandParser parser = CommandParser.defaultCommandParser();
        InputStream input = new ByteArrayInputStream(earCode.getBytes(Charset.defaultCharset()));
        List<Instruction> instructions = InputSplitter.split(input)
                .map(parser::parseCommand)
                .map(Command::compile)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        StatefulInstructionCompiler instructionCompiler = new StatefulInstructionCompiler(composer);
        MusicSource source = new EARInstructionMusicSource(instructionCompiler, instructions);
        Parser efParser = new Parser(performer);
        efParser.giveMusic(source);
        efParser.perform();
    }
}
