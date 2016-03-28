package rz;

import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

public class Peer {
    private String addr;
    private short port;
    
    private Socket socket;
    private boolean connected;
    
    private BufferMap bufferMap;
    private File file;
    
    public Peer(File file, String ip, short port, int i) {
        this.addr = ip;
        this.port = port;
        this.connected = false;
        this.bufferMap = new BufferMap(file);
        this.file = file;
        
        /* maybe we should connect to the remote peer
           only when we try to send a request */
        connect();
    }

    /**
     * @brief Create Peer for ServerThread
     */
    public Peer(Socket socket) {
	this.socket = socket;
	this.connected = true;	
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    public Peer(String peerSockAddr) {
	String[] addrPort = peerSockAddr.split(":");
	if (addrPort.length != 2) {
	    throw new RuntimeException(
                "Wrong group 'addr:port': \"" + peerSockAddr + '"');
        }
        
        this.addr = addrPort[0];
        this.port = Short.parseShort(addrPort[0]);
        this.connect();
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
    
    /* -------------------- SEND -------------------- */
    private void send(String format, Object ... args) {
        if (!connected) {
            connect();
        }
        socket.send(format, args);
    }
    
    public void sendInterested(File file) {
	this.send("interested %s", file.getKey());
    }

    public void sendHave(File file) {
	this.send("have %s %s", file.getKey(), "TODO");
    }

    public void sendGetpieces(File file, int[] index) {
	this.send("getpieces %s [%s]", file.getKey(), indexString(index));
    }

    public void sendData(File file, int[] index) {
	this.send("data %s [%s]", file.getKey(), "TODO");
    }

    /* -------------------- Reception -------------------- */
    public void parseInterested(String question) {
	Matcher match = PatternMatcher.INTERESTED.getMatcher(question);
	File file = File.getByKey(match.group(1));
	sendHave(file);
    }

    public void parseHave(String question) {
        /* this function does not work with a binary buffer map */
	Matcher match = PatternMatcher.HAVE.getMatcher(question);
        String key = match.group(1);
        String strBufferMap = match.group(2);
        
        if (!(key.compareTo(this.file.getKey()) == 0)) {
            throw new RuntimeException("bad key");
        }
        
	byte bufferMap[] = strBufferMap.getBytes();
        
        for (int i = 0; i < bufferMap.length; ++i) {
            byte b = bufferMap[i];
            if (b != 0) {
                this.bufferMap.addCompletedPart(i);
            }
        }
    }

    public void parseGetpieces(String question) {
	Matcher match = PatternMatcher.GETPIECES.getMatcher(question);
	String key = match.group(1);
	String[] strIndex = match.group(2).split("\\s+");
	int[] index = Arrays.stream(strIndex)
            .mapToInt(Integer::parseInt)
            .toArray();
	File file = File.getByKey(match.group(1));
        sendData(file, index);
    }

    public void parseData(String question) {
        /* this function does not work with a binary buffer map */
	Matcher match = PatternMatcher.DATA.getMatcher(question);
	File file = File.getByKey(match.group(1));
	String[] piecesStr = match.group(2).split("\\s+");

	for (String piece: piecesStr) {
	    String[] tmp = piece.split(":");
	    if (tmp.length != 2) {
		throw new RuntimeException("Wrong group 'pieceIndex:data'");
            }
	    int i = Integer.parseInt(tmp[0]);
	    file.addPiece(i, tmp[1].getBytes());
	}
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

    private static final String PREFIX = "receive";
    private static final int PREFIX_LEN = PREFIX.length();

    private static Map<String, Method> protocol =
	new HashMap<String, Method>();

    private static void putProtocol(String method) {
	try {
	    String key = method.substring(
		PREFIX_LEN, method.length()).toLowerCase();
	    protocol.put(
		key,
		Peer.class.getMethod(method));
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    static {
	Peer.putProtocol("receiveError");
	Peer.putProtocol("receiveHave");
	Peer.putProtocol("receiveInterested");
	Peer.putProtocol("receiveGetpieces");
    }

    public void handleRequest()
	throws RZNoMatchException, ReflectiveOperationException {
	String protocolKey = socket.receiveWord();
	if (!protocol.containsKey(protocolKey)) {
	    throw new RZNoMatchException(
		"Invalid request with '" + protocolKey + "'");
	}
	
	Method method = protocol.get(protocolKey);
	method.invoke(this);
    }

    /* -------------------- Reception -------------------- */

    public void receiveError() {
	Log.severe("Received error! " + socket);
    }

    public void receiveHave() {
	File file = getFileWithReception();
	byte[] bufferMap = socket.receiveByte((int)file.getLength());
	// TODO update buffer map(?)
	sendHave(file);
    }

    public void receiveInterested() {
	File file = getFileWithReception();
	sendHave(file);
    }

    public void receiveGetpieces() {
	File file = getFileWithReception();
	socket.receiveByte(1); // '['
	ArrayList<Integer> indexList = new ArrayList<Integer>();
	while(true) {
	    String str = socket.receiveWord();
	    if (str == "]")
		break;
	    indexList.add(Integer.parseInt(str));
	}
	int index[] = convertIntegers(indexList);
	sendData(file, index);
    }

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
