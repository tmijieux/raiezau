package RZ;

import java.util.*;
import java.util.regex.*;

class Peer {
    private PeerSocket socket;
    private Map<String, RZFile> files;

    Peer(String ip, int port) {
	socket = new PeerSocket(ip, port);
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    Peer(String peer) throws Exception {
	String[] addrPort = peer.split(":");
	if (addrPort.length != 2)
	    throw new Exception("Wrong group 'addr:port': \"" + peer + '"');
	socket = new PeerSocket(addrPort[0], Integer.parseInt(addrPort[1]));
    }

    void connect() throws Exception {
	socket.connect();
    }

    public String toString() {
	return String.format("[peer: %s]", socket);
    }
}
