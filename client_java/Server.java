package RZ;

import java.net.*;

class Server {
    private ServerSocket socket;

    Server(int port) throws Exception {
	socket = new ServerSocket(port);
    }

    void listen() throws Exception {
	while(true) {
	    Socket clientSocket = socket.accept();
	    PeerSocket peerSocket = new PeerSocket(clientSocket);
	    new Thread(new ServerThread(peerSocket)).start();
	}
    }
}
