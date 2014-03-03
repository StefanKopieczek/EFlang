package hammer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A logging class for HAMMER.
 * [TODO] Save logs to file.
 * @author rynor_000
 *
 */
public class HammerLog {
	public static enum LogLevel implements Comparable<LogLevel> {
		DEV,
		DEBUG,
		INFO,
		ERROR,
		NONE;
	}
	
	private static LogLevel PRINT_LEVEL = LogLevel.INFO;
	private static LogLevel LOG_LEVEL = LogLevel.DEBUG;
	private static String LOG_FILE = "hammer.log";
	
	public static void setPrintLevel(LogLevel level) {
		PRINT_LEVEL = level;
	}
	
	public static LogLevel getPrintLevel() {
		return PRINT_LEVEL;
	}
	
	public static void setLogLevel(LogLevel level) {
		LOG_LEVEL = level;
	}
	
	public static LogLevel getLogLevel() {
		return LOG_LEVEL;
	}
	
	public static void wipeLog() {
		try {
			FileWriter wr = new FileWriter(LOG_FILE);
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String text, LogLevel level, boolean newLine) {
		if (level.compareTo(PRINT_LEVEL) >= 0) {
			if (newLine) {
				System.out.println(text);
			}
			else {
				System.out.print(text);
			}
		}
		
		if (level.compareTo(LOG_LEVEL) >= 0) {
			try {
				FileWriter wr = new FileWriter(LOG_FILE, true);
				BufferedWriter bw = new BufferedWriter(wr);
				PrintWriter out = new PrintWriter(bw);
				if (newLine) {
					out.println(text);
				}
				else {
					out.print(text);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String text, LogLevel level) {
		log(text, level, true);
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
