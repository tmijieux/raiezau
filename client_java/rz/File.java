package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class File implements Serializable {

    private static final int pieceSize = Config.getInt("piece-size");
    private static final Map<String, File> filesByKey;

    private java.io.File jFile;
    private RandomAccessFile file;

    private BufferMap bufferMap;
    private String name;
    private String key;
    private long length; // length in byte
    private boolean seeded;
    private List<Peer> peers;

    static {
        filesByKey = new HashMap<String, File>();
        String dirname = Config.get("completed-files-directory");
        java.io.File fileDir = new java.io.File(dirname);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        loadCompleteFileFromDirectory(fileDir);
    }

    public static List<File> getFileList() {
        return new ArrayList<File>(filesByKey.values());
    }

    private static void loadIncompleteFileFromDirectory(java.io.File folder) {
	if (!folder.isDirectory()){
	    throw new IllegalArgumentException();
        }

	for (java.io.File fileEntry : folder.listFiles()) {
	    if (fileEntry.isDirectory()) {
		loadIncompleteFileFromDirectory(fileEntry);
	    } else {
		restoreFileState(fileEntry);
	    }
	}
    }

    public static void loadCompleteFileFromDirectory(java.io.File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException();
        }
        Log.debug("Entering directory " + folder);

        for (java.io.File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                loadCompleteFileFromDirectory(fileEntry);
            } else {
                String name = fileEntry.getName();
                Log.info("loading file " + name);
                addCompleteFile(name);
            }
        }

        Log.debug("Leaving directory " + folder);
    }

    private static File insertFile(File newFile) {
        File f = filesByKey.get(newFile.key);
        if (f != null) {
            Log.warning(
                "file " + newFile.name + " with key "+
                newFile.key + " is already present in "+
                "the file store and it is know as '"+f.name+"'.");
            newFile = null;
            return f;
        }
        filesByKey.put(newFile.key, newFile);
        System.err.println("size: " + filesByKey.size());
        return newFile;
    }

    public static File addCompleteFile(String name) {
        File newFile = new File(name);
        return insertFile(newFile);
    }

    public static File addFile(String name, long length, String key) {
        File newFile = new File(name, length, key);
        return insertFile(newFile);
    }

    /**
     * For uncompleted files
     */
    private File(String name, long length, String key) {
	this.key = key;
	this.seeded = false;
        jFile = new java.io.File(name);
	bufferMap = new BufferMap(this);
	peers = new ArrayList<Peer>();
    }

    /**
     * For seeded file
     */
    private File(String name) {
	this.name = name;
        String filePath = name;
        if (name.charAt(0) != '/') {
            String dir = Config.get("completed-files-directory");
            filePath =  dir +'/'+ name;
        }
        try {
            jFile = new java.io.File(filePath);
            file = new RandomAccessFile(jFile, "rw");
            seeded = true;
            key  = this.MD5Hash();
            length = file.length();
            bufferMap = new BufferMap(this);
            peers = new ArrayList<Peer>();
        } catch (IOException e) {
            throw new RuntimeException("File exception: "+filePath);
        }
    }

    public static File getByKey(String key) {
	return filesByKey.get(key);
    }

    public static List<File> getByName(String name) {
        List<File> fileList = new ArrayList<File>();
        return fileList;
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

    public String getKey() {
	return key;
    }

    public long getLength() {
	return length;
    }

    public int getPieceSize() {
	return pieceSize;
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
	}catch (Exception e) {
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

    public long size() {
        return this.length;
    }

    public BufferMap getLocalBufferMap() {
        return bufferMap;
    }

    public byte[] getBinaryBufferMap(){
	return bufferMap.toByteArray();
    }

    @Override
    public String toString() {
	return name+": "+ peers;
    }

    public static void saveFileState(File file) {
	try {
	    FileOutputStream saveFile =
                new FileOutputStream("./" + file.name +".ser");
	    ObjectOutputStream out = new ObjectOutputStream(saveFile);
	    out.writeObject(file);
	    out.close();
	    saveFile.close();
	} catch (IOException e){
	}
    }

    public static File restoreFileState(java.io.File file) {
	File f = null;
	try {
	    FileInputStream saveFile = new FileInputStream(file);
	    ObjectInputStream in = new ObjectInputStream(saveFile);
	    f = (File) in.readObject();
	    in.close();
	    saveFile.close();

	} catch (FileNotFoundException e) {

	} catch (IOException | ClassNotFoundException e) {
	}
	return f;
    }

    public String getName() {
        return name;
    }
}
