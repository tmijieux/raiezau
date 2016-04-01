package rz;

import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

class Socket {
    private java.net.Socket sock;
    private short port;
    private String ip;

    private BufferedReader from;
    private PrintWriter to;

    public short getPort() {
        return (short) sock.getPort();
    }

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

    public void sendByte(byte[] bytes) {
	char[] t = ArrayByteToChar(bytes);
	to.write(t);
	to.flush();
    }

    public void send(String text) {
	to.write(text);
	to.flush();
	Log.info("Send %s '%s'", this, text);
    }

    public void send(String format, Object... args) {
	send(String.format(format, args));
    }

    public void sendBytes(byte[] data) {
        //        sock.write(data);
    }
    
    public void sendError() {
	send("error");
    }
    
    private byte[] ArrayCharToByte(char[] t) {
	byte b[] = new byte[t.length];
	for (int i = 0; i < t.length; i++) {
	    b[i] = (byte) t[i];
	}
	return b;
    }

    private char[] ArrayByteToChar(byte[] t) {
	char b[] = new char[t.length];
	for (int i = 0; i < t.length; i++) {
	    b[i] = (char) t[i];
	}
	return b;
    }

    public byte[] receiveByte(int length) {
	char t[] = new char[length];
        try {
	    from.read(t, 0, length);
        } catch (Exception e) {
            Log.debug(e.toString());
        }
	return ArrayCharToByte(t);
    }

    /**
     * Get the string received until encountering one of the char in parameters
     * The char will not be in the string returned and will be "eaten"
     */
    public String receiveUntil(char ... c) {
	StringBuilder sb = new StringBuilder();
	while_loop:
	while(true) {
	    char tmp[] = new char[1];
	    try {
		from.read(tmp, 0, 1);
	    } catch (Exception e) {
		Log.debug(e.toString());
	    }
	    for (int i = 0; i < c.length; i++)
		if (tmp[0] == c[i])
		    break while_loop;
	    sb.append(tmp[0]);
	}
	return sb.toString();
    }
    
    public String receiveWord() {
	return receiveUntil(' ', '\n');
    }

    public String receiveHash() {
	// maybe should assert or throw exception
	return receiveWord();
    }

    public void receiveBlank() {
	// maybe should assert or throw exception
	receiveByte(1);
    }

    public String receiveLine() {
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

    public Matcher receiveAndGetMatcher(PatternMatcher patMatcher) {
	String str = receiveLine();
        return patMatcher.getMatcher(str);
    }

    @Override
    public String toString() {
	return String.format("[%s:%d]", ip, port);
    }
}
