package rz;

import java.util.*;

interface Strategy {
    void share(Map<String, File> files, TrackerSocket tracker, short port);
}
