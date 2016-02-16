package RZ;

enum CriterionOP {
    EQ ("="),
    NE ("!="),
    LT ("<"),
    LE ("<="),
    GT (">"),
    GE (">=");

    private final String text;
    CriterionOP(String text) {
	this.text = text;
    }
    
    @Override
    public String toString() {
	return text;
    }
} 
