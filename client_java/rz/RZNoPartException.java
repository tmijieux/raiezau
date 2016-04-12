package rz;

import java.lang.Exception;

public class RZNoPartException extends RuntimeException {
    RZNoPartException() {
	super();
    }

    RZNoPartException(String s) {
	super(s);
    }
}
