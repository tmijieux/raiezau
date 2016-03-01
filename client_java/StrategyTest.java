package RZ;

import java.util.*;

class StrategyTest implements Strategy {
    public void share(List<RZFile> files, Tracker tracker, int port) 
	throws Exception {
    	// testing
	files.add(new RZFile("fifi", 2,  "0000", true));
	files.add(new RZFile("ssss", 64, "9870", true));
	files.add(new RZFile("floa", 8,  "1234", false));
	files.add(new RZFile("p0rn", 8,  "6969", false));
	
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

	tracker.doGetfile(files.get(0));
	System.out.println("Getfile." + files.get(0));

	tracker.doGetfile(files.get(0));
	System.out.println("Getfile." + files.get(0));

	tracker.doGetfile(files.get(2));
	System.out.println("Getfile." + files.get(2));

	tracker.doGetfile(files.get(2));
	System.out.println("Getfile." + files.get(2));

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	RZFile file = files.get(1);
	file.peerConnect(0);

	file.peerDoInterested(0);
	System.out.println("Interesting.");

	file.peerDoHave(0);
	System.out.println("Having.");

	int index[] = new int[1];
	index[0] = 0;
	file.peerDoGetpieces(0, index);
	System.out.println("Got.");
    }
}
