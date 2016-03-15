package rz;

import java.util.*;

class BufferMap {
    private BitSet bufferMap;
    private int size;

    BufferMap(int size) {
	bufferMap = new BitSet(size);
	this.size = size;
    }

    BufferMap(int size, File file){
	this(size);
        if(file.isSeeded())
            bufferMap.set(0, size, true);
    }

    void addCompletedPart(int partNumber) {
	bufferMap.set(partNumber);
    }

    boolean isCompleted(int partNumber){
	return bufferMap.get(partNumber);
    }

    public String toString() {
        String stringedBufferMap = new String();
        for(int i = 0; i < size; i++) {
            if (bufferMap.get(i))
                stringedBufferMap += "1";
            else
                stringedBufferMap += "0";
        }

        return stringedBufferMap;
    }

}
