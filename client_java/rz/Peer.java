package rz;

import java.security.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

public abstract class Peer {
    protected Socket socket;
    private boolean connected;

    private String ip;
    private int port;

    protected boolean sendCallBack() {
        return false;
    }
    
    /**
     * @brief Create Peer for ServerThread
     * Peer only need a socket
     */
    protected Peer(Socket socket) {
	this.socket = socket;
	this.connected = true;
    }

    /**
     * @brief Create a peer from an inline couple: "ip:port"
     * This Peer is in a peer list in a File
     */
    public Peer(String peerSockAddr) {
	String[] ipPort = peerSockAddr.split(":");
	if (ipPort.length != 2) {
	    throw new InvalidParameterException(
                "Wrong group 'addr:port': \"" + peerSockAddr + '"');
        }
        this.ip   = ipPort[0];
        this.port = Integer.parseInt(ipPort[1]);
    }

    private void connect() {
        try {
            socket = new Socket(ip, port);
            this.connected = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
	if (socket != null)
	    return String.format("[peer: %s]", socket);
	else
	    return String.format("[peer: %s:%d]", ip, port);
    }
    
    /* -------------------- SEND -------------------- */

    private void send(String format, Object ... args) {
        if (!connected) {
            connect();
        }
        socket.send(format, args);
    }
    
    protected void sendInterested(File file) {
	// The space at the end IS important
	send("interested %s ", file.getKey());
    }

    protected void sendHave(File file) {
	BufferMap bm = file.getBufferMap();
	Log.info(bm.toString());
	send("have %s ", file.getKey());
	socket.sendByte(bm.toByteArray());
    }
    
    protected void sendGetpieces(File file, int[] index) {
	// the space at the end IS important
	send("getpieces %s [%s] ", file.getKey(), indexString(index));
    }
    
    private void sendData(File file, int[] index) {
	send("data %s [", file.getKey());
        for (int i = 0; i < index.length; ++i) {
            send("%d:", index[i]);
	    Log.info("Will send byte " + i);
	    Log.info("part is " + index[i]);
	    try {
		socket.sendByte(file.readPiece(index[i]));
	    } catch (RZNoPartException e) {
		socket.sendError();
	    }
        }
	// the space at the end IS important
        send("] ");
    }

    /* -------------------- SEND RELATED -------------------- */

    private String indexString(int[] index) {
	String out = "";
	for (int i: index) {
	    out += i + " ";
	}
	return out;
    }

    /* -------------------- Reception -------------------- */
    
    public void receiveError() {
	Log.severe("Received error! " + socket);
    }
    
    public byte[] receiveHave() throws RZNoFileException {
	File file = getFileWithReception();
	byte[] bufferMap = socket.receiveByte(file.getPieceCount());
	if (sendCallBack())
	    sendHave(file);
        return bufferMap;
    }
    
    public void receiveInterested() throws RZNoFileException {
	File file = getFileWithReception();
	sendHave(file);
    }
    
    public void receiveGetpieces() throws RZNoFileException {
	File file = getFileWithReception();
	socket.receiveByte(1); // '['
	ArrayList<Integer> indexList = new ArrayList<Integer>();
	while (true) {
	    String str = socket.receiveWord();
	    if (str.compareTo("]") == 0)
		break;
	    indexList.add(Integer.parseInt(str));
	}
	int index[] = Utils.convertIntegers(indexList);
	sendData(file, index);
    }
				    
    public void receiveData() throws RZNoFileException {
        File file = getFileWithReception();
	socket.receiveByte(1); // '['
        while (true) {
	    String str = socket.receiveUntil(':', ' ', '\n');
            if (str.compareTo("]") == 0)
		break;
	    int index = Integer.parseInt(str);
            byte[] piece = socket.receiveByte(file.getPieceSize());
            file.writePiece(index, piece);
	}
    }

    /* ---------------------------------------- */

    private File getFileWithReception() throws RZNoFileException {
	String hash = socket.receiveHash();
	File file = FileManager.getByKey(hash);
	if (file == null) {
	    Log.warning("Asked for unknown file hash '%s'", hash);
	    socket.sendError();
	    throw new RZNoFileException("No file " + hash);
	}
	return file;
    }
}
