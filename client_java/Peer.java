package RZ;

class Peer {
    private PeerSocket socket;

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
}
