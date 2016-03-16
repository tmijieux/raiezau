package rz;
import java.util.regex.*;


public enum PatternMatcher {

    OK("ok"),
    GETFILE("peers ([a-fA-F0-9]*) \\[([^]])*\\]"),
    LOOK("list \\[([^]])*\\]"),
    DETECT("\\s*([a-z]*)\\s.*"),
    INTERESTED("\\s*interested\\s+([a-f0-9]*)\\s*"),
    HAVE("\\s*have\\s+([a-f0-9]*)\\s+(.*)\\s*"),
    GETPIECES("\\s*getpieces\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*"),
    DATA("\\s*data\\s+([a-f0-9]*)\\s*\\[\\s*(.*)\\s*\\]\\s*");

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
