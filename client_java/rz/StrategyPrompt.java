package rz;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

class StrategyPrompt implements Strategy {
    private static String PROMPT = "> ";
    private static Map<String, Method> cmds =
	new HashMap<String, Method>();

    static {
	// general
	putCmd("quit", "cmdQuit");
	putCmd("q",    "cmdQuit");
	putCmd("exit", "cmdQuit");
	putCmd("help", "cmdHelp");
	putCmd("h",    "cmdHelp");
	putCmd("ls",   "cmdLS");

	// tracker
	putCmd("update", "cmdUpdate");
	putCmd("up",     "cmdUpdate");
	putCmd("getfile", "cmdGetfile");
	putCmd("getf",    "cmdGetfile");

	// peer
	putCmd("interested", "cmdInterested");
	putCmd("in",         "cmdInterested");
	putCmd("have", "cmdHave");
	putCmd("hv",   "cmdHave");
	putCmd("getpieces", "cmdGetpieces");
	putCmd("getp",      "cmdGetpieces");
    }

    private static void putCmd(String key, String methodName) {
	try {
	    Method method = StrategyPrompt.class.getMethod(
		methodName, String[].class);
	    cmds.put(key, method);
	} catch (ReflectiveOperationException e) {
	    Log.severe(e.toString());
	}	
    }

    private Tracker tracker;
    private Console console;

    public StrategyPrompt(Tracker tracker) {
	this.tracker = tracker;
	this.console = System.console();
    }

    @Override
    public void share() {
	console.printf("Starting prompt: waiting for input.\n");
	while(true) {
	    String[] args = askInput();
	    analyseCmd(args);
	}
    }

    private void analyseCmd(String[] args) {
	if (!cmds.containsKey(args[0])) {
	    cmdError(args);
	} else {
	    Method method = cmds.get(args[0]);
	    try {
		method.invoke(this, (Object) args);
	    } catch (ReflectiveOperationException | 
		     IllegalArgumentException e) {
		console.printf("Internal error!\n");
		Log.severe(e.toString() + method.toString());
	    }
	}
    }

    public void LSFiles(List<File> files) throws RZNoFileException {
	if (files.size() == 0)
	    throw new RZNoFileException();
	console.printf("File list:\n");
	for(int i = 0; i < files.size(); i++) {
	    File file = files.get(i);
	    console.printf("[%d]: %s '%s'\n", 
			   i, file.getName(), file.getKey());
	}
    }

    /* ------------------ ASK ------------------*/

    private String[] askInput(String prompt) {
	String command = null;
	while(true) {
	    console.printf("> " + prompt);
	    command = console.readLine();
	    if (!PatternMatcher.EMPTY.matches(command))
		break;
	}
	return command.split("\\s+");	
    }

    private String[] askInput() {
	return askInput("");
    }

    private int askIndex(int min, int max) {
	int index = -1;
	while(true) {
	    String[] args = askInput("select index: ");
	    index = Integer.parseInt(args[0]);
	    if (min < index || index <= max)
		break;
	    console.printf("Wrong index!\n");
	}
	return index;
    }

    private File askFile() throws RZNoFileException {
	List<File> files = FileManager.getFileList();
	LSFiles(files);
	int index = askIndex(0, files.size());
	return files.get(index);
    }

    private FilePeer askFilePeer(File file) throws RZNoPeerException {
	List<FilePeer> peers = file.getPeerList();
	if (peers.size() == 0)
	    throw new RZNoPeerException();
	console.printf("Peer list for file %s:\n", file.getName());
	for(int i = 0; i < peers.size(); i++) {
	    FilePeer peer = peers.get(i);
	    console.printf("[%d]: %s '%s'\n", i, peer.toString());
	}
	int index = askIndex(0, peers.size());
	return peers.get(index);	
    }

    private int[] askIndex(BufferMap bm) {
	console.printf("BufferMap: \n");
	console.printf("%s\n", bm.toString());
	List<Integer> index = new ArrayList<Integer>();
	String[] str = askInput("select parts: ");
	for (String i : str) {
	    int part = Integer.parseInt(i);
	    if (bm.isCompleted(part))
		index.add(part);
	    else
		console.printf("Part %d ignored.\n", part);
	}
	return Utils.convertIntegers(index);
    }

    /* ------------------ CMD ------------------*/

    public void cmdError(String[] args) {
	console.printf("Unknown command '%s'\n", args[0]);
    }

    public void cmdQuit(String[] args) {
	console.printf("Bye-bye...\n");
	System.exit(0);
    }

    public void cmdHelp(String[] args) {
	// TODO
	console.printf("%s\n", cmds.toString());
    }

    public void cmdLS(String[] args) {
	try {
	    List<File> files = FileManager.getFileList();
	    LSFiles(files);
	} catch (RZNoFileException e) {
	    console.printf("No files :(\n");
	}
    }

    public void cmdUpdate(String[] args) {
	List<File> files = FileManager.getFileList();
	tracker.doUpdate(files);
    }

    public void cmdGetfile(String[] args) {
	try {
	    File file = askFile();
	    tracker.doGetfile(file);
	} catch (RZNoFileException e) {
	    console.printf("No files :(\n");
	}
    }

    public void cmdLook(String[] args) {
	LookRequest lr = new LookRequest();
	// TODO
    }

    public void cmdInterested(String[] args) {
	try {
	    File file = askFile();
	    FilePeer peer = askFilePeer(file);
	    peer.doInterested(file);
	} catch (RZNoFileException e) {
	    console.printf("No files :(\n");
	} catch (RZNoPeerException e) {
	    console.printf("No peers for this file :(\n");
	}
    }

    public void cmdHave(String[] args) {
	try {
	    File file = askFile();
	    FilePeer peer = askFilePeer(file);
	    peer.doHave(file);
	} catch (RZNoFileException e) {
	    console.printf("No files :(\n");
	} catch (RZNoPeerException e) {
	    console.printf("No peers for this file :(\n");
	}
    }

    public void cmdGetpieces(String[] args){
	try {
	    File file = askFile();
	    FilePeer peer = askFilePeer(file);
	    int[] index = askIndex(peer.getBufferMap());
	    peer.doGetpieces(file, index);
	} catch (RZNoFileException e) {
	    console.printf("No files :(\n");
	} catch (RZNoPeerException e) {
	    console.printf("No peers for this file :(\n");
	}	
    }
}
