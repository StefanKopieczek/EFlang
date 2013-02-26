
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import earfuck.Parser;
import earcompiler.EARCompiler;
import earcompiler.EARPrograms;

class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.copy);

		Pattern pattern = Pattern.compile("(\\S{1,2} ?){1,10}");
		Matcher matcher = pattern.matcher(EFCode);
		
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
		
		parser.perform(EFCode);
	}
}