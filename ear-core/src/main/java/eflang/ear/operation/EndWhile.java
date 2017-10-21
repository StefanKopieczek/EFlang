package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.Instruction;

import java.util.List;

public class EndWhile implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() == 0;

        return ImmutableList.of(
                Instruction.endLoop()
        );
    }
}
