package rz;

import java.util.*;

class StrategySleepy implements Strategy, Runnable {
    private Tracker tracker;
    private final int MS_SLEEP = 60000;

    public StrategySleepy(Tracker tracker) {
	this.tracker = tracker;
    }

    public void run() {
	List<File> fileList = FileManager.getFileList();
	while(true) {
	    tracker.doUpdate(fileList);
	    try {
		Thread.sleep(MS_SLEEP);
	    } catch (InterruptedException e) {
		break;
	    }
	}	
    }

    @Override
    public void share() {
	run();
    }
}
