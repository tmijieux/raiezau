package RZ;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {
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

    private PeerSocket socket;

    ServerThread(PeerSocket socket) {
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

    private final Pattern detectPattern = Pattern.compile(
	"\\s*([a-z]*)\\s.*");

    private void listen() throws Exception {
	Matcher match = socket.receiveMatcher(detectPattern);
	String question = match.group(0);
	String key = match.group(1);

	if (!protocol.containsValue(key)) {
	    Logs.write.warning("Ignoring ununderstood request: '%s' with key '%s'", 
			       question, key);
	    return; // TODO look wrong
	}
	Method method = protocol.get(key);
	method.invoke(this, question);
    }

    private final Pattern interestedPattern = Pattern.compile(
	"\\s*interested\\s+([a-f0-9]*)\\s*");
    private final Pattern getpiecesPattern = Pattern.compile(
	"\\s*getpieces\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");
    private final Pattern havePattern = Pattern.compile(
	"\\s*have\\s+([a-f0-9]*)\\s*(.*)\\s*");
    
    public void receiveInterested(String question) {
	Logs.write.info("interested called!");
    }
    public void receiveHave(String question) {
	Logs.write.info("have called!");
    }
    public void receiveGetpieces(String question) {
	Logs.write.info("getpieces called!");
    }
}
