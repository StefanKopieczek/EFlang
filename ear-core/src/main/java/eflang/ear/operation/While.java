package eflang.ear.operation;

import com.google.common.collect.ImmutableList;
import eflang.ear.core.*;

import java.util.List;

public class While implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        assert args.size() == 1;
        int cell = args.get(0).getValue();

        return ImmutableList.of(
                Instruction.goTo(cell),
                Instruction.startLoop()
        );
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .one(ArgumentValidator.Type.CONSTANT)
                .validate(args);
    }

    public String toString() {
        return "WHILE";
    }
}
