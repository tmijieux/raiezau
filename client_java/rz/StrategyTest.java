package rz;

import java.util.*;

class StrategyTest implements Strategy {
    private Tracker tracker;

    private void testGetfile(File file) {
	tracker.doGetfile(file);
	System.out.println("Getfile: " + file);
    }

    private void testLook(LookRequest lr) {
        List<File> results = tracker.doLook(lr);
	System.out.println("Looked: " + results);
    }

    @Override
    public void share(Tracker tracker) {
        this.tracker = tracker;

	List<File> fileList = File.getFileList();
        
	testGetfile(fileList.get(0));
	testGetfile(fileList.get(1));
	testGetfile(fileList.get(1));
	testGetfile(fileList.get(2));
	testGetfile(fileList.get(2));

	LookRequest lr = new LookRequest();
	lr.addFilename("bobi");
	lr.addSizeLT(80);
        testLook(lr);
        testLook(lr);

	tracker.doUpdate(fileList);
    }
}
