package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class File {
    private static final int pieceSize = Config.getInt("piece-size");
    private static final Map<String, File> files = new HashMap<String, File>();

    private RandomAccessFile file;
    private BufferMap bufferMap;
    private String name;
    private String key;
    private long length; // length in byte
    private boolean seeded;
    private List<Peer> peers;


    public static List<File> getFileList() {
        return new ArrayList<File>(files.values());
    }

    private static void addFileToMap(File file) {
	files.put(file.key, file);
	Log.info("New file %s[%s]", file.name, file.key);
    }

    public static File addCompleteFile(String name) {
        File f = new File(name);
        addFileToMap(f);
        return f;
    }

    public static File addFile(String name, long length, String key) {
        File f = new File(name, length, key);
        addFileToMap(f);
        return f;
    }


    /**
     * For uncompleted files
     */
    private File(String name, long length, String key) {
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
    private File(String name) {
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

    public static File get(String key) {
	return files.get(key);
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

    public boolean isSeeded() {
	return seeded;
    }

    public boolean isKey(String key2) {
	return key.compareTo(key2) == 0;
    }

    public void addPiece(int index, byte[] data) {
        long pos = pieceSize * index;
        file.seek(pos);
        file.write(data);
        bufferMap.addCompletedPart(index);
    }

    @Override
    public String toString() {
	return String.format("[file: %s %s]", name, peers);
    }
}
