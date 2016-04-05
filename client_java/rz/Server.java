package rz;

import java.net.ServerSocket;

class Server implements Runnable {
    private int port;
    private java.net.ServerSocket listener;

    public Server(int port) {
        this.port = port;
        try {
            try {
                listener = new ServerSocket(port);
            } catch (java.net.BindException e){
                listener = new ServerSocket(0);
                // 0 --> ask the system random port
            }
            listener.setReuseAddress(true);
            this.port = listener.getLocalPort();
            Log.debug("Server port is " + this.port);
        } catch (java.io.IOException e) {
            Log.severe("Cannot start client's server: " + e.toString());
            System.exit(1);
        }
    }

    public Server() {
        this(0); // 0 --> ask the system random port
    }

    public int getPort() {
        return  port;
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
