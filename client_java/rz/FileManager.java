package rz;

import java.util.*;
import java.io.*;
import java.security.*;
import java.lang.*;
import java.lang.reflect.*;

class FileManager {
    private FileManager() {
    }

    private static final Map<String, File> filesByKey;
    private static final Map<String, File> filesByName;

    static {	
        filesByKey = new HashMap<String, File>();
        filesByName = new HashMap<String, File>();
 		
        String dirnameIncomplete = Config.get("partial-files-directory");
        java.io.File incompleteFileDir = new java.io.File(dirnameIncomplete);
        if (!incompleteFileDir.exists()) {
            incompleteFileDir.mkdir();
        }
	loadIncompleteFileFromDirectory(incompleteFileDir);

        String dirname = Config.get("completed-files-directory");
        java.io.File fileDir = new java.io.File(dirname);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        loadCompleteFileFromDirectory(fileDir);
        registerShutdownHook();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run() {
                for (Map.Entry<String,File> e : filesByKey.entrySet()) {
                    File f = e.getValue();
                    FileManager.saveFileState(f);
                }
            }
        });
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
	File f = restoreFileState(fileEntry);
        f.reinitIncompleteFile();
        insertFile(f);
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
        String name = newFile.getName();
        File f = filesByName.get(name);
        if (f != null)
            return f;
        
        filesByKey.put(newFile.getKey(), newFile);
        filesByName.put(name, newFile);
	Log.info("Inserted file " + newFile);
        return newFile;
    }

    public static File addCompleteFile(String name) {
        File newFile = new File(name);
        return insertFile(newFile);
    }

    public static File addFile(String name, long length,
                               int pieceSize, String key) {
	File f = filesByKey.get(key);
	if (f != null)
	    return f;
        File newFile = new File(name, length, pieceSize, key);
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
            Log.info("exception : "+ e);
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
            Log.info("exception : "+ e);
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
