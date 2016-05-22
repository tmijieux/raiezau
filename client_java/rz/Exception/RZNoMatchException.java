package rz;

import java.lang.Exception;

public class RZNoMatchException extends RZInvalidResponseException {
    public RZNoMatchException() {
	super();
    }

    public RZNoMatchException(String s) {
	super(s);
    }
}
