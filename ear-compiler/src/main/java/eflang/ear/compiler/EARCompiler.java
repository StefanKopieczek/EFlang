package eflang.ear.compiler;

import eflang.ear.composer.Composer;
import eflang.ear.composer.OnlyRunsComposer;
import eflang.ear.core.Command;
import eflang.ear.core.EARException;
import eflang.ear.core.Scales;
import eflang.ear.core.StatefulInstructionCompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compiler for the EAR language. <br/>
 * Takes in EAR code and compiles to raw EF.
 * @author Ryan Norris
 *
 */
public class EARCompiler {
    private StatefulInstructionCompiler statefulInstructionCompiler;

    /**
     * This stores the start position in the compiled EF code for each
     * EAR command.
     * Index = EAR command index
     * Value = first EF command index in the given EAR command
     */
    private ArrayList<Integer> lineStartPositions;

    public EARCompiler() {
        this(new OnlyRunsComposer(Scales.CMajor));
    }

    public EARCompiler(Composer composer) {
        statefulInstructionCompiler = new StatefulInstructionCompiler(composer);
        resetState();
    }

    private void resetState() {
        statefulInstructionCompiler.resetState();
        lineStartPositions = new ArrayList<>();
    }

    /**
     * Compiles provided EARCode into an EF program
     *
     * @param EARCode full program code.
     * @return String containing EF program
     */
    public String compile(String EARCode) throws EARException {
        resetState();
        List<String> output = new ArrayList<>();

        List<Command> commands = EARParser.parse(EARCode);
        output.add(statefulInstructionCompiler.getStartingNote());

        commands.forEach(cmd -> {
            lineStartPositions.add(output.size());
            List<String> notes = cmd.compile().stream()
                    .map(statefulInstructionCompiler::compileInstruction)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            output.addAll(notes);
        });

        return String.join(" ", output);
    }

    public ArrayList<Integer> getCommandStartPositions() {
        return lineStartPositions;
    }

}
