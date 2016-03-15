package rz;

import java.util.*;
import java.util.regex.*;

class PeerSocket extends Socket {

    public PeerSocket(String ip, short port)
      throws java.net.UnknownHostException, java.io.IOException {
	super(ip, port);
    }

    /* -------------------- SEND -------------------- */

    public void sendInterested(File file) {
	send("interested %s", file.getKey());
    }

    public void sendHave(File file) {
	send("have %s %s", file.getKey(), "TODO");
    }

    public void sendGetpieces(File file, int[] index) {
	send("getpieces %s [%s]", file.getKey(), indexString(index));
    }

    public void sendData(File file, int[] index) {
	send("data %s [%s]", file.getKey(), "TODO");
    }

    /* -------------------- Reception -------------------- */

    public void parseInterested(String question) {
	Matcher match = PatternMatcher.INTERESTED.getMatcher(question);
	File file = Client.client.getFile(match.group(1));
	sendHave(file);
    }

    public File parseHave(String question) {
	Matcher match = PatternMatcher.HAVE.getMatcher(question);
	String strBufferMap = match.group(2);
	byte bufferMap[] = strBufferMap.getBytes();
	// TODO: update the buffer

	return Client.client.getFile(match.group(1));
    }

    public void parseGetpieces(String question) {
	Matcher match = PatternMatcher.GETPIECES.getMatcher(question);
	String key = match.group(1);
	String[] strIndex = match.group(2).split("\\s+");
	int[] index = Arrays.stream(strIndex)
            .mapToInt(Integer::parseInt)
            .toArray();
	File file = Client.client.getFile(match.group(1));
        sendData(file, index);
    }

    public void parseData(String question) {
	Matcher match = PatternMatcher.DATA.getMatcher(question);
	File file = Client.client.getFile(match.group(1));
	String[] piecesStr = match.group(2).split("\\s+");

	for (String piece: piecesStr) {
	    String[] tmp = piece.split(":");
	    if (tmp.length != 2) {
		throw new RuntimeException("Wrong group 'pieceIndex:data'");
            }
	    int i = Integer.parseInt(tmp[0]);
	    // check piece index TODO
	    file.addPiece(i, tmp[1].getBytes());
	}
    }

    /* -------------------- SEND RELATED -------------------- */
    private String indexString(int[] index) {
	String out = "";
	for(int i: index) {
	    out += i + " ";
	}
	return out;
    }
}
