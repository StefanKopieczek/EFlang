import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.factorial);
		
		Pattern pattern = Pattern.compile("(.{1,2} ){1,8}");
		Matcher matcher = pattern.matcher(EFCode);

		while (matcher.find()) {
			System.out.println(matcher.group());
		}
		
		parser.perform(EFCode);
	}
}