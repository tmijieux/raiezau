package rz;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

class StrategyPrompt implements Strategy {
    private static Map<String, Method> cmds =
	new HashMap<String, Method>();
    private static Map<String, Method> lookCmds =
	new HashMap<String, Method>();

    static {
	// general
	putPromptCmd("quit", "cmdQuit");
	putPromptCmd("q",    "cmdQuit");
	putPromptCmd("exit", "cmdQuit");
	putPromptCmd("stop", "cmdQuit");
	putPromptCmd("help", "cmdHelp");
	putPromptCmd("h",    "cmdHelp");
	putPromptCmd("ls",   "cmdLS");

	// tracker
	putPromptCmd("update",  "cmdUpdate");
	putPromptCmd("up",      "cmdUpdate");
	putPromptCmd("getfile", "cmdGetfile");
	putPromptCmd("getf",    "cmdGetfile");
	putPromptCmd("gf",      "cmdGetfile");
	putPromptCmd("look",    "cmdLook");
	putPromptCmd("lk",      "cmdLook");

	// peer
	putPromptCmd("interested", "cmdInterested");
	putPromptCmd("in",         "cmdInterested");
	putPromptCmd("have", "cmdHave");
	putPromptCmd("hv",   "cmdHave");
	putPromptCmd("getpieces", "cmdGetpieces");
	putPromptCmd("getp",      "cmdGetpieces");
	putPromptCmd("gp",        "cmdGetpieces");

	// look
	putLookCmd("size", "lookCmdSize");
	putLookCmd("key",  "lookCmdKey");
	putLookCmd("file", "lookCmdFilename");
	putLookCmd("name", "lookCmdFilename");
	putLookCmd("done", "cmdQuit");
	putLookCmd("quit", "cmdQuit");
	putLookCmd("stop", "cmdQuit");
	putLookCmd("q",    "cmdQuit");
	putLookCmd("exit", "cmdQuit");
    }

    private static void putPromptCmd(String key, String methodName) {
	putCmd(cmds, key, methodName);
    }

    private static void putLookCmd(String key, String methodName) {
	putCmd(lookCmds, key, methodName);
    }

    private static void putCmd(Map<String, Method> map, String key,
			       String methodName) {
	try {
	    Method method = StrategyPrompt.class.getMethod(
		methodName, String[].class);
	    map.put(key, method);
	} catch (ReflectiveOperationException e) {
	    Log.severe(e.toString());
	}	
    }

    private Tracker tracker;
    private Console console;
    private List<File> files;
    private LookRequest lr = null;
    private boolean loop;
    
    public StrategyPrompt(Tracker tracker) {
	this.tracker = tracker;
	this.console = System.console();
	this.files   = FileManager.getFileList();
	this.loop    = true;
    }

    @Override
    public void share() {
	console.printf("Starting prompt: waiting for input.\n");
	listenCmds(cmds);
	console.printf("Bye-bye...\n");
    }

    private void listenCmds(Map<String, Method> map) {
	try {
	    while(loop) {
		String[] args = askInput();
		invokeCmd(map, args);
	    } 
	} catch (EOFException e) {
	    // End of input, exiting loop
	    console.printf("\n");
	}
    }

    private void updateFiles() {
	this.files = FileManager.getFileList();
    }

    private void invokeCmd(Map<String, Method> map, String[] args) {
	if (!map.containsKey(args[0])) {
	    cmdError(args);
	} else {
	    Method method = map.get(args[0]);
	    try {
		method.invoke(this, (Object) args);
	    } catch (ReflectiveOperationException | 
		     IllegalArgumentException e) {
		console.printf("Internal error!\n");
		Log.severe("%s\nin %s\n", 
			   e.toString(), method.toString());
	    }
	}
    }

    private File getFile(String[] args) throws EOFException {
	if (args.length >= 2) {
	    int index = Integer.parseInt(args[1]);
	    return files.get(index);
	} else {
	    return askFile();
	}
    }

    private void LSFiles() {
	updateFiles();
	console.printf("File list:\n");
	for(int i = 0; i < files.size(); i++) {
	    File file = files.get(i);
	    StringColorer colorer;
	    if (file.isSeeded())
		colorer = StringColorer.GREEN;
	    else
		colorer = StringColorer.RED;
	    String number = colorer.format("[%d]", i);
	    console.printf(
		"%s:\t%s\t%dp x%dB\t'%s'\n", number, file.getName(), 
		file.getPieceCount(), file.getPieceSize(), 
		file.getKey());
	}
    }

