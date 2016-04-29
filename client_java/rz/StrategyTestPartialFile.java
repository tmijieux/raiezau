package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class StrategyTestPartialFile implements Strategy {
    private Tracker tracker;

    public StrategyTestPartialFile(Tracker t) {
	this.tracker = t;
    }


    void generatePartialFile() {

	
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
	//do getFile sur tracker
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
	

	int pCount = peer.getPieceCount();
	int[] index = new int[pCount/2];
	for (int i = 0; i < pCount/2; i++)
	    index[i] = i;
	

	peer.doGetpieces(file, index);
	FileManager.saveFileState(file);
    
    }

    @Override
    public void share(){
	Log.info("Strategy Partial");
	generatePartialFile();
	Log.info("Partial file generated");
    }
    
}
