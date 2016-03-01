package RZ;

import java.util.*;
import java.util.regex.*;

class Peer {
    private PeerSocket socket;

    private final Pattern dataPattern = Pattern.compile(
	"\\s*data\\s*([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");

    Peer(String ip, int port) {
	socket = new PeerSocket(ip, port);
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    static Peer newPeerInline(String peer) throws Exception {
	String[] addrPort = peer.split(":");
	if (addrPort.length != 2) 
	    throw new Exception("Wrong group 'addr:port': \"" + peer + '"');
	return new Peer(addrPort[0], Integer.parseInt(addrPort[1]));
    }

    void connect() throws Exception {
	socket.connect();
    }

    void doInterested(RZFile file) throws Exception {
	socket.send("interested " + file.getKey());
	receiveHave(file);
    }
    void doGetpieces(RZFile file, int[] index) throws Exception {
	socket.send("getpieces %s [%s]", file.getKey(), indexString(index));
	receivePieces(file, index);
    }
    void doHave(RZFile file) throws Exception {
	socket.send("have %s BUFTODO", file.getKey());
	receiveHave(file);
    }

    private void receiveHave(RZFile file) throws Exception {
	String regexp = String.format(
	    "\\s*have\\s*([a-f0-9]*)\\s*(.{%d})\\s*", file.getLength() / 8);
	Pattern havePattern = Pattern.compile(regexp);

	Matcher match = socket.receiveMatcher(havePattern);	
	file.assertIsKey(match.group(1));

	String strBufferMap = match.group(2);
	byte bufferMap[] = strBufferMap.getBytes();
	// TODO: use the bufferMap
    }

    private void receivePieces(RZFile file, int[] index) throws Exception {
	Matcher match = socket.receiveMatcher(dataPattern);
	file.assertIsKey(match.group(1));

	String[] piecesStr = match.group(2).split("\\s+");
	if (piecesStr.length != index.length)
	    throw new Exception("Wrong number of pieces received.");

	for (String piece: piecesStr) {
	    String[] tmp = piece.split(":");
	    if (tmp.length != 2)
		throw new Exception("Wrong group 'pieceIndex:data'");
	    int i = Integer.parseInt(tmp[0]);
	    // check piece index TODO
	    file.addPiece(i, tmp[1].getBytes());
	}
    }
   
    private String indexString(int[] index) {
	String out = "";
	for(int i: index) {
	    out += i + " ";
	}
	return out;
    }

    public String toString() {
	return String.format("[peer: %s]", socket);
    }
}
