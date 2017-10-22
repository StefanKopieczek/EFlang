package eflang.ear.core;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InputSplitter {
    public static Stream<String> split(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter(Pattern.compile("(\\r?\\n)+"));
        Iterable<String> iterable = () -> scanner;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
