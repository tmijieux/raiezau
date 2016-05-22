package rz;

import java.io.*;
import java.net.*;

class Server implements Runnable {
    private int port;
    private ServerSocket listener;

    public Server(int port) {
        this.port = port;
        try {
            try {
                listener = new ServerSocket(port);
            } catch (BindException e){
                listener = new ServerSocket(0);
                // 0 --> ask the system random port
            }
            listener.setReuseAddress(true);
            this.port = listener.getLocalPort();
            Log.debug("Server port is " + this.port);
        } catch (IOException e) {
            Log.severe("Cannot start client's server: " + e.toString());
            System.exit(1);
        }
    }

    public Server() {
        this(0); // 0 --> ask the system random port
    }

    public int getPort() {
        return port;
    }

    public void run() {
	acceptConnections();
    }

    private void acceptConnections() {
	Log.info("Client server is listening on port:" + this.port);
	while (true) {
            try {
                java.net.Socket clientSocket = listener.accept();
                ServerPeer peer = new ServerPeer(new Socket(clientSocket));
                Log.info("New client: " + peer);
                new Thread(new ServerThread(peer)).start();
            } catch (IOException e) {
                Log.severe(e.toString());
            }
	}
    }
}
