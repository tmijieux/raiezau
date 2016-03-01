package RZ;

import java.util.*;
import java.util.regex.*;

class RZPattern {
    static private final Pattern emptyPattern = Pattern.compile("\\s*");

    static boolean isEmpty(String s) {
	Matcher match = emptyPattern.matcher(s);
	return match.matches();
    }
}
