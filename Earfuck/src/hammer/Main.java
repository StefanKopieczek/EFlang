package hammer;

public class Main {
	public static void main(String[] args) {
		HammerLog.setLogLevel(HammerLog.LogLevel.INFO);
		
		HammerTest ioTest = new HammerTest("IO Test",
				"c4 d4 c4 r b3 c4 c4 r");
		ioTest.addInputTask(3);
		ioTest.addOutputTask(4);
		
		HammerTest loopTest = new HammerTest("Loop Test",
				"c4 d4 c4 r ( d4 d4 d4 c4 c4 ) d4 r");
		loopTest.addInputTask(4);
		loopTest.addOutputTask(8);
		
		EarTest earAddTest = new EarTest("EAR ADD Test",
				"MOV 2 0\n MOV 3 1\n ADD @0 1\n OUT 1\n " +
				"ADD 3 1\n OUT 1");
		earAddTest.addOutputTask(5);
		earAddTest.addOutputTask(8);
		
		EarTest earSubTest = new EarTest("EAR SUB Test",
				"MOV 5 0\n MOV 2 1\n SUB @1 0\n OUT 0\n " +
				"SUB 2 0\n OUT 0");
		earSubTest.addOutputTask(3);
		earSubTest.addOutputTask(1);
		
		LobeTest lobeTest = new LobeTest("LOBE Addition Test",
				"x = 3\ny = 4\nprint x + y\n" +
				"print x + 5\nprint 2 + y\nprint 3 + 4");
		lobeTest.addOutputTask(7);
		lobeTest.addOutputTask(8);
		lobeTest.addOutputTask(6);
		lobeTest.addOutputTask(7);
		
		ioTest.run();
		loopTest.run();
		earAddTest.run();
		earSubTest.run();
		lobeTest.run();
	}
}
