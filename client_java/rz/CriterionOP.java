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

    static CriterionOP getCriterion(String name) {
	for(CriterionOP c : CriterionOP.values()) {
	    if (c.toString().compareTo(name) == 0) 
		return c;
	}
	throw new RuntimeException("NoSuchCriterion: " + name);
    }

    @Override
    public String toString() {
	return text;
    }
}
