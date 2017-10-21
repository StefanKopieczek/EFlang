package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.Argument;
import eflang.ear.Instruction;

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
