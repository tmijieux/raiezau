package RZ;

import java.util.List;
import java.util.ArrayList;

class File {
    private String name;
    private String key;
    private int length;
    private int pieceSize = 1024;

    private boolean seed;
    private List<Peer> peers;

    File(String name, int length, String key, boolean seed) {
	this.name   = name;
	this.key    = key;
	this.length = length;
	this.seed   = seed;
	peers = new ArrayList<Peer>();
    }

    void addPeer(Peer peer) {
	peers.add(peer);
    }

    String announceSeed() {
	return (name + " " + length + " " +
		pieceSize + " " + key);
    }

    String announceLeech() {
	return key;
    }
    
    boolean isSeeded() {
	return seed;
    }

    boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }

    public String toString() {
	return String.format("[file: %s]", name);
    }
}
