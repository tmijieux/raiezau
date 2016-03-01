package RZ;

import java.util.*;

interface Strategy {
    void share(List<RZFile> files, Tracker tracker, int port) 	
	throws Exception;
}
