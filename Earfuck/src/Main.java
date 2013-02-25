
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import earfuck.Parser;
import earcompiler.EARCompiler;
import earcompiler.EARPrograms;

class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.factorial);
		System.out.println(EFCode);
		parser.perform(EFCode);
	}
}