package rz;

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

    void addSizeCriterion(CriterionOP c, int size) {
	criterions.add(Criterion.CriterionSize(c, size));
    }
    void addSizeEQ(int size) {
	addSizeCriterion(CriterionOP.EQ, size);
    }
    void addSizeLT(int size) {
	addSizeCriterion(CriterionOP.LT, size);
    }
    void addSizeLE(int size) {
	addSizeCriterion(CriterionOP.LE, size);
    }
    void addSizeGT(int size) {
	addSizeCriterion(CriterionOP.GT, size);
    }
    void addSizeGE(int size) {
	addSizeCriterion(CriterionOP.GE, size);
    }

    private List<String> mapToString() {
	List<String> list = new ArrayList<String>();
	for(Criterion c: criterions) {
	    list.add(c.toString());
	}
	return list;
    }

    @Override
    public String toString() {
	return String.join(" ", mapToString());
    }
}
