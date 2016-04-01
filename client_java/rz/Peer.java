package rz;

import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

public class Peer {
    private Socket socket;
    private boolean connected;

    private String addr;
    private short port;
    
    /**
     * @brief Create Peer for ServerThread
     * Peer only need a socket
     */
    public Peer(Socket socket) {
	this.socket = socket;
	this.connected = true;	
    }

    /**
     * @brief Create a peer from an inline couple: "addr:port"
     * This Peer is in a peer list in a File
     */
    public Peer(String peerSockAddr) {
	String[] addrPort = peerSockAddr.split(":");
	if (addrPort.length != 2) {
	    throw new RuntimeException(
                "Wrong group 'addr:port': \"" + peerSockAddr + '"');
        }
        this.addr = addrPort[0];
        this.port = Short.parseShort(addrPort[1]);
    }

    private void connect() {
        try {
            socket = new Socket(addr, port);
            this.connected = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
	return String.format("[peer: %s]", socket);
    }
    
    /* -------------------- DO -------------------- */

    public void doInterested(File file) {
	sendInterested(file);
	String protocolKey = socket.receiveWord();
	if (protocolKey == "have")
	    receiveHave(false);
	else
	    throw new RuntimeException("Wrong response");
    }

    public void doHave(File file) {
	sendHave(file);
	String protocolKey = socket.receiveWord();
	if (protocolKey == "have")
	    receiveHave(false);
	else
	    throw new RuntimeException("Wrong response");
    }

    public void doGetpieces(File file, int[] index) {
	sendGetpieces(file, index);
	String protocolKey = socket.receiveWord();
	if (protocolKey == "data")
	    receiveData(false);
	else
	    throw new RuntimeException("Wrong response");
    }

    /* -------------------- SEND -------------------- */

    private void send(String format, Object ... args) {
        if (!connected) {
            connect();
        }
        socket.send(format, args);
    }
    
    private void sendInterested(File file) {
	send("interested %s", file.getKey());
    }

    private void sendHave(File file) {
	byte[] bufferMap = file.getBinaryBufferMap();
	send("have %s %s", file.getKey(), "TODO");
    }
    
    private void sendGetpieces(File file, int[] index) {
	send("getpieces %s [%s]", file.getKey(), indexString(index));
    }
    
    private void sendData(File file, int[] index) {
	send("data %s [", file.getKey());
        for (int i = 0; i < index.length; ++i) {
            send(" %d:", index[i]);
            //  socket.sendBytes(file.getPiece(index[i]));
        }
        send(" ]\n");
    }

   

    /* -------------------- SEND RELATED -------------------- */

    private String indexString(int[] index) {
	String out = "";
	for (int i: index) {
	    out += i + " ";
	}
	return out;
    }

    /* -------------------- Server side -------------------- */

    private static final String PREFIX  = "receive";
    private static final int PREFIX_LEN = PREFIX.length();

    private static Map<String, Method> protocol =
	new HashMap<String, Method>();

    private static void putProtocol(String method) {
	try {
	    String key = method.substring(
		PREFIX_LEN, method.length()).toLowerCase();
	    protocol.put(
		key,
		Peer.class.getMethod(method, boolean.class));
	} catch (Exception e) {
	    Log.severe(e.toString());
	}
    }
    
    static {
	Peer.putProtocol("receiveError");
	Peer.putProtocol("receiveHave");
	Peer.putProtocol("receiveInterested");
	Peer.putProtocol("receiveGetpieces");
    }

    public void handleRequest(boolean sendCallBack)
	throws RZNoMatchException, ReflectiveOperationException {
	String protocolKey = socket.receiveWord();
	if (!protocol.containsKey(protocolKey)) {
	    throw new RZNoMatchException(
		"Invalid request with '" + protocolKey + "'");
	}
	
	Method method = protocol.get(protocolKey);
	method.invoke(this, sendCallBack);
    }
    
    /* -------------------- Reception -------------------- */
    
    public void receiveError(boolean sendCallBack) {
	Log.severe("Received error! " + socket);
    }
    
    public void receiveHave(boolean sendCallBack) {
	File file = getFileWithReception();
	byte[] bufferMap = socket.receiveByte((int)file.getLength());
	// TODO update buffer map(?)
	if (sendCallBack)
	    sendHave(file);
    }
    
    public void receiveInterested(boolean sendCallBack) {
	File file = getFileWithReception();
	sendHave(file);
    }
    
    public void receiveGetpieces(boolean sendCallBack) {
	File file = getFileWithReception();
	socket.receiveByte(1); // '['
	ArrayList<Integer> indexList = new ArrayList<Integer>();
	while (true) {
	    String str = socket.receiveWord();
	    if (str == "]")
		break;
	    indexList.add(Integer.parseInt(str));
	}
	int index[] = convertIntegers(indexList);
	sendData(file, index);
    }
				    
    public void receiveData(boolean sendCallBack) {
        File file = getFileWithReception();
	socket.receiveByte(1); // '['
        while (true) {
	    String str = socket.receiveUntil(':', ' ', '\n');
            if (str.compareTo("]") == 0)
		break;
	    int index = Integer.parseInt(str);
            byte[] piece = socket.receiveByte(file.getPieceSize());
            file.addPiece(index, piece);
     	    socket.receiveByte(1); // ' '
	}
    }

    /* ---------------------------------------- */

    private File getFileWithReception() {
	String hash = socket.receiveHash();
	File file = File.getByKey(hash);
	if (file == null) {
	    Log.warning("Asked for unknown file hash '%s'", hash);
	    socket.sendError();
	    throw new RZNoFileException("No file " + hash);
	}
	return file;
    }

    private static int[] convertIntegers(List<Integer> integers) {
	int[] ret = new int[integers.size()];
	for (int i = 0; i < ret.length; i++) {
	    ret[i] = integers.get(i).intValue();
	}
	return ret;
    }
}
