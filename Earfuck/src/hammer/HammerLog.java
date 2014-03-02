package hammer;

public class HammerLog {
	public static enum LogLevel implements Comparable<LogLevel> {
		DEBUG,
		INFO,
		ERROR,
		NONE;
	}
	
	private static LogLevel LEVEL = LogLevel.INFO;
	
	public static void setLogLevel(LogLevel level) {
		LEVEL = level;
	}
	
	public static void log(String text, LogLevel level) {
		if (level.compareTo(LEVEL) >= 0) {
			System.out.println(text);
		}
	}
	
	public static void info(String text) {
		log(text, LogLevel.INFO);
	}
	
	public static void debug(String text) {
		log(text, LogLevel.DEBUG);
	}
	
	public static void error(String text) {
		log(text, LogLevel.ERROR);
	}
}
