package RZ;

import java.util.*;
import java.io.*;
import java.lang.*;

class Client {
    private Config conf;
    private int port;

    private List<RZFile> files;
    private Tracker tracker;

    Client() throws Exception {
	conf = new Config();

	port = conf.getInt("user-port");
	tracker = new Tracker(conf.get("tracker-address"), 
			      conf.getInt("tracker-port"));
	files = new ArrayList<RZFile>();
    }

    void share() throws Exception {
	// testing
	files.add(new RZFile("fifi", 2, "0000", true));
	files.add(new RZFile("ssss", 2, "9870", true));
	files.add(new RZFile("floa", 8, "1234", false));
	files.add(new RZFile("p0rn", 8, "6969", false));
	
	tracker.doAnnounce(files, port);
	System.out.println("Announced.");

	tracker.doGetfile(files.get(1));
	System.out.println("Getfile." + files.get(1));

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
	List<RZFile> results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	tracker.doUpdate(files);
    }

    public static void main(String args[]) throws Exception {
	Client me = new Client();
	me.share();
	
    }
}
