package rz;

import java.util.*;
import java.io.*;
import java.lang.*;

class Client {
    public static Client me;

    private short port;
    private Map<String, File> files;
    private TrackerSocket tracker;
    private Strategy strategy;

    public Client(Strategy strategy) {
	port = Config.getShort("user-port");
        try {
            tracker = new TrackerSocket(
                Config.get("tracker-address"),
                Config.getShort("tracker-port")
            );
        } catch (Exception e) {
            Log.severe(e.toString());
            System.exit(1);
        }

	files = new HashMap<String, File>();
	this.strategy = strategy;
	new Thread(new Server(this.port)).start();
    }

    public File getFile(String key) {
	return files.get(key);
    }

    public void start() {
	strategy.share(files, tracker, port);
    }

    public static void main(String args[]) {
	Strategy strategy = new StrategyTest();
	me = new Client(strategy);
	me.start();
    }
}
