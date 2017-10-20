package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.Argument;
import eflang.ear.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Copy implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() >= 3;

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
}
