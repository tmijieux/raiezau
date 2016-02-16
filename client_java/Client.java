package RZ;

import java.util.List;
import java.util.ArrayList;

class Client {
    private int port = 8080;

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
	
	tracker.doAnnounce(files, port);
	System.out.println("Announced.");

	tracker.doGetfile(files.get(1));
	System.out.println("Getfile." + files.get(1));

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
	List<File> results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	tracker.doUpdate(files);
    }

    public static void main(String args[]) throws Exception {
	Client me = new Client("localhost", 8080);
	me.share();
    }
}
