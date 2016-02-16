package RZ;

import java.util.*;
import java.io.*;

//import org.ho.yaml.*;
//Object conf = Yaml.load(new File("config.ini"));

class Config {
    static final private String filename = "config.ini";
    private Properties conf;

    Config() throws Exception {
	conf = new Properties();
	conf.load(new FileInputStream("config.ini"));
    }

    String get(String field) {
	return conf.getProperty(field);
    }

    int getInt(String field) {
	return Integer.parseInt(get(field));
    }
}
