package RZ;

import java.util.*;

class StrategyTest implements Strategy {
    public void share(Map<String, RZFile> files, TrackerSocket tracker, int port) 
	throws Exception {

    	// testing
	new RZFile("fifi.dat").putInMap(files);
	new RZFile("ssss.dat").putInMap(files);
	new RZFile("floa.dat").putInMap(files);
	new RZFile("p0rn.dat").putInMap(files);
	
	tracker.doAnnounce(files, port);
	System.out.println("Announced.");

	tracker.doGetfile(files.get("a5aec02572473b2c486856a02d066c8d"));
	System.out.println("Getfile." + files.get(1));

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
	List<RZFile> results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	tracker.doUpdate(files);

	tracker.doGetfile(files.get("a5aec02572473b2c486856a02d066c8d"));
	System.out.println("Getfile." + files.get("a5aec02572473b2c486856a02d066c8d"));

	tracker.doGetfile(files.get("a5aec02572473b2c486856a02d066c8d"));
	System.out.println("Getfile." + files.get("a5aec02572473b2c486856a02d066c8d"));

	tracker.doGetfile(files.get("17a21d4b0d8d05ab60ae538c5d9836cf"));
	System.out.println("Getfile." + files.get("17a21d4b0d8d05ab60ae538c5d9836cf"));

	tracker.doGetfile(files.get("17a21d4b0d8d05ab60ae538c5d9836cf"));
	System.out.println("Getfile." + files.get("17a21d4b0d8d05ab60ae538c5d9836cf"));

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);

	results = tracker.doLook(lr);
	System.out.println("Looked." + results);
	/*
	RZFile file = files.get("ssss");
	file.peerConnect(0);

	file.peerDoInterested(0);
	System.out.println("Interesting.");

	file.peerDoHave(0);
	System.out.println("Having.");

	int index[] = new int[1];
	index[0] = 0;
	file.peerDoGetpieces(0, index);
	System.out.println("Got.");
	*/
    }
}
