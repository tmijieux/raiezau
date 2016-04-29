package rz;

import java.lang.Exception;

public class RZNoPeerException extends Exception {
    public RZNoPeerException() {
	super();
    }

    public RZNoPeerException(String s) {
	super(s);
    }
}
