package RZ;

import java.util.*;
import java.io.*;
import java.lang.*;

class Client {
    static Client me;

    private int port;
    private Map<String, RZFile> files;
    private TrackerSocket tracker;
    private Strategy strategy;

    Client(Strategy strategy) throws Exception {
	port = Config.getInt("user-port");
	tracker = new TrackerSocket(Config.get("tracker-address"),
				    Config.getInt("tracker-port"));
	files = new HashMap<String, RZFile>();
	this.strategy = strategy;
	new Thread(new Server(this.port)).start();
    }

    RZFile getFile(String key) {
	return files.get(key);
    }

    void start() throws Exception {
	strategy.share(files, tracker, port);
    }

    public static void main(String args[]) throws Exception {
	Strategy strategy = new StrategyTest();
	me = new Client(strategy);
	me.start();
    }
}
