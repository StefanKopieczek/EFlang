package eflang.ear.operation;

import eflang.ear.Argument;
import eflang.ear.Instruction;

import java.util.Arrays;
import java.util.List;

public interface Operation {
    List<Instruction> compile(List<Argument> args);

    default List<Instruction> compile(Argument... args) {
        return compile(Arrays.asList(args));
    }
}
