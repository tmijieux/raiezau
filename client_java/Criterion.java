package RZ;

class Criterion {
    private String text;
    private CriterionOP op;
    private CriterionField field;

    private Criterion(CriterionField field, CriterionOP op, 
		      String text) {
	this.op = op;
	this.field = field;
	this.text = text;
    }

    static Criterion CriterionFilename(String filename) {
	return new Criterion(CriterionField.FILENAME, CriterionOP.EQ,
			     filename);
    }

    static Criterion CriterionKey(String key) {
	return new Criterion(CriterionField.KEY, CriterionOP.EQ, key);
    }

    static Criterion CriterionSize(CriterionOP op, int size) {
	return new Criterion(CriterionField.FILESIZE, op, 
			     Integer.toString(size));
    }

    public String toString() {
	return field.toString() + op.toString() + '"' + text + '"';
    }
}
