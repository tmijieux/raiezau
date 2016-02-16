package RZ;

import java.util.*;

class Peer {
    private PeerSocket socket;

    private final Pattern havePattern = Pattern.compile(
	"\\s*have\\s*([a-f0-9]*)\\s*");
    private final Pattern dataPattern = Pattern.compile(
	"\\s*data\\s*([a-f0-9]*)\\s*");

    Peer(String ip, int port) {
	socket = new PeerSocket(ip, port);
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    static Peer newPeerInline(String peer) throws Exception {
	String[] addrPort = peer.split(":");
	if (addrPort.length != 2) 
	    throw new Exception("Wrong group 'addr:port'.");
	return new Peer(addrPort[0], Integer.parseInt(addrPort[1]));
    }

    void doInterested(RZFile file) throws Exception {
	socket.send("interested " + file.getKey());
	receiveHave(file);
    }

    void doGetpieces(RZFile file, int[] index) throws Exception {
	socket.send("getpieces %s [%s]", file.getKey(), indexString(index));
	receivePieces(file);
    }

    void doHave(RZFile file) throws Exception {
	socket.send("have %s BUFTODO", file.getKey());
	receiveHave(file);
    }

    private void receiveHave(RZFile file) throws Exception {
	String response = socket.receive();
	// TODO
    }

    private void receivePieces(RZFile file) throws Exception {
    	String response = socket.receive();
	// TODO
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
