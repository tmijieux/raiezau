import java.util.regex.*;

class Test {

    public static void main(String[] args) {
        final String patternString = "peers ([a-fA-F0-9]*) \\[([^]])*\\]";
        final String str = "peers a5aec02572473b2c486856a02d066c8d []\n";

        Pattern pattern = Pattern.compile(patternString);
        Matcher match = pattern.matcher(str);

        System.out.println("length: "+ str.length());
        if (match.matches()) {
            System.out.println("ok");
        } else {
            System.out.println("ko");
        }
    }
}
