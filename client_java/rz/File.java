package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class File implements Serializable {
    private static final int pieceSize = Config.getInt("piece-size");

    private java.io.File jFile;
    private RandomAccessFile file;

    private BufferMap bufferMap;
    private String name;
    private String key;
    private long length; // length in byte
    private boolean seeded;
    private List<Peer> peers;
    
    /**
     * For uncompleted files
     */
    public File(String name, long length, String key) {
	this.name = name;
	this.length = length;
	this.key = key;
	this.seeded = false;
        this.jFile = new java.io.File(name);
	this.bufferMap = new BufferMap(this);
	this.peers = new ArrayList<Peer>();
	Log.info(this.toString());
    }

    /**
     * For seeded file
     */
    public File(String name) {
	this.name = name;
	this.peers = new ArrayList<Peer>();
        String filePath = name;
        if (name.charAt(0) != '/') {
            String dir = Config.get("completed-files-directory");
            filePath =  dir +'/'+ name;
        }
	this.jFile = new java.io.File(filePath);
        try {
            file = new RandomAccessFile(jFile, "rw");
            key  = this.MD5Hash();
            length = file.length();
        } catch (IOException e) {
            throw new RuntimeException("File exception: "+filePath);
        }
	this.seeded = true;
	this.bufferMap = new BufferMap(this);
	Log.info(this.toString());
    }

    private String MD5Hash() throws IOException {
        return MD5.hash(new FileInputStream(jFile));
    }

    public byte[] getByte(int pieceIndex) {
	int offset = pieceIndex * pieceSize;
	if (offset > length) {
	    throw new RuntimeException("Out of file index");
        }
	if (!bufferMap.isCompleted(pieceIndex)) {
	    throw new RuntimeException("Request for unpossessed piece");
        }
	byte[] piece = new byte[pieceSize];
        try {
            file.read(piece, offset, pieceSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	return piece;
    }

    public void addPeer(Peer peer) {
	peers.add(peer);
    }

    public String announceSeed() {
	return name + " " + length + " " +
            Config.getInt("piece-size") + " " + key;
    }
    
    public byte[] getPiece(int pieceIndex){
	long startPos = pieceSize * pieceIndex;
	byte[] data = new byte[pieceSize];
	try{
	    if (bufferMap.isCompleted(pieceIndex)) {
		file.seek(startPos);
		file.readFully(data, (pieceSize * pieceIndex), pieceSize);
	    }
	    return data;
	} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean isSeeded() {
	return seeded;
    }
    
    public boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }
    
    public void addPiece(int index, byte[] data) {
        long pos = pieceSize * index;
        try {
            file.seek(pos);
            file.write(data);
            bufferMap.addCompletedPart(index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------- Get Local ----------------- */
    
    public BufferMap getLocalBufferMap() {
        return bufferMap;
    }
    
    public long getLength() {
	return length;
    }
    
    public int getPieceSize() {
	return pieceSize;
    }
    
    public List<Peer> getPeerList() {
	return peers;
    }
    
    public String getKey() {
	return key;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
	return String.format("File: '%s' '%s' '%s'", 
			     name, peers, bufferMap);
    }
}
