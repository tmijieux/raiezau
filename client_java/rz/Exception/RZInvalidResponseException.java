package rz;

import java.lang.Exception;

public class RZInvalidResponseException extends Exception {
    public RZInvalidResponseException() {
	super();
    }

    public RZInvalidResponseException(String s) {
	super(s);
    }
}
