package RZ;

import java.util.ArrayList;

class Client {
    public static void main(String args[]) throws Exception {
	ArrayList<File> files = new ArrayList<File>();
	files.add(new File("fifi", 2, "x000", true));
	files.add(new File("floa", 8, "x890", false));

        P2TProtocole p2t = new P2TProtocole();
        boolean r = p2t.announce(files);
        System.out.println("announce: " + r);
    }
}
