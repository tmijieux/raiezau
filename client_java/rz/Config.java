package rz;

import java.util.*;
import java.io.*;

class Config {
    private static final String filename = "config.ini";
    private static final Properties props = new Properties();

    static {
	try {
            FileInputStream in = new FileInputStream(filename);
	    props.load(in);
            in.close();
	} catch (Exception e) {
            System.err.println(
                "Cannot load configuration file '"+filename+"'");
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
