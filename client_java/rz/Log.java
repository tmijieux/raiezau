package rz;

import java.io.*;
import java.util.logging.*;

class Log {
    private static final String name = "client.log";
    private static final Logger logger = Logger.getLogger(name);

    static {
	try {
            FileHandler file = new FileHandler(name);
            SimpleFormatter formatter = new SimpleFormatter();

            file.setFormatter(formatter);
	    logger.addHandler(file);
	    logger.setLevel(Level.ALL);
	} catch (Exception e) {
	    System.err.println("Cannot initiate logging capabilities");
	    System.exit(1);
	}
    }


    public static void debug(String s) {
        Log.info("[DEBUG]: " + s);
    }

    public static void info(String s) {
	logger.info(s);
    }

    public static void info(String format, Object... args) {
	logger.info(String.format(format, args));
    }

    public static void warning(String s) {
	logger.warning(s);
    }

    public static void warning(String format, Object... args) {
	logger.warning(String.format(format, args));
    }

    public static void severe(String s) {
	logger.severe(s);
    }

    public static void severe(String format, Object... args) {
	logger.severe(String.format(format, args));
    }
}
