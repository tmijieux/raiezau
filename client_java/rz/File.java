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
    private List<FilePeer> peers;
    
    private File(String name, boolean seeded) {
	this.name = name;
	this.seeded = seeded;
	this.peers = new ArrayList<FilePeer>();

	String filePath = getFilePath();
	this.jFile = new java.io.File(filePath);
        try {
            this.file = new RandomAccessFile(jFile, "rw");
	} catch (IOException e) {
            throw new RuntimeException("File exception: " + filePath + e.toString());
        }
    }

    /**
     * For uncompleted files
     */
    public File(String name, long length, String key) {
	this(name, false);
	this.length = length;
	this.key = key;
	this.bufferMap = new BufferMap(this);
    }

    /**
     * For seeded file
     */
    public File(String name) {
	this(name, true);
        try {
            this.key    = this.MD5Hash();
	    this.length = this.file.length();
        } catch (IOException e) {
            throw new RuntimeException("File exception: " + name + e.toString());
        }
	this.bufferMap = new BufferMap(this);
    }

    private String MD5Hash() throws IOException {
        return MD5.hash(new FileInputStream(jFile));
    }

    private String getFilePath() {
        String filePath = name;
        if (name.charAt(0) != '/') {
            String dir = Config.get("completed-files-directory");
            filePath =  dir +'/'+ name;
        }
	return filePath;
    }

    public void addPeer(FilePeer peer) {
	peers.add(peer);
    }

    public String announceSeed() {
	return name + " " + length + " " +
            Config.getInt("piece-size") + " " + key;
    }
    
    /* ------------- Piece read / write -------------*/

    public byte[] readPiece(int pieceIndex) {
	long startPos = pieceSize * pieceIndex;
	byte[] data = new byte[pieceSize];
	if (!bufferMap.isCompleted(pieceIndex))
	    throw new RZNoPartException("Request for unpossessed piece");
	try{
	    file.seek(startPos);
	    file.read(data);
	} catch (IOException e) {
            throw new RuntimeException(e);
        }
	return data;
    }
    
    public void writePiece(int index, byte[] data) {
        long pos = pieceSize * index;
        try {
            file.seek(pos);
            file.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	bufferMap.addCompletedPart(index);
    }

    /* ------------------- Get ----------------- */
    
    public boolean isSeeded() {
	return seeded;
    }
    
    public boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }
    
    public BufferMap getBufferMap() {
        return bufferMap;
    }

    public int getPieceCount() {
        return (int) (length + (pieceSize-1)) / pieceSize;
    }

    public int getPieceSize() {
	return pieceSize;
    }
    
    public List<FilePeer> getPeerList() {
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
