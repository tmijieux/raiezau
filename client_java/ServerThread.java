package RZ;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {
    private final Pattern detectPattern = Pattern.compile(
	"\\s*([a-z]*)\\s.*");

    private static Map<String, Method> protocol = new HashMap<String, Method>();

    private static void putProtocol(String key, String method) {
	try {
	    protocol.put(key, ServerThread.class.getMethod(method, String.class));
	}
	catch (Exception e) {
	    System.out.println(e);
	}
    }

    static {
	ServerThread.putProtocol("interested", "receiveInterested");
	ServerThread.putProtocol("have",       "receiveHave");
	ServerThread.putProtocol("getpieces",  "receiveGetpieces");
    }

    private RZSocket socket;

    ServerThread(RZSocket socket) {
	this.socket = socket;
    }

    public void run() {
	try {
	    while(true) {
		listen();
	    }
	}
	catch (Exception e) {
	    Logs.write.severe("Exception in client reception: %s", e.toString());
	}
    }

    private void listen() throws Exception {
	Matcher match;
	try {
	    match = socket.receiveMatcher(detectPattern);
	}
	catch (RZNoMatchException e) {
	    Logs.write.severe(e.toString());
	    return;
	}
	String question = match.group(0);
	String key = match.group(1);

	if (!protocol.containsKey(key)) {
	    Logs.write.warning("Ignoring unknown request: '%s' with key '%s'", 
			       question, key);
	    socket.sendError();
	    return;
	}
	Method method = protocol.get(key);
	method.invoke(this, question);
    }
    
}
