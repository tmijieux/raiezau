package rz;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;

class Client {
    /* Static & main */

    public static Client client;
    private static Map<String, Constructor<Strategy>> strats = 
	new HashMap<String, Constructor<Strategy>>();

    /**
     * klass must be a class implementing Strategy
     */
    private static void putConstructor(String key, Class klass) {
	try {
	    // a check could be done
	    @SuppressWarnings("unchecked")
		Class<Strategy> strategy = klass;
	    strats.put(key, strategy.getConstructor(Tracker.class));
	} catch (ReflectiveOperationException e) {
	    Log.severe(e.toString());
	}
    }

    static {
	putConstructor("sleepy",   StrategySleepy.class);
	putConstructor("test",     StrategyTest.class);
	putConstructor("advanced", StrategyAdvanced.class);
	putConstructor("TestPartialFile", StrategyTestPartialFile.class);
	putConstructor("TestPartialFile2", StrategyTestPartialFile2.class);
    }

    /**
     * First argument is the path to the config.ini file wanted.
     */
    private static void ApplyArgs(String args[]) {
	if (args.length >= 1)
	    Config.init(args[0]);
	else
	    Config.init();
    }

    private static Tracker getTracker() {
        Tracker tracker = null;
        try {
	    tracker = new Tracker(
                Config.get("tracker-address"),
                Config.getShort("tracker-port")
            );
        } catch (IOException e) {
            Log.abort("Error in Tracker creation: " + e.toString());
        }
	return tracker;
    }

    private static Server getServer() {
        int clientServerPort = Config.getShort("user-port");
	Server clientServer = new Server(clientServerPort);
	return clientServer;
    }

    private static Strategy getStrategy(Tracker tracker) {
	Constructor<Strategy> constr = 
	    strats.get(Config.get("strategy"));
	Strategy strategy = null;
	try {
	    strategy = constr.newInstance(tracker);
        } catch (ReflectiveOperationException e) {
            Log.abort("Error in Strategy creation: " + e.toString());
        }
	return strategy;
    }

    public static void main(String args[]) {
	ApplyArgs(args);
	
	Tracker tracker = getTracker();
	Server clientServer = getServer();
	Strategy strategy = getStrategy(tracker);

        Client.client = new Client(strategy, tracker, clientServer);
        Client.client.start();
    }

    /* Client */

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
        List<File> fileList = FileManager.getFileList();
	tracker.doAnnounce(fileList, server.getPort());
	strategy.share();
    }
}
