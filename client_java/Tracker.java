package RZ;

import java.util.List;
import java.util.Iterator;
import java.util.regex.*;
import java.lang.Integer;

class Tracker {
    private PeerSocket socket;

    private Pattern getfilePattern = Pattern.compile(
	"\\s*peers\\s*([a-f0-9]*)\\s*\\[(.*)\\]\\s*");

    Tracker(String ip, int port) throws Exception {
	socket = new PeerSocket(ip, port);
	socket.connect();
    }

    void doAnnounce(List<File> files) throws Exception {
	sendAnnounce(files);
	receiveAnnounce();
    }

    void doGetfile(File file) throws Exception {
	socket.send("getfile %s", file.announceLeech());
	receiveGetfile(file);
    }
    
    void doLook() throws Exception {
	// Prototype à terminer
    }

    private String seedString(List<File> files) {
	String leech = "";
	for (File file : files)
	    if (file.isSeeded())
		leech += file.announceLeech();
	return leech;
    }

    private String leechString(List<File> files) {
	String seed = "";
	for (File file : files)
	    if (!file.isSeeded())
		seed += file.announceSeed();
	return seed;
    }

    private void sendAnnounce(List<File> files) throws Exception {
	String seed = String.format("seed [%s]", seedString(files));
	String leech =String.format("leech [%s]", leechString(files));
	socket.send("announce %s %s", seed, leech);
    }

    private void receiveAnnounce() throws Exception {
	String s = socket.receive();
	if (s.toLowerCase().compareTo("ok") != 0) {
	    throw new Exception("Tracker refused announcement.");
	}
    }

    private void receiveGetfile(File file) throws Exception {
	String response = socket.receive();
	Matcher match = getfilePattern.matcher(response);
	
	if (match.matches()) {
	    String key = match.group(1);
	    if (!file.isKey(key))
		throw new Exception("Wrong key received.");

	    String[] peers = match.group(2).split("\\s+");
	    for(String peer : peers) {
		file.addPeer(Peer.newPeerInline(peer));
	    }
	} else {
	    throw new Exception("Invalid tracker response on getfile");
	}
    }
}
