package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class StrategyTestPartialFile2 implements Strategy {
    private Tracker tracker;

    public StrategyTestPartialFile2(Tracker t) {
	this.tracker = t;
    }


    void completePartialFile(File fileToBeCompleted) {
	
	LookRequest lr = new LookRequest();
	lr.addFilename("img.jpg");

	List<File> files =  tracker.doLook(lr);	
	File file;

	try {
	    file = files.get(0);
	} catch (Exception e) {
	    Log.warning(e.toString());
	    return;
	}
	
	Log.info("File " + file);
	tracker.doGetfile(file);

	List<FilePeer> peers = file.getPeerList();
	FilePeer peer;

	try {
	    peer = peers.get(0);
	} catch (Exception e) {
	    Log.warning(e.toString());
	    return;
	}

	Log.info("Gonna try DL");
	peer.doInterested(file);
	peer.doHave(file);

	BufferMap bm = fileToBeCompleted.getBufferMap();
	ArrayList<Integer> piecesIndex = new ArrayList<Integer>();
	int pCount = fileToBeCompleted.getPieceCount();
	for (int i = 0; i < pCount; i++){
	    if (!bm.isCompleted(i)){
		Log.info("\nADDING : " + i + "\n");
		piecesIndex.add(i);
	    }
	}
	
	peer.doGetpieces(file, Utils.convertIntegers(piecesIndex));
	

	
    }

    
    @Override
    public void share(){
	Log.info("Strategy Partial");
	File fileToBeCompleted = null;
	List<File> myFiles = FileManager.getFileList();
	for (File file : myFiles){
	    if(file.getName().equals("img.jpg"))
		fileToBeCompleted = file;
	}
	    
	

	Log.info("\n FILE TO BE COMPLETED " + fileToBeCompleted);
	
	completePartialFile(fileToBeCompleted);
	Log.info("Partial file completed");
    }


}
