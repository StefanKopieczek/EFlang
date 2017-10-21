package eflang.ear.operation;

import eflang.ear.Argument;
import eflang.ear.core.Instruction;

import java.util.Collections;
import java.util.List;

public class Noop implements Operation {
    @Override
    public List<Instruction> compile(List<Argument> args) {
        return Collections.emptyList();
    }
}
