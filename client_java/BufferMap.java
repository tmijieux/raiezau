package RZ;

import java.util.BitSet;


class BufferMap {
    private BitSet bufferMap;
    private int totalPart;
    
    BufferMap(int pieceSize) {
        
	bufferMap = new BitSet(pieceSize);
    totalPart = pieceSize;
    }
	
    BufferMap(int pieceSize, RZFile file){
        bufferMap = new BitSet(pieceSize);
        if(file.isSeeded())      
            bufferMap.set(0,totalPart,true);
    }
    

    public void addCompletedPart(int partNumber) {
	    bufferMap.set(partNumber);
	}
    


    public boolean isCompleted(int partNumber){
	   return bufferMap.get(partNumber);
    }

    public String toString() {
        String stringedBufferMap = new String();
        for(int i = 0; i < totalPart; i++){
            if (bufferMap.get(i))
                stringedBufferMap +="1";
            else
                stringedBufferMap +="0";
        }
        
        return stringedBufferMap;
    }

}
