package RZ;

import java.io.*;
import java.util.logging.*;

class Logs {
    static private final String loggerName = "RZLog";
    static private final String filePath   = "./RZ.log";
    static private final Level level = Level.ALL;

    static final Logs write = new Logs();
    static private Logger logger;
    static private FileHandler fh;
    
    private Logs() {
	try {
	    logger = Logger.getLogger(Logs.loggerName);
	    fh = new FileHandler(Logs.filePath);
	    logger.addHandler(fh);
	    logger.setLevel(Logs.level);
	    SimpleFormatter formatter = new SimpleFormatter();
	    fh.setFormatter(formatter);
	}
	catch (Exception e) {
	    System.err.println("Cannot initiate logging capabilities");
	    System.exit(1);
	}
    }

    void info(String s) {
	logger.info(s);
    }
    void info(String format, Object... args) {
	logger.info(String.format(format, args));
    }

    void warning(String s) {
	logger.warning(s);
    }
    void warning(String format, Object... args) {
	logger.warning(String.format(format, args));
    }

    void severe(String s) {
	logger.severe(s);
    }
    void severe(String format, Object... args) {
	logger.severe(String.format(format, args));
    }
}
