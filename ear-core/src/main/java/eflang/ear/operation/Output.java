package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.List;

public class Output implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        int cell = args.get(0).getValue();

        return ImmutableList.of(
                Instruction.goTo(cell),
                Instruction.ensureHappy(),
                Instruction.rest()
        );
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "OUT";
    }
}
