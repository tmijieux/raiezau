package rz;

import java.lang.Exception;

public class RZNoResponseException extends RuntimeException {
    RZNoResponseException() {
	super();
    }
    RZNoResponseException(String s) {
	super(s);
    }
}
