package rz;

import java.lang.reflect.*;

class ServerThread implements Runnable {
    private ServerPeer peer;

    public ServerThread(ServerPeer peer) {
	this.peer = peer;
    }

    public void run() {
	try {
	    while (true) {
                peer.handleRequest();
	    }
	} catch (ReflectiveOperationException e) {
	    Log.severe("Exception in client: %s", e.toString());
	}
    }
}
