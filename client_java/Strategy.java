package RZ;

import java.util.*;

interface Strategy {
    void share(Map<String, RZFile> files, TrackerSocket tracker, int port) 	
	throws Exception;
}
