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

    void putInMap(Map<String, RZFile> files) {
	files.put(this.key, this);
    }

    void addPeer(Peer peer) {
	peers.add(peer);
    }
    
    void peerConnect(int peerIndex) throws Exception {
	peers.get(peerIndex).connect();
    }
    void peerDoInterested(int peerIndex) throws Exception {
	peers.get(peerIndex).doInterested(this);
    }
    void peerDoGetpieces(int peerIndex, int[] index) throws Exception {
	peers.get(peerIndex).doGetpieces(this, index);
    }
    void peerDoHave(int peerIndex) throws Exception {
	peers.get(peerIndex).doHave(this);
    }

    String announceSeed() {
	return name + " " + length + " " + pieceSize + " " + key;
    }

    String getKey() {
	return key;
    }
    
    int getLength() {
	return length;
    }
    
    boolean isSeeded() {
	return seed;
    }

    boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }

    void assertIsKey(String key2) throws Exception {
	if (!isKey(key2))
	    throw new Exception("Wrong key.");
    }

    void addPiece(int index, byte[] data) {
	// TODO:
	// write in file
	// update buffermap
    }

    public String toString() {
	return String.format("[file: %s %s]", name, peers);
    }
}
