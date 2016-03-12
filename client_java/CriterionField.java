package rz;

enum CriterionField {
    FILENAME ("filename"),
    KEY      ("key"),
    FILESIZE ("filesize");

    private final String text;
    CriterionField(String text) {
	this.text = text;
    }
    
    @Override
    public String toString() {
	return text;
    }
}
