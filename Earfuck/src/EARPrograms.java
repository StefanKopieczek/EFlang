
public class EARPrograms {
	public static String test = "IN 0;IN 1;ADD @0 1;OUT 1;";
	public static String adder = 	"IN 1;" +
									"IF 1;" +
									"ADD @1 0;" +
									"IN 1;" +
									"REPIF;" +
									"OUT 0;";
	public static String destroyer = "IN 0; OUT 0; ZERO 0; OUT 0;";
	public static String fibonacci_broken = "ADD 1 0; IF 0; ADD @0 1 2; OUT 1; " +
									"ADD @1 2 0; OUT 2; ADD 2 1; REPIF;";
	
	public static String fibonacci = "ADD 1 0; OUT 0;IF 0; ADD @0 1 2; OUT 1; ADD @2 0;" +
									"ADD @1 0 2; OUT 0; ADD @2 1; REPIF;";
}
