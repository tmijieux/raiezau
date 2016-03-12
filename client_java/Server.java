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
            Socket peerSocket;

            try {
                java.net.Socket clientSocket = listener.accept();
                peerSocket = new Socket(clientSocket);
            } catch (Exception e) {
                Log.severe(e.toString());
                continue;
            }

	    Log.info("New client: " + peerSocket);
	    new Thread(new ServerThread(peerSocket)).start();
	}
    }
}
