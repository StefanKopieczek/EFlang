package eflang.ear.compiler;

import eflang.ear.core.Command;
import eflang.ear.core.CommandParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EARParser {

    private EARParser() {}

    public static List<Command> parse(String earCode) {
        List<String> lines = Arrays.asList(earCode.split("(\\r?\\n)+"));
        return lines.stream().map(CommandParser::parseLine).collect(Collectors.toList());
    }
}