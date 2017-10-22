package eflang.ear.operation;

import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Add implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        List<Integer> outputCells = args.subList(1, args.size()).stream()
                .map(Argument::getValue)
                .collect(Collectors.toList());

        List<Instruction> instructions = new ArrayList<>();

        switch (args.get(0).getType()) {
            case CONSTANT:
                int value = args.get(0).getValue();
                for (int cell : outputCells) {
                    instructions.add(Instruction.goTo(cell));
                    for (int i = 0; i < value; i++) {
                        instructions.add(Instruction.increment());
                    }
                }
                break;

            case CELL:
                int inputCell = args.get(0).getValue();
                instructions.add(Instruction.goTo(inputCell));

                // Optimization to ensure we don't flip to happy again at the end of the loop, just to have to reset
                // to sad again inside the loop.
                instructions.add(Instruction.ensureSad());
                instructions.add(Instruction.startLoop());
                instructions.add(Instruction.decrement());

                instructions.addAll(outputCells.stream().flatMap(cell -> Stream.of(
                        Instruction.goTo(cell),
                        Instruction.increment()
                        )
                ).collect(Collectors.toList()));

                instructions.add(Instruction.endLoop());
                break;
        }

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
        return "ADD";
    }
}
