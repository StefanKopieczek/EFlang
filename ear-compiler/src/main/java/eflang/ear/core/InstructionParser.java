package eflang.ear.core;

import eflang.ear.operation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InstructionParser {

    private InstructionParser() {}

    public static Command parseLine(String line) {
        line = line.trim();
        if ((line.equals("")) || (isComment(line))) {
            return Command.of(new Noop(), Collections.emptyList());
        }

        List<String> tokens = Arrays.asList(line.split(" +"));
        List<Argument> args = tokens.subList(1, tokens.size()).stream()
                .map(InstructionParser::parseArg)
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

    private static Operation lookupOperation(String opCode) {
        switch (opCode) {
            case "ADD":
                return new Add();
            case "COPY":
                return new Copy();
            case "DIV":
                return new Divide();
            case "ENDWHILE":
                return new EndWhile();
            case "GOTO":
                return new Goto();
            case "IN":
                return new Input();
            case "MOV":
                return new Move();
            case "MUL":
                return new Multiply();
            case "OUT":
                return new Output();
            case "SUB":
                return new Subtract();
            case "WHILE":
                return new While();
            case "ZERO":
                return new Zero();
            default:
                throw new RuntimeException("Unknown opCode: " + opCode);
        }
    }
}