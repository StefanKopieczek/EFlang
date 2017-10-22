package eflang.ear.core;

import eflang.ear.operation.Operation;

import java.util.List;
import java.util.stream.Collectors;

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

    public void validate() throws EARException {
        operation.validateArgs(arguments);
    }

    public static Command of(Operation operation, List<Argument> arguments) {
        Command cmd = new Command(operation, arguments);
        try {
            cmd.validate();
        } catch (EARException e) {
            throw new RuntimeException("Invalid command: " + cmd, e);
        }
        return cmd;
    }

    public String toString() {
        String argString = String.join(" ",
                arguments.stream().map(Argument::toString).collect(Collectors.toList()));
        return String.format("%s %s", operation, argString);
    }
}
