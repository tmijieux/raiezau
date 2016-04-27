package rz;

import java.lang.Exception;

public class RZInvalidResponseException extends RuntimeException {
    public RZInvalidResponseException() {
	super();
    }

    public RZInvalidResponseException(String s) {
	super(s);
    }
}
