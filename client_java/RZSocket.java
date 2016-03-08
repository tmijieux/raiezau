package RZ;

import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

class RZSocket {
    private int port;
    private String ip;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    RZSocket(String ip, int port) {
	this.ip = ip;
	this.port = port;
    }

    RZSocket(Socket sock) throws Exception {
	socket = sock;
	port = socket.getPort();
	ip =   socket.getInetAddress().getHostAddress();
        reader = new BufferedReader(
	    new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(
	    new OutputStreamWriter(socket.getOutputStream()), true);
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
	Logs.write.info("Send %s '%s'", this, text);
    }
    void send(String format, Object... args) throws Exception {
	send(String.format(format, args));
    }
    void sendError() throws Exception {
	send("error");
    }

    String receive() throws Exception {
	String response = reader.readLine();
	if (response == null)
	    throw new RZNoResponseException("No response from " + this);
	Logs.write.info("Receive %s '%s'", this, response);
	return response;
    }

    Matcher receiveMatcher(Pattern pattern) throws Exception {
	String s = receive();
	Matcher match = pattern.matcher(s);
	if (!match.matches()) {
	    sendError();
	    throw new RZNoMatchException(
		"Response '" + s + "' does not match pattern.");
	}
	return match;
    }

    public String toString() {
	return String.format("[%s:%d]", ip, port);
    }
}
