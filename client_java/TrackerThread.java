package RZ;

import java.util.*;
import java.io.*;
import java.lang.*;

/*
  Thread t = new Thread(new TrackerThread());
  t.start();
 */

class TrackerThread implements Runnable {
    private final int sleepTime = 4000;
    
    TrackerThread() {
    }

    public void run() {
	while(true) {
	    // update 
	    Thread.sleep(sleepTime);
	}
    }
}
