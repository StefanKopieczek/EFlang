package eflang.ear;

import eflang.ear.operation.Operation;

import java.util.List;

public class Command {
    private Operation operation;
    private List<Argument> arguments;

    private Command(Operation operation, List<Argument> arguments) {
        this.operation = operation;
        this.arguments = arguments;
    }

    public static Command of(Operation operation, List<Argument> arguments) {
        return new Command(operation, arguments);
    }

    public Operation getOperation() {
        return operation;
    }

    public List<Argument> getArguments() {
        return arguments;
    }
}
