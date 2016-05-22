package rz;

import java.lang.Exception;

public class RZNoResponseException extends RZInvalidResponseException {
    RZNoResponseException() {
	super();
    }

    RZNoResponseException(String s) {
	super(s);
    }
}
