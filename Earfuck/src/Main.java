import org.jfugue.*;

class Main {
	public static void main(String args[]) {
		Parser parser = new Parser();
		
		parser.perform(Compositions.sum);
	}
}