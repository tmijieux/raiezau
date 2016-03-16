package rz;

import java.util.*;

public class BufferMap {
    private int size;
    private BitSet bitSet;

    public BufferMap(int size) {
        this.size = size;
	bitSet = new BitSet(size);
    }

    public BufferMap(File file){
	this((int) file.size());
        if (file.isSeeded()) {
            /* when the file is already fully present on the disk
               just fill the bitSet: */
            bitSet.set(0, size, true);
        }
    }

    public void addCompletedPart(int partNumber) {
	bitSet.set(partNumber);
    }

    public boolean isCompleted(int partNumber){
	return bitSet.get(partNumber);
    }

    @Override
    public String toString() {
        String stringedBufferMap = new String();
        for(int i = 0; i < size; i++) {
            if (bitSet.get(i))
                stringedBufferMap += "1";
            else
                stringedBufferMap += "0";
        }
        return stringedBufferMap;
    }
}
