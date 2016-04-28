package rz;

import java.util.*;
import java.lang.reflect.*;

class ServerPeer extends Peer {
    private static Map<String, Method> protocol =
	new HashMap<String, Method>();

    static {
	putProtocol("receiveError");
	putProtocol("receiveHave");
	putProtocol("receiveInterested");
	putProtocol("receiveGetpieces");
    }
    
    private static void putProtocol(String method) {
	try {
	    String key = method.substring(
		"receive".length(), method.length()).toLowerCase();
	    protocol.put(key, Peer.class.getMethod(method));
	} catch (ReflectiveOperationException e) {
	    Log.severe(e.toString());
	}
    }

    public ServerPeer(Socket s) {
        super(s);
    }
    
    @Override
    protected boolean sendCallBack() {
        return true;
    }

    public void handleRequest()
	throws ReflectiveOperationException, RZNoMatchException {
	String protocolKey = socket.receiveWord();
        
	if (!protocol.containsKey(protocolKey)) {
	    throw new RZNoMatchException(
		"Invalid request with '" + protocolKey + "'");
	}

	Method method = protocol.get(protocolKey);
	method.invoke(this);
    }
}
