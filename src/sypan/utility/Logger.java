package sypan.utility;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Used to log output to the console.<p>
 * This class cannot be instantiated or sub-classed, it must be used statically.
 * 
 * @author Carl Linley
 **/
public final class Logger {

    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("sypan");

    private Logger() {
    }

    public static void init() {
        java.util.logging.Logger.getLogger("com.jme3").setLevel(Level.SEVERE); // Hide JME3's info and warnings
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LoggingFormat());
        logger.addHandler(handler);
    }

    public static void log(Level level, String message) {
        logger.log(level, message);
    }

    public static void logInfo(String s) {
        logger.info(s);
    }

    public static void logSevere(String s) {
        logger.severe(s);
    }

    public static void logWarning(String s) {
        logger.warning(s);
    }

    public static void logDebug(String toLog) {
        System.out.println("[" + Utility.getDate(false) + " - " + Utility.getTime(false) + "] DEBUG: " + toLog);
    }
}

final class LoggingFormat extends Formatter {

    @Override
    public String format(LogRecord record) {
        return "[" + Utility.getDate(false) + " - " + Utility.getTime(false) + "] " + record.getLevel() + ": " + record.getMessage() + "\r\n";
    }
}