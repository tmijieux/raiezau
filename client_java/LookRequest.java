package RZ;

import java.util.*;
import java.lang.*;

class LookRequest {
    private List<Criterion> criterions;

    LookRequest() {
	criterions = new ArrayList<Criterion>();
    }
    
    void addFilename(String filename) {
	criterions.add(Criterion.CriterionFilename(filename));
    }

    void addKey(String key) {
	criterions.add(Criterion.CriterionKey(key));
    }

    void addSizeEQ(int size) {
	criterions.add(Criterion.CriterionSize(CriterionOP.EQ, size));
    }
    void addSizeLT(int size) {
	criterions.add(Criterion.CriterionSize(CriterionOP.LT, size));
    }
    void addSizeLE(int size) {
	criterions.add(Criterion.CriterionSize(CriterionOP.LE, size));
    }
    void addSizeGT(int size) {
	criterions.add(Criterion.CriterionSize(CriterionOP.GT, size));
    }
    void addSizeGE(int size) {
	criterions.add(Criterion.CriterionSize(CriterionOP.GE, size));
    }

    private List<String> mapToString() {
	List<String> list = new ArrayList<String>();
	for(Criterion c: criterions) {
	    list.add(c.toString());
	}
	return list;
    }

    public String toString() {
	return String.join(" ", mapToString());
    }
}
