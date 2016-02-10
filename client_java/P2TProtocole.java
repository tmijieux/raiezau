import java.io.*;
import java.net.*;

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

    private void seed(String filename, int length, String key) {
        writer.print("seed ");
        writer.print(filename + " " + length + " " + key + " ");
    }

    private void leech(String key) {
        writer.print("seed ");
        writer.print(key + " ");
    }

    boolean announce() throws Exception {
        writer.print("announce listen " + port + " ");
        seed("file1", 16, "key1");
        leech("key2");
        writer.println("");

        String response = reader.readLine();

        if (response.compareTo("OK") != 0) {
            return false;
        }
        return true;
    }
}
