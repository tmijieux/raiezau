package rz;

public enum CriterionOP {
    EQ ("="),
    NE ("!="),
    LT ("<"),
    LE ("<="),
    GT (">"),
    GE (">=");

    private final String text;
    public CriterionOP(String text) {
	this.text = text;
    }

    @Override
    public String toString() {
	return text;
    }
}
