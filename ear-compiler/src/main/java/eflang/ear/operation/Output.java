package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.Argument;
import eflang.ear.core.Instruction;

import java.util.List;

public class Output implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() == 1;
        int cell = args.get(0).getValue();

        return ImmutableList.of(
                Instruction.goTo(cell),
                Instruction.ensureHappy(),
                Instruction.rest()
        );
    }
}
