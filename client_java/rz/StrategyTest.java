package rz;

import java.util.*;

class StrategyTest implements Strategy {
    private Tracker tracker;

    public StrategyTest() {
    }

    private void testGetfile(File file) {
	tracker.doGetfile(file);
	System.out.println("Getfile: " + file);
    }

    private List<File> testLook(LookRequest lr) {
        List<File> results = tracker.doLook(lr);
	System.out.println("Looked: " + results);
	return results;
    }

    private void testTracker() {
	List<File> fileList = FileManager.getFileList();
        
	testGetfile(fileList.get(0));
	testGetfile(fileList.get(1));
	testGetfile(fileList.get(2));

	// exist beacause it's mine
	LookRequest lr = new LookRequest();
	lr.addFilename("file.dat");
	lr.addSizeLT(8000);
        testLook(lr);

	// not exist
	lr = new LookRequest();
	lr.addFilename("file.XXX");
	lr.addSizeLT(1024);
        testLook(lr);

	tracker.doUpdate(fileList);
    }

    private void testPeer() {
	LookRequest lr = new LookRequest();
	lr.addFilename("fifi.dat");
	lr.addSizeLT(2048);
        List<File> files = testLook(lr);

	File file;
	try {
	    file = files.get(0);
	} catch (Exception e) {
	    Log.warning(e.toString());
	    return;
	}
	Log.info("File: " + file);
	testGetfile(file);

	List<Peer> peers = file.getPeerList();
	Peer peer;
	try {
	    peer = peers.get(0);
	} catch (Exception e) {
	    Log.warning(e.toString());
	    return;
	}
	Log.info("File: " + file);
	peer.doInterested(file);
	Log.info("File: " + file);
	peer.doHave(file);
    }

    @Override
    public void share(Tracker tracker) {
        this.tracker = tracker;
	testTracker();
	testPeer();
    }
}
