package rz;

import java.lang.Exception;

public class RZWrongKeyException extends RuntimeException {
    public RZWrongKeyException() {
	super();
    }

    public RZWrongKeyException(String s) {
	super(s);
    }
}
