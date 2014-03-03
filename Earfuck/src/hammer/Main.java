package hammer;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		String testPath;
		if (args.length >= 1) {
			testPath = args[0];
		}
		else {
			testPath = System.getProperty("user.dir");
		}
		File testDir = new File(testPath);
		
		File[] contents = testDir.listFiles();
		
		for (File file : contents) {
			if (!file.isDirectory()) {
				continue;
			}
			
			HammerLog.setLogLevel(HammerLog.LogLevel.NONE);
			HammerSuite loadedSuite = null;
			try {
				loadedSuite = HammerLoader.loadSuite(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HammerLog.setLogLevel(HammerLog.LogLevel.INFO);
			loadedSuite.run();
		}
	}
}
