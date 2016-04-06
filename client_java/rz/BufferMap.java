package rz;

import java.util.*;

public class BufferMap {
    private int size; // length in byte
    private BitSet bitSet;
    private byte[] bufferMap;

    public BufferMap(int size) {
        this.size = size;
	bitSet = new BitSet(size);
	bitSet.set(0, size, false);

	bufferMap = new byte[size/8];
    }

    public BufferMap(File file){
	this.size = (int) file.getLength();
	bitSet = new BitSet(size);
	bufferMap = new byte[size/8];
        if (file.isSeeded()) {
            /* when the file is already fully present on the disk
               just fill the bitSet: */
            bitSet.set(0, size, true);
	    //this.setAllBits();
        }
    }

    public void addCompletedPart(int partNumber) {
	//bitSet.set(partNumber);
        this.setBit(partNumber);
    }

    public boolean isCompleted(int partNumber){
	//return bitSet.get(partNumber);
	return this.isBitSet(partNumber);
    }

    @Override //Useless method
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

    private void setBit(int bitNumber){
	int bitIndex = bitNumber/8;
	int pos = (bitIndex + bitNumber%8) -1;

	bufferMap[bitIndex] |= 1 << pos; // shall set the bit to 1
    }

    private void setAllBits(){
	for (int index = 0; index < bufferMap.length; index++){
	    for (int bit = 0; bit < 8; bit++){
		bufferMap[index] |= 1 << bit;
	    }
	}
    }

    private boolean isBitSet(int bitNumber){
	int bitIndex = bitNumber/8;
	int pos = (bitIndex + bitNumber%8) -1;

	return (bufferMap[bitIndex] & (1 << pos)) == 1;
    }

    
    public byte[] toByteArray(){
	return bufferMap;
    }
}
