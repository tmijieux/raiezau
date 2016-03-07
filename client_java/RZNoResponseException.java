package RZ;

import java.lang.Exception;

public class RZNoResponseException extends Exception {
    RZNoResponseException() {
	super();
    }
    RZNoResponseException(String s) {
	super(s);
    }
}
