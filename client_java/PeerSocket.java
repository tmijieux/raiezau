package RZ;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

class PeerSocket {
    private int port;
    private String ip;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    PeerSocket(String ip, int port) {
	this.ip = ip;
	this.port = port;
    }

    void connect() throws Exception {
	socket = new Socket(ip, port);
        reader = new BufferedReader(
	    new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(
	    new OutputStreamWriter(socket.getOutputStream()), true);
    }

    void send(String text) throws Exception {
	writer.println(text);
    }

    void send(String format, Object... args) throws Exception {
	writer.printf(format, args);
	writer.println("");
    }

    String receive() throws Exception {
	String response = reader.readLine();
	if (response == null)
	    throw new Exception("No response.");
	return response;
    }

    public String toString() {
	return String.format("[%s:%d]", ip, port);
    }
}
