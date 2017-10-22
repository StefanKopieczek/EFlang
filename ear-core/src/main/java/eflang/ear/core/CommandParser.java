package eflang.ear.core;

import eflang.ear.operation.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandParser {

    private Map<String, Supplier<Operation>> operationRegistry;

    public CommandParser() {
        this.operationRegistry = new HashMap<>();
    }

    public CommandParser registerOperation(String opCode, Supplier<Operation> operationSupplier) {
        this.operationRegistry.put(opCode, operationSupplier);
        return this;
    }

    public Command parseCommand(String line) {
        line = line.trim();
        if ((line.equals("")) || (isComment(line))) {
            return Command.of(new Noop(), Collections.emptyList());
        }

        List<String> tokens = Arrays.asList(line.split(" +"));
        List<Argument> args = tokens.subList(1, tokens.size()).stream()
                .map(CommandParser::parseArg)
                .collect(Collectors.toList());

        return Command.of(lookupOperation(tokens.get(0)), args);
    }

    private static boolean isComment(String line) {
        return line.startsWith("//");
    }

    private static Argument parseArg(String arg) {
        if (arg.startsWith("@")) {
            return Argument.cell(Integer.parseInt(arg.substring(1)));
        } else {
            return Argument.constant(Integer.parseInt(arg));
        }
    }

    private Operation lookupOperation(String opCode) {
        if (operationRegistry.containsKey(opCode)) {
            return operationRegistry.get(opCode).get();
        } else {
            throw new RuntimeException("Unknown opCode: " + opCode);
        }
    }

    public static CommandParser defaultCommandParser() {
        return new CommandParser()
                .registerOperation("ADD", Add::new)
                .registerOperation("COPY", Copy::new)
                .registerOperation("DIV", Divide::new)
                .registerOperation("ENDWHILE", EndWhile::new)
                .registerOperation("GOTO", Goto::new)
                .registerOperation("IN", Input::new)
                .registerOperation("MOV", Move::new)
                .registerOperation("MUL", Multiply::new)
                .registerOperation("OUT", Output::new)
                .registerOperation("SUB", Subtract::new)
                .registerOperation("WHILE", While::new)
                .registerOperation("ZERO", Zero::new);
    }
}