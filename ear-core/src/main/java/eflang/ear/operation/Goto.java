package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.Argument;
import eflang.ear.core.ArgumentValidator;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.List;

public class Goto implements Operation {

    @Override
    public List<Instruction> compile(List<Argument> args) {
        return ImmutableList.of(
                Instruction.goTo(args.get(0).getValue())
        );
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "GOTO";
    }
}
