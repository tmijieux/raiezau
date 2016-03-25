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
    
    public Peer(File file, String ip, short port) {
        this.addr = ip;
        this.port = port;
        this.connected = false;
        this.bufferMap = new BufferMap(file);
        this.file = file;
        
        /* maybe we should connect to the remote peer
           only when we try to send a request */
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(addr, port);
            this.connected = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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


    // receive
    private static final int KEY_LEN = 4;
    private static final String PREFIX = "receive";
    private static final int PREFIX_LEN = PREFIX.length();

    private static Map<byte[], Method> protocol =
	new HashMap<byte[], Method>();

    private static void putProtocol(String method) {
	try {
	    byte[] key = method.substring
		(PREFIX_LEN, PREFIX_LEN + KEY_LEN).getBytes();
	    protocol.put(
		key,
		ServerThread.class.getMethod(method, String.class));
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    static {
	Peer.putProtocol("receiveHave");
	Peer.putProtocol("receiveInterested");
	Peer.putProtocol("receiveGetpieces");
    }

    public void receive() throws ReflectiveOperationException {
	byte[] key = socket.receiveByte(KEY_LEN);
	if (!protocol.containsKey(key)) {
	    Log.warning("Ignoring request begining with '%s'", key);
	    socket.sendError();
	    return;
	}
	
	Method method = protocol.get(key);
	try {
	    method.invoke(this, key);
	} catch (RZNoMatchException e) {
	    Log.severe(e.toString());
	}
    }

    private void check(String name) throws RZNoMatchException {
	byte[] expected = name.substring
	    (KEY_LEN, name.length()).getBytes();
	if (expected.length == 0)
	    return;
	byte[] received = socket.receiveByte(expected.length);
	if (Arrays.equals(expected, received))
	    throw new RZNoMatchException
		("No match for '"+expected+"' '"+received+"'");
    }
    
    public void receiveHave(byte[] key) throws RZNoMatchException {
	check("have");
	// TODO
    }

    public void receiveInterested(byte[] key) throws RZNoMatchException {
	check("interested");
	// TODO call parseInterested
    }

    public void receiveGetpieces(byte[] key) throws RZNoMatchException {
	check("getpieces");
	// TODO call parseGetpieces
    }
}
