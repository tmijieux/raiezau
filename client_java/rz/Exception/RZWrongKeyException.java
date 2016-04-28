package rz;

import java.lang.Exception;

public class RZWrongKeyException extends RZInvalidResponseException {
    public RZWrongKeyException() {
	super();
    }

    public RZWrongKeyException(String s) {
	super(s);
    }
}
