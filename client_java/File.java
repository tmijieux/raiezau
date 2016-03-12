package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class File {
    private RandomAccessFile file;
    private BufferMap bufferMap;

    private String name;
    private String key;
    private long length; // length in byte
    private int pieceSize = Config.getInt("piece-size");

    private boolean seeded;
    private List<Peer> peers;

    /**
     * For uncompleted files
     */
    public File(String name, int length, String key) {
	this.name = name;
	this.length = length;
	this.key = key;
	this.seeded = false;
	bufferMap = new BufferMap((int) length / pieceSize, this);
	peers = new ArrayList<Peer>();
    }

    /**
     * For seeded file
     */
    public File(String name) {
	this.name = name;
        String filePath = Config.get("file-dir") + name;
        try {
            file = new RandomAccessFile(filePath, "rw");
            seeded = true;
            key  = MD5Hash();
            length = file.length();
            bufferMap = new BufferMap((int)length / pieceSize, this);
            peers = new ArrayList<Peer>();
        } catch (IOException e) {
            throw new RuntimeException("File exception: "+filePath);
        }
    }

    private String MD5Hash() throws IOException {
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] b = new byte[(int)file.length()];
            byte[] array = md5.digest(b);

            file.read(b);
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString(
                              (array[i] & 0xFF) | 0x100).substring(1,3));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public byte[] getByte(int pieceIndex) {
	int offset = pieceIndex * pieceSize;
	if (offset > length)
	    throw new RuntimeException("Out of file index");
	if (!bufferMap.isCompleted(pieceIndex))
	    throw new RuntimeException("Request for unpossessed piece");
	byte[] piece = new byte[pieceSize];
        try {
            file.read(piece, offset, pieceSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

	return piece;
    }

    public void putInMap(Map<String, File> files) {
	files.put(this.key, this);
	Log.info("New file %s[%s]", name, key);
    }

    public void addPeer(Peer peer) {
	peers.add(peer);
    }

    public String announceSeed() {
	return name + " " + length + " " + Config.getInt("piece-size") + " " + key;
    }

    public String getKey() {
	return key;
    }

    public long getLength() {
	return length;
    }

    public boolean isSeeded() {
	return seeded;
    }

    public boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }

    public void addPiece(int index, byte[] data) {
	// TODO:
	// write in file
	// update buffermap
    }

    public String toString() {
	return String.format("[file: %s %s]", name, peers);
    }
}
