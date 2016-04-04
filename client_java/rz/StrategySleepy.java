package rz;

import java.util.*;

class StrategySleepy implements Strategy {
    public StrategySleepy() {
    }

    private final int MS_SLEEP = 10000;

    @Override
    public void share(Tracker tracker) {
	List<File> fileList = File.getFileList();

	while(true) {
	    tracker.doUpdate(fileList);
	    try {
		Thread.sleep(MS_SLEEP);
	    } catch (InterruptedException e) {
		break;
	    }
	}
    }
}
