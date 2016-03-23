package rz;

import java.util.*;

class StrategyTest implements Strategy {
    private Tracker tracker;
    private Server clientServer;

    private void getFile(String key) {
	tracker.doGetfile(File.getByKey(key));
	System.out.println("Getfile." + File.getByKey(key));
    }

    private void look(LookRequest lr) {
        List<File> results = tracker.doLook(lr);
	System.out.println("Looked." + results);
    }

    @Override
    public void share(Tracker tracker, Server clientServer) {
        this.tracker = tracker;
        this.clientServer = clientServer;

    	// testing

        List<File> fileList = File.getFileList();
        System.err.println(">>>"+tracker+"<<<");
	tracker.doAnnounce(fileList, clientServer.getPort());
	System.out.println("Announced.");

        getFile("a5aec02572473b2c486856a02d066c8d");

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
        look(lr);

	tracker.doUpdate(fileList);

        getFile("a5aec02572473b2c486856a02d066c8d");
        getFile("a5aec02572473b2c486856a02d066c8d");
        getFile("17a21d4b0d8d05ab60ae538c5d9836cf");
        getFile("17a21d4b0d8d05ab60ae538c5d9836cf");

        look(lr);
        look(lr);
        look(lr);
        look(lr);

	/*
          File file = File.getByKey("ssss");
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
