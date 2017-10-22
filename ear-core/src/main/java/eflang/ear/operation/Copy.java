package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Copy implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        switch (args.get(0).getType()) {
            case CONSTANT:
                // If passed a constant arg, just use MOV since it's faster and has the same result.
                return (new Move()).compile(args);

            case CELL:
                int sourceCell = args.get(0).getValue();
                int workingCell = args.get(args.size() - 1).getValue();
                List<Integer> targetCells = args.subList(1, args.size() - 1).stream()
                        .map(Argument::getValue)
                        .collect(Collectors.toList());

                List<Instruction> instructions = new ArrayList<>();

                // MOV source cell into targets and working.
                instructions.addAll((new Move()).compile(ImmutableList.<Argument>builder()
                        .add(Argument.cell(sourceCell))
                        .addAll(targetCells.stream().map(Argument::constant).collect(Collectors.toList()))
                        .add(Argument.constant(workingCell))
                        .build()
                ));

                // Use ADD to move the working cell back to source since we know source is already 0.
                instructions.addAll((new Add()).compile(ImmutableList.of(
                        Argument.cell(workingCell),
                        Argument.constant(sourceCell)
                )));
                return instructions;

            default:
                throw new RuntimeException("Unknown arg type");
        }
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.EITHER)
                .many(ArgumentValidator.Type.CONSTANT)
                .one(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "COPY";
    }
}
