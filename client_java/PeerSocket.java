package RZ;

import java.util.*;
import java.util.regex.*;

class PeerSocket extends RZSocket {
    private final Pattern interestedPattern = Pattern.compile(
	"\\s*interested\\s+([a-f0-9]*)\\s*");
    private final Pattern havePattern = Pattern.compile(
	"\\s*have\\s+([a-f0-9]*)\\s+(.*)\\s*");
    private final Pattern getpiecesPattern = Pattern.compile(
	"\\s*getpieces\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");
    private final Pattern dataPattern = Pattern.compile(
	"\\s*data\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");

    PeerSocket(String ip, int port) {
	super(ip, port);
    }

    /* -------------------- SEND -------------------- */

    void sendInterested(RZFile file) throws Exception {
	send("interested %s", file.getKey());
    }
    
    void sendHave(RZFile file) throws Exception {
	send("have %s %s", file.getKey(), "TODO");
    }
    
    void sendGetpieces(RZFile file, int[] index) throws Exception {
	send("getpieces %s [%s]", file.getKey(), indexString(index));
    }
    
    void sendData(RZFile file, int[] index) throws Exception {
	send("data %s [%s]", file.getKey(), "TODO");
    }

    /* -------------------- Reception -------------------- */

    void parseInterested(String question) throws Exception {
	Matcher match = getMatcherOnMatch(interestedPattern, question);

	RZFile file = Client.me.getFile(match.group(1));
	sendHave(file);
    }

    RZFile parseHave(String question) throws Exception {
	Matcher match = getMatcherOnMatch(havePattern, question);
	
	String strBufferMap = match.group(2);
	byte bufferMap[] = strBufferMap.getBytes();
	// TODO: update the buffer
	
	return Client.me.getFile(match.group(1));
    }

    void parseGetpieces(String question) throws Exception {
	Matcher match = getMatcherOnMatch(getpiecesPattern, question);
	
	String key = match.group(1);
	String[] strIndex = match.group(2).split("\\s+");
	int[] index = Arrays.stream(strIndex).mapToInt(Integer::parseInt).toArray();

	RZFile file = Client.me.getFile(match.group(1));
        sendData(file, index);
    }

    void parseData(String question) throws Exception {
	Matcher match = getMatcherOnMatch(dataPattern, question);

	RZFile file = Client.me.getFile(match.group(1));

	String[] piecesStr = match.group(2).split("\\s+");
	for (String piece: piecesStr) {
	    String[] tmp = piece.split(":");
	    if (tmp.length != 2)
		throw new Exception("Wrong group 'pieceIndex:data'");
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

    /* -------------------- PARSE RELATED -------------------- */

    private Matcher getMatcherOnMatch(Pattern pattern, String question)
	throws Exception {
	Matcher match = pattern.matcher(question);
	if (!match.matches())
	    throw new Exception("Client question does not match pattern.");
	return match;
    }
}
