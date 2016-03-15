package rz;
import java.util.regex.*;


public enum PatternMatcher {

    OK("ok"),
    GETFILE("peers ([a-fA-F0-9]*) \\[([^]])*\\]"),
    LOOK("list \\[([^]])*\\]"),
    DETECT("\\s*([a-z]*)\\s.*");

    private String patternString;
    private java.util.regex.Pattern pattern;

    private PatternMatcher(String patternString) {
        this.patternString = patternString;
        this.pattern = Pattern.compile(patternString);
    }

    public Matcher getMatcher(String str) {
	Matcher match = pattern.matcher(str);

	if (!match.matches()) {
	    throw new RZNoMatchException(
                "Response '" + str + "' does not match pattern '" +
                patternString + "'.");
	}
	return match;
    }
}
