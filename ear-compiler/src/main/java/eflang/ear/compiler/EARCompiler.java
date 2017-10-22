package eflang.ear.compiler;

import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.core.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compiler for the EAR language. <br/>
 * Takes in EAR code and compiles to raw EF.
 * @author Ryan Norris
 *
 */
public class EARCompiler {
    private Composer composer;

    /**
     * This stores the start position in the compiled EF code for each
     * EAR command.
     * Index = EAR command index
     * Value = first EF command index in the given EAR command
     */
    private List<Long> lineStartPositions;

    public EARCompiler() {
        this(new OnlyRunsComposer(Scales.CMajor));
    }

    public EARCompiler(Composer composer) {
        this.composer = composer;
    }

    /**
     * Compiles provided EARCode into an EF program
     *
     * @param EARCode full program code.
     * @return String containing EF program
     */
    public String compile(String EARCode) throws EARException {
        StatefulInstructionCompiler instructionCompiler = new StatefulInstructionCompiler(composer);

        InputStream input = new ByteArrayInputStream(EARCode.getBytes(Charset.defaultCharset()));

        CommandParser parser = CommandParser.defaultCommandParser();
        Stream<Command> commands = InputSplitter.split(input).map(parser::parseCommand);

        List<List<String>> notesPerCommand = commands.map(cmd ->
            cmd.compile().stream()
                    .map(instructionCompiler::compileInstruction)
                    .flatMap(List::stream)
                .collect(Collectors.toList())
        ).collect(Collectors.toList());

        AtomicLong accumulator = new AtomicLong(0);
        lineStartPositions = notesPerCommand.stream()
                .map(List::size)
                .map(accumulator::addAndGet)
                .collect(Collectors.toList());

        List<String> output = notesPerCommand.stream().flatMap(List::stream).collect(Collectors.toList());

        return String.join(" ", output);
    }

    public List<Long> getCommandStartPositions() {
        return lineStartPositions;
    }

}
