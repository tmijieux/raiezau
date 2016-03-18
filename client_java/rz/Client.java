package rz;

import java.util.*;
import java.io.*;
import java.lang.*;

class Client {
    public static Client client;

    private Tracker tracker;
    private Strategy strategy;
    private Server server;

    public Client(Strategy strategy, Tracker tracker, Server server) {
        this.tracker = tracker;
	this.strategy = strategy;
        this.server = server;
    }

    public void start() {
        new Thread(server).start();
	strategy.share(tracker, server);
    }

    public static void main(String args[]) {
        short clientServerPort = Config.getShort("user-port");
        String trackerAdress = Config.get("tracker-address");
        short trackerPort = Config.getShort("tracker-port");

	Strategy strategy = new StrategyTest();
        Server clientServer = new Server(clientServerPort);

        Tracker tracker = null;
        try {
            tracker = new Tracker(
                Config.get("tracker-address"),
                Config.getShort("tracker-port")
            );
        } catch (Exception e) {
            Log.severe(e.toString());
            System.exit(1);
        }

        Client.client = new Client(
            strategy,
            tracker,
            clientServer
        );
        Client.client.start();
    }
}
