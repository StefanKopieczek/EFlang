package earfuck;

import java.util.Scanner;

public class SystemIoManager implements IoManager {

	@Override
	public void requestInput(Parser parser) {
		Scanner sc = new Scanner(System.in);
		System.out.print(":> ");
		parser.giveInput(sc.nextInt());
	}

	@Override
	public void output(int value) {
		System.out.println(String.valueOf(value));
	}

}
