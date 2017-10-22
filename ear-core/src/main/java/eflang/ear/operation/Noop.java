package eflang.ear.operation;

import eflang.ear.core.Argument;
import eflang.ear.core.EARException;
import eflang.ear.core.Instruction;

import java.util.Collections;
import java.util.List;

public class Noop implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        return Collections.emptyList();
    }

    @Override
    public void validateArgs(List<Argument> args) throws EARException {
        Argument.validator()
                .validate(args);
    }

    public String toString() {
        return "NOP";
    }
}
