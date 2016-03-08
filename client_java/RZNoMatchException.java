package RZ;

import java.lang.Exception;

public class RZNoMatchException extends Exception {
    RZNoMatchException() {
	super();
    }
    RZNoMatchException(String s) {
	super(s);
    }
}
