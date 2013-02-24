import org.jfugue.*;

class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		EARCompiler compiler = new EARCompiler();
		String EFCode = compiler.compile(EARPrograms.fibonacci);
		System.out.println(EFCode);
		parser.perform(EFCode);
	}
}