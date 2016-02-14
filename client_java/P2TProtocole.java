package RZ;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

class P2TProtocole {
    
    private int port = 8080;
    private String ip = "localhost";

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    P2TProtocole() throws Exception {
        socket = new Socket(ip, port);
        reader = new BufferedReader(
	    new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(
	    new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private void seed(ArrayList<File> files) {
        writer.print("seed [");
	for(File file : files) {
	    if(file.isSeeded()) {
		writer.print(file.announceSeed());
		writer.print(" ");
	    }
	}
	writer.print("] ");
    }
    
    private void leech(ArrayList<File> files) {
        writer.print("leech [");
	for(File file : files) {
	    if(!file.isSeeded()) {
		writer.print(file.announceLeech());
		writer.print(" ");
	    }
	}	
        writer.print("] ");
    }

    boolean announce(ArrayList<File> files) throws Exception {
        writer.print("announce listen " + port + " ");
        seed(files);
        leech(files);
        writer.println("");

        String response = reader.readLine();

        if (response.compareTo("ok") != 0) {
            return false;
        }
        return true;
    }

    boolean getfile(File file) {
	writer.print("getfile " + file.announceLeech());
	return true;
    }
}
