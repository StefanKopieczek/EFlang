package eflang.hammer;

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
        DEV ("  DEV: "),
        DEBUG ("DEBUG: "),
        INFO (""),
        ERROR ("ERROR: "),
        NONE ("");

        public String prefix;

        LogLevel(String s) {
            prefix = s;
        }
    }

    /**
     * Messages with level above this will be printed to stdout.
     */
    private static LogLevel PRINT_LEVEL = LogLevel.INFO;

    /**
     * Messages with level above this will be logged to LOG_FILE.
     */
    private static LogLevel LOG_LEVEL = LogLevel.DEBUG;

    /**
     * The file to write the test log to.
     */
    private static String LOG_FILE = "hammer.log";

    /**
     * Set the level at which we print messages to stdout.
     * @param level
     */
    public static void setPrintLevel(LogLevel level) {
        PRINT_LEVEL = level;
    }

    /**
     * Get the level at which we are printing messages to stdout.
     * @return
     */
    public static LogLevel getPrintLevel() {
        return PRINT_LEVEL;
    }

    /**
     * Set the level at which we print messages to file.
     * @return
     */
    public static void setLogLevel(LogLevel level) {
        LOG_LEVEL = level;
    }

    /**
     * Get the level at which we are printing messages to file.
     * @return
     */
    public static LogLevel getLogLevel() {
        return LOG_LEVEL;
    }

    /**
     * Clear the log file.
     */
    public static void wipeLog() {
        try {
            FileWriter wr = new FileWriter(LOG_FILE);
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Base level logging function.</br>
     * Logs the given text.
     * @param text - text to log
     * @param level - level at which to log
     * @param newLine - whether to end with a newline or not
     */
    public static void log(String text, LogLevel level, boolean newLine) {
        text = level.prefix + text;

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

    /**
     * Logs the given message, ending with a newline.
     * @param text
     * @param level
     */
    public static void log(String text, LogLevel level) {
        log(text, level, true);
    }

    /**
     * Logs the given message at level INFO
     * @param text
     */
    public static void info(String text) {
        log(text, LogLevel.INFO);
    }

    /**
     * Logs the given message at level DEBUG
     * @param text
     */
    public static void debug(String text) {
        log(text, LogLevel.DEBUG);
    }

    /**
     * Logs the given message at level ERROR
     * @param text
     */
    public static void error(String text) {
        log(text, LogLevel.ERROR);
    }
}
