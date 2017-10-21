package eflang.ear;

import eflang.ear.core.Instruction;
import eflang.ear.operation.Operation;

import java.util.List;

public class Command {
    private Operation operation;
    private List<Argument> arguments;

    private Command(Operation operation, List<Argument> arguments) {
        this.operation = operation;
        this.arguments = arguments;
    }

    public List<Instruction> compile() {
        return operation.compile(arguments);
    }

    public static Command of(Operation operation, List<Argument> arguments) {
        return new Command(operation, arguments);
    }
}
