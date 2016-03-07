package RZ;

import java.util.*;

interface Strategy {
    void share(Map<String, RZFile> files, Tracker tracker, int port) 	
	throws Exception;
}
