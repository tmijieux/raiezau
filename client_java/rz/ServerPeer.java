package rz;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerPeer extends Peer
{
    private static final String PREFIX  = "receive";
    private static final int PREFIX_LEN = PREFIX.length();
    private static Map<String, Method> protocol =
	new HashMap<String, Method>();

    static {
	putProtocol("receiveError");
	putProtocol("receiveHave");
	putProtocol("receiveInterested");
	putProtocol("receiveGetpieces");
    }
    
    public ServerPeer(Socket s)
    {
        super(s);
    }
    
    @Override
    protected boolean sendCallBack()
    {
        return true;
    }

    private static void putProtocol(String method) {
	try {
	    String key = method.substring(
		PREFIX_LEN, method.length()).toLowerCase();
	    protocol.put(key, Peer.class.getMethod(method, boolean.class));
	} catch (Exception e) {
	    Log.severe(e.toString());
	}
    }

    public void handleRequest()
	throws RZNoMatchException, ReflectiveOperationException
    {
	String protocolKey = socket.receiveWord();
        
	if (!protocol.containsKey(protocolKey)) {
	    throw new RZNoMatchException(
		"Invalid request with '" + protocolKey + "'");
	}

	Method method = protocol.get(protocolKey);
	method.invoke(this);
    }
}
