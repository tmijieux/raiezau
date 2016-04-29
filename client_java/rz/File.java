package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class File implements Serializable {
    private java.io.File jFile;
    
    private transient RandomAccessFile file;

    private BufferMap bufferMap;
    private String name;
    private String key;
    private long length; // length in byte
    private boolean seeded;
    private int pieceSize;
    private transient  Map<String, FilePeer> peers;
    
    private File(String name, boolean seeded) {
	this.name = name;
	this.seeded = seeded;
	this.peers = new HashMap<String, FilePeer>();

	String filePath = getFilePath();
	this.jFile = new java.io.File(filePath);
        try {
            this.file = new RandomAccessFile(jFile, "rw");
	} catch (IOException e) {
            throw new RuntimeException(e.toString() + filePath);
        }
    }

    /**
     * For uncompleted files
     */
    public File(String name, long length, int pieceSize, String key) {
	this(name, false);
	this.length = length;
	this.key = key;
        this.pieceSize = pieceSize;
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
            this.pieceSize = Config.getInt("piece-size");
        } catch (IOException e) {
            throw new RuntimeException(e.toString() + name);
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
            filePath = dir +'/'+ name;
        }
	return filePath;
    }

    public String announceSeed() {
	return name + " " + length + " " + pieceSize + " " + key;
    }
    
    /* ------------- Piece read / write -------------*/

    public byte[] readPiece(int pieceIndex) throws RZNoPartException {
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
    
    public void addPeer(FilePeer peer) {
	if (!peers.containsKey(peer.toString()))
	    peers.put(peer.toString(), peer);
    }

    public List<FilePeer> getPeerList() {
	return new ArrayList<FilePeer>(peers.values());
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
