package rz;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.lang.reflect.*;

class ServerThread implements Runnable {
    private Peer peer;

    public ServerThread(Peer peer) {
	this.peer = peer;
    }

    public void run() {
	try {
	    while (true) {
                peer.handleRequest();
	    }
	} catch (Exception e) {
	    Log.severe("Exception in client reception: %s", 
		       e.toString());
	}
    }
}
