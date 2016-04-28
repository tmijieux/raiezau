package rz;

import java.net.*;
import java.security.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class Tracker {
    private Socket socket;

    public Tracker(String ip, short port) throws IOException {
	socket = new Socket(ip, port);
    }

    public void doAnnounce(List<File> files, int port) {
	doDeclare("announce " + listenString(port), files);
    }

    public void doUpdate(List<File> files) {
	doDeclare("update", files);
    }

    private void doDeclare(String announcement, List<File> files) {
	sendDeclare(announcement, files);
	try {
	    receiveDeclare();
	} catch (RZInvalidResponseException e) {
	    Log.warning(this + " " + e.toString());
	    socket.sendError();
	}
    }

    public void doGetfile(File file) {
	socket.send("getfile %s\n", file.getKey());
	try {
	    receiveGetfile(file);
	} catch (RZInvalidResponseException e) {
	    Log.warning(this + " " + e.toString());
	    socket.sendError();
	} catch (RZNoPeerException e) {
	    Log.warning(this + " " + e.toString());
	}
    }

    public List<File> doLook(LookRequest lr) {
	socket.send("look [%s]\n", lr);
	try {
	    return receiveLook();
	} catch (RZInvalidResponseException e) {
	    Log.warning(this + " " + e.toString());
	    socket.sendError();
	} catch (RZNoFileException e) {
	    Log.warning(this + " " + e.toString());
	}
	return new ArrayList<File>();
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

    private void sendDeclare(String announcement, List<File> files) {
	socket.send("%s %s %s\n", announcement,
                    seedString(files), leechString(files));
    }

    private void receiveDeclare() throws RZInvalidResponseException {
	socket.receiveAndGetMatcher(PatternMatcher.OK);
    }

    private void receiveGetfile(File file)
	throws RZNoPeerException, RZInvalidResponseException {
	Matcher match = socket.receiveAndGetMatcher(
	    PatternMatcher.GETFILE);

	if (!file.isKey(match.group(1)))
	    throw new RZWrongKeyException("in getfile.");

	if (PatternMatcher.EMPTY.matches(match.group(2)))
	    throw new RZNoPeerException();

	String[] peers = match.group(2).split("\\s+");
	for(String peer : peers) {
	    try {
		FilePeer fp = new FilePeer(peer, file); 
		file.addPeer(fp);
	    } catch (InvalidParameterException e) {
		Log.warning(e.toString());
		socket.sendError();
	    }
	}
    }

    private List<File> receiveLook()
	throws RZNoFileException, RZInvalidResponseException {
	Matcher match = socket.receiveAndGetMatcher(
	    PatternMatcher.LOOK);

	if (PatternMatcher.EMPTY.matches(match.group(1)))
	    throw new RZNoFileException();

	String[] filesStr = match.group(1).split("\\s+");
	if (filesStr.length % 4 != 0) {
            throw new RZInvalidResponseException(
                "Invalid list size in look response. " +
                filesStr.toString()
            );
        }

	List<File> files = new ArrayList<File>();
	for (int i = 0; i < filesStr.length; i += 4) {	    
	    File file = FileManager.addFile(
                filesStr[i], 
                Integer.parseInt(filesStr[i+1]),
                Integer.parseInt(filesStr[i+2]),
                filesStr[i+3]
            );
	    files.add(file);
	}
	return files;
    }
}
