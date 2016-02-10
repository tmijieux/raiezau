import java.io.*;
import java.net.*;

class Client {
    public static void main(String args[]) throws Exception {
        P2TProtocole p2t = new P2TProtocole();
        boolean r = p2t.announce();
        System.out.println("announce: " + r);
    }
}
