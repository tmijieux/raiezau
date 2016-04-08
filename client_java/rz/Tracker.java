package rz;

import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class Tracker {
    Socket socket;

    public Tracker(String ip, short port)
      throws java.net.UnknownHostException, java.io.IOException {
	socket = new Socket(ip, port);
    }

    public void doAnnounce(List<File> files, int port) {
	doDeclare("announce " + listenString(port), files);
    }

    public void doUpdate(List<File> files) {
	doDeclare("update", files);
    }

    public void doGetfile(File file) {
	socket.send("getfile %s\n", file.getKey());
	receiveGetfile(file);
    }

    public List<File> doLook(LookRequest lr) {
	socket.send("look [%s]\n", lr);
	return receiveLook();
    }

    private String listenString(int port) {
	return "listen " + port;
    }

    private String leechString(List<File> files) {
	String leech = "";
	for (File file : files) {
	    if (!file.isSeeded())
		leech += file.getKey() + " ";
	}
	return "leech ["+leech+"]";
    }

    private String seedString(List<File> files) {
	String seed = "";
	for (File file : files) {
	    if (file.isSeeded())
		seed += file.announceSeed() + " ";
	}
	return "seed ["+seed+"]";
    }

    private void doDeclare(String announcement, List<File> files) {
	sendDeclare(announcement, files);
	receiveDeclare();
    }

    private void sendDeclare(String announcement, List<File> files) {
	socket.send("%s %s %s\n", announcement,
                    seedString(files), leechString(files));
    }

    private void receiveDeclare() {
	try {
	    socket.receiveAndGetMatcher(PatternMatcher.OK);
	} catch (RZNoResponseException e) {
	    Log.warning(this + " is not responding.");
	}
    }

    private void receiveGetfile(File file) {
	Matcher match;
	try {
	    match = socket.receiveAndGetMatcher(PatternMatcher.GETFILE);
	} catch (RZNoResponseException e) {
	    Log.warning(this + " is not responding.");
	    return;
	}
	String key = match.group(1);

	if (!file.isKey(key)) {
	    throw new RuntimeException("getfile: wrong key");
        }

	if (PatternMatcher.EMPTY.matches(match.group(2))) {
	    return;
        }

	String[] peers = match.group(2).split("\\s+");
	for(String peer : peers) {
	    file.addPeer(new FilePeer(peer, file));
	}
    }

    private List<File> receiveLook() {
	Matcher match;
	List<File> files = new ArrayList<File>();
	try {
	    match = socket.receiveAndGetMatcher(PatternMatcher.LOOK);
	} catch (RZNoResponseException e) {
	    Log.warning(this + " is not responding.");
	    return files;
	}

	if (PatternMatcher.EMPTY.matches(match.group(1))) {
	    return files;
        }

	String[] filesStr = match.group(1).split("\\s+");
	if (filesStr.length % 4 != 0) {
            throw new RuntimeException(
                "Invalid list size in look response. " +
                filesStr.toString()
            );
        }

	for (int i = 0; i < filesStr.length; i += 4) {
            int localPieceSize = Config.getInt("piece-size");
            int receivedPieceSize = Integer.parseInt(filesStr[i+2]);
	    if (localPieceSize != receivedPieceSize) {
		throw new RuntimeException(
                    "Remote and local piece size do not match");
            }
	    
	    File file = FileManager.addFile(
                filesStr[i], 
                Integer.parseInt(filesStr[i+1]),
                filesStr[i+3]
            );
	    files.add(file);
	}
	return files;
    }
}
