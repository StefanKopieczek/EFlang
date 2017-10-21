package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.Argument;
import eflang.ear.Instruction;

import java.util.List;

public class Input implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() == 1;
        int cell = args.get(0).getValue();

        return ImmutableList.of(
                Instruction.goTo(cell),
                Instruction.ensureSad(),
                Instruction.rest()
        );
    }
}
