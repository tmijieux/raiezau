package rz;

import java.util.*;

class StrategySleepy implements Strategy {
    @Override
    public void share(Tracker tracker) {
	List<File> fileList = File.getFileList();

	while(true) {
	    tracker.doUpdate(fileList);
	    try {
		wait(1000);
	    } catch (InterruptedException e) {
		break;
	    }
	}
    }
}
