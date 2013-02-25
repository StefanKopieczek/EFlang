import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.factorial);
		
}
}