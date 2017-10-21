package eflang.ear.operation;

import eflang.ear.core.Argument;
import eflang.ear.core.Instruction;

import java.util.Arrays;
import java.util.List;

public interface Operation {
    List<Instruction> compile(List<Argument> args);

    default List<Instruction> compile(Argument... args) {
        return compile(Arrays.asList(args));
    }
}
