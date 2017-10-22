package eflang.ear.core;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InputSplitter {
    public static Stream<String> split(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter(Pattern.compile("(\\r?\\n)+"));
        return Stream.generate(scanner::next);
    }
}
