package rz;

import java.io.*;
import java.util.*;

class Config {
    private Config() {
    }

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
	} catch (IOException e) {
            Log.abort("Cannot load configuration file '"+ path +"'");
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
