package rz;

public enum CriterionOP {
    EQ ("="),
    NE ("!="),
    LT ("<"),
    LE ("<="),
    GT (">"),
    GE (">=");

    private final String text;
    private CriterionOP(String text) {
	this.text = text;
    }

    @Override
    public String toString() {
	return text;
    }
}
