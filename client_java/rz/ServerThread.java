package rz;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {

    private static Map<String, Method> protocol = new HashMap<String, Method>();
    private Socket socket;

    private static void putProtocol(String key, String method) {
	try {
	    protocol.put(
                key,
                ServerThread.class.getMethod(method, String.class)
            );
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    static {
	ServerThread.putProtocol("interested", "receiveInterested");
	ServerThread.putProtocol("have",       "receiveHave");
	ServerThread.putProtocol("getpieces",  "receiveGetpieces");
    }

    public ServerThread(Socket socket) {
	this.socket = socket;
    }

    public void run() {
	try {
	    while (true) {
                handleIncomingRequests();
	    }
	} catch (Exception e) {
	    Log.severe("Exception in client reception: %s", e.toString());
	}
    }

    private void handleIncomingRequests() throws ReflectiveOperationException {
	Matcher match;
	try {
	    match = socket.receiveAndGetMatcher(PatternMatcher.DETECT);
	} catch (RZNoMatchException e) {
	    Log.severe(e.toString());
	    return;
	}
	String question = match.group(0);
	String key = match.group(1);

	if (!protocol.containsKey(key)) {
	    Log.warning("Ignoring unknown request: '%s' with key '%s'",
                        question, key);
	    socket.sendError();
	    return;
	}

	Method method = protocol.get(key);
	method.invoke(this, question);
    }
}
