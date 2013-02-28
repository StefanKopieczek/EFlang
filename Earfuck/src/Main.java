
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import earcompiler.EARCompiler;
import earcompiler.EARException;
import earcompiler.EARPrograms;
import earfuck.Parser;

class Main {
	public static void main(String args[]) throws EARException {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.fibonacci);

		Pattern pattern = Pattern.compile("(\\S{1,2} ?){1,10}");
		Matcher matcher = pattern.matcher(EFCode);
		
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
		
		parser.giveMusic(EFCode);
		parser.perform();
	}
}