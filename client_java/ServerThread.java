package RZ;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {
    private static Map<String, Method> protocol = 
	new HashMap<String, Method>();

    private PeerSocket socket;

    ServerThread(PeerSocket socket) {
	this.socket = socket;
    }

    public void run() {
	try {
	    listen();
	}
	catch (Exception e) {
	    Logs.write.severe("Exception in thread: %s", e.toString());
	}
    }

    private final Pattern detectPattern = Pattern.compile(
	"\\s*([a-z]*) .*");

    private void listen() throws Exception {
	Matcher match = socket.receiveMatcher(detectPattern);
	String question = match.group(1);
	// switch with a Map
    }

    private final Pattern interestedPattern = Pattern.compile(
	"\\s*interested\\s+([a-f0-9]*)\\s*");
    private final Pattern getpiecesPattern = Pattern.compile(
	"\\s*getpieces\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");
    private final Pattern havePattern = Pattern.compile(
	"\\s*have\\s+([a-f0-9]*)\\s*(.*)\\s*");
    
}
