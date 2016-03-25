package rz;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {
    private Socket socket;

    private static final int KEY_LEN = 4;
    private static final String PREFIX = "receive";
    private static final int PREFIX_LEN = PREFIX.length();

    private static Map<String, Method> protocol =
	new HashMap<String, Method>();

    private static void putProtocol(String method) {
	try {
	    String key = method.substring
		(PREFIX_LEN, PREFIX_LEN + KEY_LEN).toLowerCase();
	    protocol.put(
		key,
		ServerThread.class.getMethod(method, String.class));
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    static {
	ServerThread.putProtocol("receiveHave");
	ServerThread.putProtocol("receiveInterested");
	ServerThread.putProtocol("receiveGetpieces");
    }

    public ServerThread(Socket socket) {
	this.socket = socket;
    }

    public void run() {
	try {
	    while (true) {
                handleRequest();
	    }
	} catch (Exception e) {
	    Log.severe("Exception in client reception: %s", 
		       e.toString());
	}
    }

    public void handleRequest()
	throws RZNoMatchException, ReflectiveOperationException {
	byte[] keyBytes = socket.receiveByte(KEY_LEN);
	String key = new String(keyBytes);
	if (!protocol.containsKey(key)) {
	    throw new RZNoMatchException(
		"Invalid request with '" + key + "'");
	}
	
	Method method = protocol.get(key);
	method.invoke(this, key);
    }

    private void check(String name) throws RZNoMatchException {
	String expected = name.substring(KEY_LEN, name.length());
	if (expected.length() == 0)
	    return;
	String received = new String(
	    socket.receiveByte(expected.length()));
	if (expected.compareTo(received) != 0) {
	    throw new RZNoMatchException( // TODO failing 
		"Check failed received '" + expected + "'");
	}
    }
    
    /* -------------------- Reception -------------------- */

    public void receiveHave(String key) 
	throws RZNoMatchException {
	check("have");
	// TODO
    }

    public void receiveInterested(String key) 
	throws RZNoMatchException {
	check("interested");
	// TODO call parseInterested
    }

    public void receiveGetpieces(String key) 
	throws RZNoMatchException {
	check("getpieces");
	// TODO call parseGetpieces
    }
    
    /* -------------------- Send -------------------- */

}
