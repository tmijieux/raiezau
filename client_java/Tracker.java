package RZ;

import java.util.*;
import java.util.regex.*;
import java.lang.*;

class Tracker {
    private PeerSocket socket;

    private Pattern getfilePattern = Pattern.compile(
	"\\s*peers\\s*([a-f0-9]*)\\s*\\[(.*)\\]\\s*");
    private Pattern lookPattern = Pattern.compile(
	"\\s*list\\s*\\[(.*)\\]\\s*");

    Tracker(String ip, int port) throws Exception {
	socket = new PeerSocket(ip, port);
	socket.connect();
    }

    void doAnnounce(List<RZFile> files, int port) throws Exception {
	doDeclare("announce " + listenString(port), files);
    }

    void doUpdate(List<RZFile> files) throws Exception {
	doDeclare("update", files);
    }

    void doGetfile(RZFile file) throws Exception {
	socket.send("getfile %s", file.announceLeech());
	receiveGetfile(file);
    }
    
    List<RZFile> doLook(LookRequest lr) throws Exception {
	socket.send("look [%s]", lr);
	return receiveLook();
    }

    private String listenString(int port) {
	return "listen " + port;
    }

    private String seedString(List<RZFile> files) {
	String leech = "";
	for (RZFile file : files)
	    if (file.isSeeded())
		leech += file.announceLeech();
	return String.format("leech [%s]", leech);
    }

    private String leechString(List<RZFile> files) {
	String seed = "";
	for (RZFile file : files)
	    if (!file.isSeeded())
		seed += file.announceSeed();
	return String.format("seed [%s]", seed);
    }

    private void doDeclare(String announcement, List<RZFile> files)
	throws Exception {
	sendDeclare(announcement, files);
	receiveDeclare();
    }

    private void sendDeclare(String announcement, List<RZFile> files)
	throws Exception {
	socket.send("%s %s %s", announcement, 
		    seedString(files), leechString(files));
    }

    private void receiveDeclare() throws Exception {
	String s = socket.receive();
	if (s.toLowerCase().compareTo("ok") != 0) {
	    throw new Exception("Tracker does not responde OK");
	}
    }

    private void receiveGetfile(RZFile file) throws Exception {
	String response = socket.receive();
	Matcher match = getfilePattern.matcher(response);
	
	if (!match.matches())
	    throw new Exception("Invalid tracker response on getfile");

	String key = match.group(1);
	if (!file.isKey(key))
	    throw new Exception("Wrong key received.");
	
	String[] peers = match.group(2).split("\\s+");
	for(String peer : peers) {
	    file.addPeer(Peer.newPeerInline(peer));
	}
    }

    private List<RZFile> receiveLook() throws Exception {
	String response = socket.receive();
	Matcher match = lookPattern.matcher(response);

	if (!match.matches())
	    throw new Exception("Invalid tracker response on look.");
	
	String[] filesStr = match.group(1).split("\\s+");
	if (filesStr.length % 4 != 0)
	    throw new Exception("Invalid list size in look response");
	
	List<RZFile> files = new ArrayList<RZFile>();
	for (int i = 0; i < filesStr.length; i += 4) {
	    files.add(new RZFile(filesStr[i],
			       Integer.parseInt(filesStr[i+1]),
			       Integer.parseInt(filesStr[i+2]),
			       filesStr[i+3]));
	}
	return files;
    }
}
