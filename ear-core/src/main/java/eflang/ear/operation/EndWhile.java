package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.List;

public class EndWhile implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        return ImmutableList.of(
                Instruction.endLoop()
        );
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .validate(args);
    }

    public String toString() {
        return "ENDWHILE";
    }
}
