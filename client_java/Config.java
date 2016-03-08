package RZ;

import java.util.*;
import java.io.*;

//import org.ho.yaml.*;
//Object conf = Yaml.load(new File("config.ini"));

class Config {
    private static final String filename = "config.ini";
    private static final Properties props = new Properties();

    static {
	try {
            FileInputStream in = new FileInputStream(filename);
	    props.load(in);
            in.close();
	}
	catch (Exception e) {
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
}
