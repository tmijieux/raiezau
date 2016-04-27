package rz;

import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

public class FilePeer extends Peer {
    private BufferMap bm;
    private File file;
    
    /**
     * @brief Create a peer from an inline couple: "ip:port"
     * This Peer is in a peer list in a File
     */
    public FilePeer(String peerSockAddr, File file) {
        super(peerSockAddr);
        this.file = file;
        this.bm = new BufferMap(file);
    }

    public void doInterested(File file) {
	sendInterested(file);
	String protocolKey = socket.receiveWord();
	if (protocolKey.compareTo("have") == 0)
	    receiveHave();
	else
	    throw new RZInvalidResponseException("Wrong response in interested");
    }
    
    public void doHave(File file) {
	sendHave(file);
	String protocolKey = socket.receiveWord();
	if (protocolKey.compareTo("have") == 0) {
	    byte[] bufferMap = receiveHave();
            this.bm = BufferMap.fromByteArray(bufferMap);
	} else {
	    throw new RZInvalidResponseException("Wrong response in have");
        }
    }
    
    public void doGetpieces(File file, int[] index) {
	sendGetpieces(file, index);
	String protocolKey = socket.receiveWord();
	if (protocolKey.compareTo("data") == 0)
	    receiveData();
	else
	    throw new RZInvalidResponseException("Wrong response in getpieces");
    }

    public BufferMap getBufferMap() {
        return bm;
    }

    public int getPieceCount() {
	return file.getPieceCount();
    }
}
