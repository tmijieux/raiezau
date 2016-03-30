package rz;

import java.util.*;
import java.io.*;

class Config {
    private static final String DEFAULT_INIT_PATH = "config.ini";
    private static final Properties props = new Properties();

    public static void init() {
	init(DEFAULT_INIT_PATH);
    }

    public static void init(String path) {
	try {
	    Log.info("path: " + path);
            FileInputStream in = new FileInputStream(path);
	    props.load(in);
            in.close();
	} catch (Exception e) {
            System.err.println(
                "Cannot load configuration file '"+ path +"'");
            System.exit(1);
	}
    }

    public static String get(String field) {
	return props.getProperty(field);
    }

    public static int getInt(String field) {
	return Integer.parseInt(
            props.getProperty(field)
        );
    }

    public static short getShort(String field) {
	return Short.parseShort(
            props.getProperty(field)
        );
    }
}
