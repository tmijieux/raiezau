package rz;

public enum StringColorer {
    BLACK  ("\u001B[30m"),
    RED    ("\u001B[31m"),
    GREEN  ("\u001B[32m"),
    YELLOW ("\u001B[33m"),
    BLUE   ("\u001B[34m"),
    PURPLE ("\u001B[35m"),
    CYAN   ("\u001B[36m"),
    WHITE  ("\u001B[37m");

    private final String COLOR;
    private StringColorer(String color) {
	COLOR = color;
    }

    public String color(String s) {
	return COLOR + s + "\u001B[0m";
    }
    
    public String format(String format, Object ... args) {
	return color(String.format(format, args));
    }
}
