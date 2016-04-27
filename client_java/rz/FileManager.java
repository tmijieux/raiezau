package rz;

import java.util.*;
import java.io.*;
import java.security.*;
import java.lang.*;
import java.lang.reflect.*;

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

    private static void loadFileFromDirectory(java.io.File folder, Method fileJob) {
	if (!folder.isDirectory()) {
	    throw new IllegalArgumentException();
        }
        Log.debug("Entering directory " + folder);

	for (java.io.File fileEntry : folder.listFiles()) {
	    if (fileEntry.isDirectory()) {
		loadFileFromDirectory(fileEntry, fileJob);
	    } else {
		try {
		    fileJob.invoke(null, fileEntry);
		} catch (ReflectiveOperationException e) {
		    Log.severe(e.toString());
		}
	    }
	}	
    }

    private static void loadFileFromDirectory(java.io.File folder, String name) {
	try {
	    Method method = FileManager.class.getMethod(name, java.io.File.class);
	    loadFileFromDirectory(folder, method);
	} catch (NoSuchMethodException e) {
	    Log.severe(e.toString());
	}
    }

    public static void loadIncompleteFile(java.io.File fileEntry) {
	restoreFileState(fileEntry);
    }

    public static void loadCompleteFile(java.io.File fileEntry) {
	String name = fileEntry.getName();
	Log.info("loading file " + name);
	addCompleteFile(name);	
    }

    private static void loadIncompleteFileFromDirectory(java.io.File folder) {
	loadFileFromDirectory(folder, "loadIncompleteFile");
    }
    
    private static void loadCompleteFileFromDirectory(java.io.File folder) {
	loadFileFromDirectory(folder, "loadCompleteFile");
    }
    
    private static File insertFile(File newFile) {
        File f = filesByKey.get(newFile.getKey());
        if (f != null) {
            /*
	    Log.warning(
                "file " + newFile.getName() + " with key "+
                newFile.getKey() + " is already present in "+
                "the file store and it is know as '"+f.getName()+"'.");
	    */
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
}
