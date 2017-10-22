package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Move implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        List<Instruction> instructions = new ArrayList<>();

        List<Integer> outputCells = args.subList(1, args.size()).stream()
                .map(Argument::getValue)
                .collect(Collectors.toList());

        // Zero all output cells.
        outputCells.forEach(cell ->
                instructions.addAll((new Zero()).compile(ImmutableList.of(Argument.constant(cell))))
        );

        // Use ADD to move the values.
        instructions.addAll((new Add()).compile(args));

        return instructions;
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.EITHER)
                .many(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "MOV";
    }
}
