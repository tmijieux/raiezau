package rz;

import java.util.*;

class StrategyAdvanced implements Strategy {
    private Tracker tracker;
    private Random random;

    private LookRequest lr;    
    private int maxFileSize = 1024;
    private final double ENLARGE_COEFF = 2;
    private final int MAX_BCL = 50;
    private final int MAX_PARTS = 5;

    private int randInt() {
	return Math.abs(random.nextInt());
    }

    public StrategyAdvanced(Tracker tracker) {
	this.tracker = tracker;
	random = new Random();
	newLookRequest(false);
    }

    private void newLookRequest(boolean enlarge) {
	if (enlarge)
	    maxFileSize *= ENLARGE_COEFF;
	lr = new LookRequest();
	lr.addSizeLE(maxFileSize);
    }

    private File chooseFile(List<File> files) throws RZNoFileException{
	for (File file : files) {
	    if (!file.isSeeded())
		return file;
	}
	throw new RZNoFileException("No file to seed.");
    }

    private FilePeer chooseFilePeer(List<FilePeer> peers) throws RZNoPeerException {
	if (peers.size() == 0)
	    throw new RZNoPeerException("No peer to choose.");
	return peers.get(randInt() % peers.size());
    }

    private int[] chooseParts(FilePeer peer) throws RZNoPartException {
	BufferMap bm = peer.getBufferMap();
	List<Integer> index = new ArrayList<Integer>();
	for(int i = 0; i < MAX_BCL && index.size() < MAX_PARTS; i++) {
	    int part = randInt() % peer.getPieceCount();
	    if(!bm.isCompleted(part))
		index.add(part);
	}
	if (index.size() == 0)
	    throw new RZNoPartException("No part found for " + peer);
	return Utils.convertIntegers(index);
    }

    private void downloadFile(File file) throws RZNoPeerException, RZNoPartException {
	tracker.doGetfile(file);
	Log.info("Downloading " + file);
	List<FilePeer> peers = file.getPeerList();
	FilePeer peer = chooseFilePeer(peers);
	int[] index = chooseParts(peer);
	peer.doGetpieces(file, index);
    }

    @Override
    public void share() {
	while (true) {
	    try {
		File file = chooseFile(FileManager.getFileList());
		downloadFile(file);
	    } catch (RZNoPeerException | RZNoFileException e) {
		newLookRequest(true);
		tracker.doLook(lr);
	    } catch (RZNoPartException e) {
		Log.info(e.toString());
	    }
	    tracker.doUpdate(FileManager.getFileList());
	}
    }
}
