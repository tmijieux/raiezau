package rz;

import java.util.*;

class StrategyTest implements Strategy {
    private Tracker tracker;

    private void testGetfile(File file) {
	tracker.doGetfile(file);
	System.out.println("Getfile: " + file);
    }

    private List<File> testLook(LookRequest lr) {
        List<File> results = tracker.doLook(lr);
	System.out.println("Looked: " + results);
	return results;
    }

    @Override
    public void share(Tracker tracker) {
        this.tracker = tracker;

	List<File> fileList = File.getFileList();
        
	testGetfile(fileList.get(0));
	testGetfile(fileList.get(1));
	testGetfile(fileList.get(2));

	// exist beacause it's mine
	LookRequest lr = new LookRequest();
	lr.addFilename("p0rn.dat");
	lr.addSizeLT(8000);
        testLook(lr);

	// not exist
	lr = new LookRequest();
	lr.addFilename("gayPorn.XXX");
	lr.addSizeLT(1024);
        testLook(lr);

	// other peer
	lr = new LookRequest();
	lr.addFilename("fifi.dat.XXX");
	lr.addSizeLT(2048);
        List<File> files = testLook(lr);

	testGetfile(files.get(0));

	tracker.doUpdate(fileList);
    }
}
