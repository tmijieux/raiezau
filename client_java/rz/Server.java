package rz;

import java.net.ServerSocket;

class Server implements Runnable {
    private short port;
    private java.net.ServerSocket listener;

    public Server(short port) {
	this.port = port;
        try {
            listener = new ServerSocket(port);
        } catch (java.io.IOException e) {
            Log.severe("Cannot start client's server: " + e.toString());
            System.exit(1);
        }
    }

    public short getPort() {
        return port;
    }

    public void run() {
	try {
            this.acceptConnections();
	} catch (Exception e) {
	    Log.severe("Exception in server: %s", e.toString());
            System.exit(1);
	}
    }

    private void acceptConnections() {
	Log.info("Client server is listening on port:" + this.port);
	while (true) {
            Peer peer;

            try {
                java.net.Socket clientSocket = listener.accept();
                peer = new Peer(new Socket(clientSocket));
            } catch (Exception e) {
                Log.severe(e.toString());
                continue;
            }

	    Log.info("New client: " + peer);
	    new Thread(new ServerThread(peer)).start();
	}
    }
}
