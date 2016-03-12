package rz;

import java.util.*;

class StrategyTest implements Strategy {

    private TrackerSocket tracker;
    private Map<String, File> files;

    private void getFile(String key) {
	tracker.doGetfile(files.get(key));
	System.out.println("Getfile." + files.get(key));
    }

    private void look(LookRequest lr) {
        List<File> results = tracker.doLook(lr);
	System.out.println("Looked." + results);
    }

    @Override
    public void share(Map<String, File> files,
                      TrackerSocket tracker, short port) {
        this.files = files;
        this.tracker = tracker;

    	// testing
	new File("fifi.dat").putInMap(files);
	new File("ssss.dat").putInMap(files);
	new File("floa.dat").putInMap(files);
	new File("p0rn.dat").putInMap(files);

        System.err.println(">>>"+tracker+"<<<");
	tracker.doAnnounce(files, port);
	System.out.println("Announced.");

	tracker.doGetfile(files.get("a5aec02572473b2c486856a02d066c8d"));
	System.out.println("Getfile." + files.get(1));

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
        look(lr);

	tracker.doUpdate(files);

        getFile("a5aec02572473b2c486856a02d066c8d");
        getFile("a5aec02572473b2c486856a02d066c8d");
        getFile("17a21d4b0d8d05ab60ae538c5d9836cf");
        getFile("17a21d4b0d8d05ab60ae538c5d9836cf");

        look(lr);
        look(lr);
        look(lr);
        look(lr);

	/*
          File file = files.get("ssss");
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
