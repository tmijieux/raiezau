package rz;

import java.util.*;

class StrategySleepy implements Strategy {
    public StrategySleepy() {
    }

    @Override
    public void share(Tracker tracker) {
	List<File> fileList = File.getFileList();

	while(true) {
	    tracker.doUpdate(fileList);
	    try {
		Thread.sleep(10000);
	    } catch (InterruptedException e) {
		break;
	    }
	}
    }
}
