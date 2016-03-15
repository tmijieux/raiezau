package rz;

import java.util.*;
import java.util.regex.*;

public class Peer {
    private PeerSocket socket;
    private Map<String, File> files;

    public Peer(String ip, short port) {
        try {
            socket = new PeerSocket(ip, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    public Peer(String peer) {
	String[] addrPort = peer.split(":");
	if (addrPort.length != 2) {
	    throw new RuntimeException(
                "Wrong group 'addr:port': \"" + peer + '"');
        }
        try {
            socket = new PeerSocket(
                addrPort[0],
                Short.parseShort(addrPort[1])
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
	return String.format("[peer: %s]", socket);
    }
}
