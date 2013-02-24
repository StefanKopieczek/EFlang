import org.jfugue.*;

class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		System.out.println("PARSER CREATED");
		EARCompiler compiler = new EARCompiler();
		System.out.println("COMPILING");
		String EFCode = compiler.compile(EARPrograms.test);
		System.out.println(EFCode);
		parser.perform(EFCode);
		
	}
}