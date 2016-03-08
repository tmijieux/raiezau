package RZ;

import java.net.*;

class Server implements Runnable {
    private int port;
    private ServerSocket socket;

    Server(int port) throws Exception {
	this.port = port;
	socket = new ServerSocket(port);
    }

    public void run() {
	try {
	    listen();
	}
	catch (Exception e) {
	    Logs.write.severe("Exception in server: %s", e.toString());
	}
    }

    private void listen() throws Exception {
	Logs.write.info("Client server is listening on port:" + this.port);
	while(true) {
	    Socket clientSocket = socket.accept();
	    RZSocket peerSocket = new RZSocket(clientSocket);
	    Logs.write.info("New client: " + peerSocket);
	    new Thread(new ServerThread(peerSocket)).start();
	}
    }
}
