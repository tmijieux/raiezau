package rz;

import java.lang.Exception;

public class RZNoFileException extends Exception {
    public RZNoFileException() {
	super();
    }

    public RZNoFileException(String s) {
	super(s);
    }
}
