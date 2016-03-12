package rz;

import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

class Socket {
    java.net.Socket sock;
    private short port;
    private String ip;

    //    private java.net.Socket socket;
    private BufferedReader from;
    private PrintWriter to;

    private void setupStreams() throws IOException {
        InputStreamReader reader;
        reader = new InputStreamReader(sock.getInputStream());
        from = new BufferedReader(reader);

        OutputStreamWriter writer;
        writer = new OutputStreamWriter(sock.getOutputStream());
        to = new PrintWriter(writer, true);
    }

    public Socket(String ip, short port)
      throws java.net.UnknownHostException, IOException {
        this.ip = ip;
        this.port = port;

        sock = new java.net.Socket(ip, port);
        setupStreams();
    }

    public Socket(java.net.Socket sock)
      throws java.net.UnknownHostException, IOException {
        this.ip = ip;
        this.port = port;
        this.sock = sock;
        setupStreams();
    }

    public void send(String text) {
	to.println(text);
	Log.info("Send %s '%s'", this, text);
    }

    public void send(String format, Object... args) {
	send(String.format(format, args));
    }

    public void sendError() {
	send("error");
    }

    public String receive() {
        String response = null;
        try {
            response = from.readLine();
        } catch (Exception e) {
            Log.debug(e.toString());
        }

        if (response == null) {
	    throw new RZNoResponseException("No response from " + this);
        }

	Log.info("Receive %s '%s'", this, response);
	return response;
    }

    private void dumpString(String a) {
        System.err.println("length: " + a.length());
        for (int i = 0; i < a.length(); ++i) {
            System.err.print(
                    String.format("%02X ", (int) a.charAt(i)));
        }
        System.err.println("");
    }

    public Matcher receiveMatcher(Pattern pattern) {
	String s = receive();
	Matcher match = pattern.matcher(s);

	if (!match.matches()) {
	    sendError();
	    dumpString(s);
	    throw new RZNoMatchException(
                "Response '" + s + "' does not match pattern '"+ pattern + "'.");
	}
	return match;
    }

    @Override
    public String toString() {
	return String.format("[%s:%d]", ip, port);
    }
}
