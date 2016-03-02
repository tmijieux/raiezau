package RZ;

import java.util.*;
import java.io.*;
import java.lang.*;

class Client {
    private int port;

    private List<RZFile> files;
    private Tracker tracker;
    private Strategy strategy;

    Client(Strategy strategy) throws Exception {
	port = Config.cfg.getInt("user-port");
	tracker = new Tracker(Config.cfg.get("tracker-address"), 
			      Config.cfg.getInt("tracker-port"));
	files = new ArrayList<RZFile>();
	this.strategy = strategy;
	new Thread(new Server(this.port)).start();
    }

    void start() throws Exception {
	strategy.share(files, tracker, port);
    }

    public static void main(String args[]) throws Exception {
	Strategy strategy = new StrategyTest();
	Client me = new Client(strategy);
	//me.start();
    }
}
