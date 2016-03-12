package rz;

import java.util.*;
import java.util.regex.*;
import java.lang.*;

class TrackerSocket extends Socket {

    private final Pattern declarePattern = Pattern.compile("ok");
    private final Pattern getfilePattern =
        Pattern.compile("peers ([a-fA-F0-9]*) \\[([^]])*\\]");

    private final Pattern lookPattern =
        Pattern.compile("list \\[([^]])*\\]");

    TrackerSocket(String ip, short port)
      throws java.net.UnknownHostException, java.io.IOException {
	super(ip, port);
    }

    void doAnnounce(Map<String, File> files, short port) {
	doDeclare("announce " + listenString(port), files);
    }

    void doUpdate(Map<String, File> files) {
	doDeclare("update", files);
    }

    void doGetfile(File file) {
	send("getfile %s", file.getKey());
	receiveGetfile(file);
    }

    List<File> doLook(LookRequest lr) {
	send("look [%s]", lr);
	return receiveLook();
    }

    private String listenString(short port) {
	return "listen " + port;
    }

    private String seedString(Map<String, File> files) {
	String leech = "";
	for (Map.Entry<String, File> entry : files.entrySet()) {
	    File file = entry.getValue();
	    if (file.isSeeded())
		leech += file.getKey() + " ";
	}
	return String.format("leech [%s]", leech);
    }

    private String leechString(Map<String, File> files) {
	String seed = "";
	for (Map.Entry<String, File> entry : files.entrySet()) {
	    File file = entry.getValue();
	    if (!file.isSeeded())
		seed += file.announceSeed() + " ";
	}
	return String.format("seed [%s]", seed);
    }

    private void doDeclare(String announcement, Map<String, File> files) {
	sendDeclare(announcement, files);
	receiveDeclare();
    }

    private void sendDeclare(String announcement, Map<String, File> files) {
	send("%s %s %s", announcement,
             leechString(files), seedString(files));
    }

    private void receiveDeclare() {
	receiveMatcher(declarePattern);
    }

    private void receiveGetfile(File file) {
	Matcher match = receiveMatcher(getfilePattern);

	String key = match.group(1);
	if (!file.isKey(key)) {
	    throw new RuntimeException("getfile: wrong key");
        }

	if (RZPattern.isEmpty(match.group(2))) {
	    return;
        }

	String[] peers = match.group(2).split("\\s+");
	for(String peer : peers) {
	    file.addPeer(new Peer(peer));
	}
    }

    private List<File> receiveLook() {
	Matcher match = receiveMatcher(lookPattern);
	List<File> files = new ArrayList<File>();

	if (RZPattern.isEmpty(match.group(1))) {
	    return files;
        }

	String[] filesStr = match.group(1).split("\\s+");
	if (filesStr.length % 4 != 0) {
            throw new RuntimeException("Invalid list size in look response");
        }

	for (int i = 0; i < filesStr.length; i += 4) {
            int localPieceSize = Config.getInt("piece-size");
            int receivedPieceSize = Integer.parseInt(filesStr[i+2]);
	    if (localPieceSize != receivedPieceSize) {
		throw new RuntimeException(
                    "Remote and local piece size not matching");
            }

	    files.add(new File(
                          filesStr[i],
                          Integer.parseInt(filesStr[i+1]),
                          filesStr[i+3]
                      ));
	}
	return files;
    }
}
