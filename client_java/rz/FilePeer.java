package rz;

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
	handleHaveReception();
    }
    
    public void doHave(File file) {
	sendHave(file);
	handleHaveReception();
    }
    
    public void doGetpieces(File file, int[] index) {
	sendGetpieces(file, index);
	try {
	    checkProtocolKey("data");
	    receiveData();
	} catch (RZInvalidResponseException | RZNoFileException e) {
	    Log.warning(e.toString());
	    socket.sendError();
	}
    }

    private void checkProtocolKey(String expected) 
	throws RZInvalidResponseException {
	String protocolKey = socket.receiveWord();
	if (protocolKey.compareTo(expected) != 0)
	    throw new RZInvalidResponseException(
		"Wrong protocol key: " + expected + "!="
		+ protocolKey);
    }

    private void handleHaveReception() {
	try {
	    checkProtocolKey("have");
	    byte[] bufferMap = receiveHave();
	    this.bm = BufferMap.fromByteArray(bufferMap);
	} catch (RZInvalidResponseException | RZNoFileException e) {
	    Log.warning(e.toString());
	    socket.sendError();
	}
    }

    public BufferMap getBufferMap() {
        return bm;
    }

    public int getPieceCount() {
	return file.getPieceCount();
    }
}
