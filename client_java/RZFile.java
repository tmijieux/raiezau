package RZ;

import java.util.*;

class RZFile {
    private String name;
    private String key;
    private int length;
    private final int pieceSize = 1024;

    private boolean seed;
    private List<Peer> peers;

    RZFile(String name, int length, int pieceSize, String key) {
	this(name, length, key, false);
    }

    RZFile(String name, int length, String key, boolean seed) {
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
	return name + " " + length + " " + pieceSize + " " + key;
    }

    String getKey() {
	return key;
    }
    
    boolean isSeeded() {
	return seed;
    }

    boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }

    public String toString() {
	return String.format("[file: %s %s]", name, peers);
    }
}
