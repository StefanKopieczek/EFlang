package eflang.ear.core;

import com.google.common.collect.ImmutableList;
import eflang.ear.operation.Noop;
import eflang.ear.operation.Operation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandParserTest {

    private static CommandParser parser = new CommandParser()
            .registerOperation("TEST", TestOperation::new)
            .registerOperation("NOARGS", ZeroArgsOperation::new);

    @Test
    void testParseEmptyAsNoop() {
        parses("", Command.of(new Noop(), ImmutableList.of()));
    }

    @Test
    void testFailToParseUnknownOperation() {
        doesNotParse("SOMETHING");
    }

    @Test
    void testParsesKnownCommand() {
        parses("TEST 1 2 3", Command.of(
                new TestOperation(),
                ImmutableList.of(
                        Argument.constant(1),
                        Argument.constant(2),
                        Argument.constant(3)
                )
        ));
    }

    @Test
    void testParsesCellReferences() {
        parses("TEST @1 2 @3", Command.of(
                new TestOperation(),
                ImmutableList.of(
                        Argument.cell(1),
                        Argument.constant(2),
                        Argument.cell(3)
                )
        ));
    }

    @Test
    void testChecksArgumentsAreValid() {
        parses("NOARGS", Command.of(new ZeroArgsOperation(), ImmutableList.of()));
        doesNotParse("NOARGS 1 2");
    }

    private void doesNotParse(String line) {
        try {
            parser.parseCommand(line);
            fail("Should have failed to parse");
        } catch (Exception e) {
        }
    }

    private void parses(String line, Command cmd) {
        assertEquals(cmd, parser.parseCommand(line));
    }

    private static class TestOperation implements Operation {

        @Override
        public List<Instruction> compile(List<Argument> args) {
            return null;
        }

        @Override
        public void validateArgs(List<Argument> args) throws EARException {

        }
    }

    private static class ZeroArgsOperation implements Operation {

        @Override
        public List<Instruction> compile(List<Argument> args) {
            return null;
        }

        @Override
        public void validateArgs(List<Argument> args) throws EARException {
            if (args.size() > 0) {
                throw new EARInvalidOpcodeException("Always invalid");
            }
        }
    }
}
