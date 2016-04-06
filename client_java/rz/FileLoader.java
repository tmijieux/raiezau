package rz;

import java.util.*;
import java.io.*;
import java.security.*;

class FileManager {
    private static final Map<String, File> filesByKey;
    
    
    static {
        filesByKey = new HashMap<String, File>();
        String dirname = Config.get("completed-files-directory");
        java.io.File fileDir = new java.io.File(dirname);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        loadCompleteFileFromDirectory(fileDir);
    }

    private static void loadIncompleteFileFromDirectory(java.io.File folder) {
	if (!folder.isDirectory()) {
	    throw new IllegalArgumentException();
        }

	for (java.io.File fileEntry : folder.listFiles()) {
	    if (fileEntry.isDirectory()) {
		loadIncompleteFileFromDirectory(fileEntry);
	    } else {
		restoreFileState(fileEntry);
	    }
	}
    }

    private static void loadCompleteFileFromDirectory(java.io.File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException();
        }
        Log.debug("Entering directory " + folder);

        for (java.io.File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                loadCompleteFileFromDirectory(fileEntry);
            } else {
                String name = fileEntry.getName();
                Log.info("loading file " + name);
                addCompleteFile(name);
            }
        }
        Log.debug("Leaving directory " + folder);
    }
    
    private static File insertFile(File newFile) {
        File f = filesByKey.get(newFile.getKey());
        if (f != null) {
            Log.warning(
                "file " + newFile.getName() + " with key "+
                newFile.getKey() + " is already present in "+
                "the file store and it is know as '"+f.getName()+"'.");
            newFile = null;
            return f;
        }
        filesByKey.put(newFile.getKey(), newFile);
	Log.info("Inserted file " + newFile);
        return newFile;
    }

    public static File addCompleteFile(String name) {
        File newFile = new File(name);
        return insertFile(newFile);
    }

    public static File addFile(String name, long length, String key) {
        File newFile = new File(name, length, key);
        return insertFile(newFile);
    }
    
    public static void saveFileState(File file) {
	try {
	    FileOutputStream saveFile =
                new FileOutputStream("./" + file.getName() +".ser");
	    ObjectOutputStream out = new ObjectOutputStream(saveFile);
	    out.writeObject(file);
	    out.close();
	    saveFile.close();
	} catch (IOException e){
            
	}
    }

    public static File restoreFileState(java.io.File file) {
	File f = null;
	try {
	    FileInputStream saveFile = new FileInputStream(file);
	    ObjectInputStream in = new ObjectInputStream(saveFile);
	    f = (File) in.readObject();
	    in.close();
	    saveFile.close();
	} catch (FileNotFoundException e) {

	} catch (IOException | ClassNotFoundException e) {
            
	}
	return f;
    }

    public static List<File> getFileList() {
        return new ArrayList<File>(filesByKey.values());
    }

    public static File getByKey(String key) {
	return filesByKey.get(key);
    }
};
