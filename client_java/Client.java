package RZ;

import java.util.List;
import java.util.ArrayList;

class Client {
    private List<File> files;
    private Tracker tracker;
    
    Client(String ip, int port) throws Exception {
	tracker = new Tracker(ip, port);
	files = new ArrayList<File>();
    }

    void share() throws Exception {
	// testing
	files.add(new File("fifi", 2, "0000", true));
	files.add(new File("floa", 8, "1234", false));
	
	tracker.doAnnounce(files);
	System.out.println("Announced.");

	tracker.doGetfile(files.get(1));
	System.out.println("Getfile.");
    }

    public static void main(String args[]) throws Exception {
	Client me = new Client("localhost", 8080);
	me.share();
    }
}
