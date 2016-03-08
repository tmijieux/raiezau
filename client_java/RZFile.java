package RZ;

import java.util.*;
import java.io.*;
import java.security.*;

class RZFile {
    private RandomAccessFile file;
    private BufferMap bm;

    private String name;
    private String key;
    private int length;
    private final int pieceSize = 1024;

    private boolean seed;
    private List<Peer> peers;

    /**
     * For uncompleted files
     */
    RZFile(String name, int length, int pieceSize, String key) {
	this.name = name;
	this.length = length;
	this.key = key;
	this.seed = false;
	bm = new BufferMap(length / pieceSize, this);
	peers = new ArrayList<Peer>();
    }
    
    /**
     * For seeded file
     */
    RZFile(String name) throws Exception {
	this.name = name;
	file = new RandomAccessFile(name, "w");
	seed = true;
	key  = MD5Hash();
	length = (int) file.length();
	bm = new BufferMap(length / pieceSize, this); 
	peers = new ArrayList<Peer>();
    }
 
    private String MD5Hash() throws Exception {
	byte[] b = new byte[(int)file.length()];
	file.read(b);
	MessageDigest md = MessageDigest.getInstance("MD5");
	byte[] array = md.digest(b);
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < array.length; ++i) {
	    sb.append(Integer.toHexString(
			  (array[i] & 0xFF) | 0x100).substring(1,3));
	}
	return sb.toString();
    }

    byte[] getByte(int pieceIndex) throws Exception {
	int offset = pieceIndex * pieceSize;
	if (offset > length)
	    throw new Exception("Out of file index");
	if (!bm.isCompleted(pieceIndex))
	    throw new Exception("Request for unpossessed piece");

	byte[] piece = new byte[pieceSize];
	file.read(piece, offset, pieceSize);
	return piece;
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
