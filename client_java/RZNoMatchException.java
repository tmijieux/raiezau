package rz;

import java.lang.Exception;

public class RZNoMatchException extends RuntimeException {
    public RZNoMatchException() {
	super();
    }
    
    public RZNoMatchException(String s) {
	super(s);
    }
}