    private List<FilePeer> LSPeers(File file) {
	List<FilePeer> peers = file.getPeerList();
	console.printf("Peer list for %s:\n", file.getName());
	for(int i = 0; i < peers.size(); i++) {
	    FilePeer peer = peers.get(i);
	    console.printf("[%d]: %s\n", i, peer.toString());
	}
	return peers;
    }

    private void LSBufferMap(BufferMap bm) {
	console.printf("BufferMap:\n");
	console.printf("%s\n", bm.toString());
    }

    /* ------------------ ASK ------------------*/

    private String[] askInput(String prompt) throws EOFException {
	String command = null;
	while(true) {
	    console.printf("> " + prompt);
	    command = console.readLine();
	    if (command == null)
		throw new EOFException();
	    if (!PatternMatcher.EMPTY.matches(command))
		break;
	}
	return command.split("\\s+");	
    }

    private String[] askInput() throws EOFException {
	return askInput("");
    }

    private int askIndex(int min, int max) throws EOFException {
	int index = -1;
	String[] args = askInput("select index: ");
	index = Integer.parseInt(args[0]);
	return index;
    }

    private File askFile() throws EOFException {
	LSFiles();
	int index = askIndex(0, files.size());
	return files.get(index);
    }

    private FilePeer askFilePeer(File file) throws EOFException {
	List<FilePeer> peers = LSPeers(file);
	int index = askIndex(0, peers.size());
	return peers.get(index);	
    }

    private int[] askParts(BufferMap bm) throws EOFException {
	LSBufferMap(bm);
	List<Integer> index = new ArrayList<Integer>();
	String[] str = askInput("select parts: ");
	for (String i : str) {
	    int part = Integer.parseInt(i);
	    index.add(part);
	}
	return Utils.convertIntegers(index);
    }

    /* ------------------ CMD ------------------*/

    public void cmdError(String[] args) {
	console.printf("Unknown command '%s'\n", args[0]);
    }

    public void cmdQuit(String[] args) {
	this.loop = false;
    }

    public void cmdHelp(String[] args) {
	console.printf("Send help!\n");
    }

    public void cmdLS(String[] args) throws EOFException {
	if (args.length >= 2) {
	    File file = getFile(args);
	    LSPeers(file);
	} else {
	    LSFiles();
	}
    }

    public void cmdUpdate(String[] args) {
	files = FileManager.getFileList();
	tracker.doUpdate(files);
    }

    public void cmdGetfile(String[] args) throws EOFException {
	File file = getFile(args);
	tracker.doGetfile(file);
	LSPeers(file);
    }

    public void cmdLook(String[] args) {
	this.lr = new LookRequest();
	console.printf("Creating look request:\n");
	listenCmds(lookCmds);
	files = tracker.doLook(lr);
	LSFiles();
	this.loop = true;
    }

    public void cmdInterested(String[] args) throws EOFException {
	File file = getFile(args);
	FilePeer peer = askFilePeer(file);
	peer.doInterested(file);
    }

    public void cmdHave(String[] args) throws EOFException {
	File file = getFile(args);
	FilePeer peer = askFilePeer(file);
	peer.doHave(file);
    }

    public void cmdGetpieces(String[] args) throws EOFException {
	File file = getFile(args);
	FilePeer peer = askFilePeer(file);
	int[] index = askParts(peer.getBufferMap());
	peer.doGetpieces(file, index);
	LSBufferMap(file.getBufferMap());
    }

    /* ------------------ Look ------------------*/

    public void lookCmdSize(String[] args) {
	CriterionOP op = CriterionOP.getCriterion(args[1]);
	int size = Integer.parseInt(args[2]);
	lr.addSizeCriterion(op, size);
	console.printf("Added size criterion.\n");
    }

    public void lookCmdKey(String[] args) {
	lr.addKey(args[1]);
	console.printf("Added key criterion.\n");
    }

    public void lookCmdFilename(String[] args) {
	lr.addFilename(args[1]);
	console.printf("Added filename criterion.\n");
    }
}
