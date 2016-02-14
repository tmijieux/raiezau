package RZ;

class File {
    private String name;
    private String key;
    private int length;
    private int pieceSize = 1024;

    private boolean seed;

    File(String name, int length, String key, boolean seed) {
	this.name   = name;
	this.key    = key;
	this.length = length;
	this.seed   = seed;
    }
    
    String announceSeed() {
	return (name + " " + length + " " +
		pieceSize + " " + key);
    }

    String announceLeech() {
	return key;
    }
    
    boolean isSeeded() {
	return seed;
    }

}
