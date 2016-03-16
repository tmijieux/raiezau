package rz;

import java.util.*;
import java.util.regex.*;

class RZPattern {
    static private final Pattern emptyPattern = Pattern.compile("\\s*");

    public static boolean isEmpty(String s) {
        System.err.println(">>>"+s+"<<<");
	Matcher match = emptyPattern.matcher(s);
	return match.matches();
    }
}
