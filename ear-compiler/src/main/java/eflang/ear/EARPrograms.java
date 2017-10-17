package eflang.ear;

public class EARPrograms {
	public static String test = "IN 0;IN 1;ADD @0 1;OUT 1;";
	public static String adder = 	"IN 1;" +
									"WHILE 1;" +
									"ADD @1 0;" +
									"IN 1;" +
									"ENDWHILE;" +
									"OUT 0;";
	
	public static String copy = 	"IN 0;" +
									"COPY 2 1 2 3;" +
									"OUT 0; OUT 1; OUT 2; OUT 3;";
	
	public static String mulTest = 	"IN 1;" +
									"IN 2;" +
									"WHILE 2;" +
									"WHILE 2;" +
									"MUL @1 @2 3 0;" +
									"ADD @3 1;" +
									"IN 2;" +
									"ENDWHILE;" +
									"ENDWHILE;" +
									"OUT 1;";
	
	public static String factorial = "IN 0\n" +
									"ADD @0 1 2\n" +
									"SUB 1 2\n" +
									"WHILE 2\n" +
									"WHILE 2\n" +
									"COPY @2 3 4\n" +
									"MUL @3 @1 0 4\n"+
									"MOV @0 1\n" +
									"SUB 1 2\n" +
									"ENDWHILE\n" +
									"ENDWHILE\n" +
									"OUT 1\n";
	
	public static String destroyer = "IN 0; OUT 0; ZERO 0; OUT 0;";
	public static String fibonacci_broken = "ADD 1 0; WHILE 0; ADD @0 1 2; OUT 1; " +
									"ADD @1 2 0; OUT 2; ADD 2 1; ENDWHILE;";
	
	public static String fibonacci = "ADD 1 0; OUT 0;WHILE 0; ADD @0 1 2; OUT 1; ADD @2 0;" +
									"ADD @1 0 2; OUT 0; ADD @2 1; ENDWHILE;";
}
