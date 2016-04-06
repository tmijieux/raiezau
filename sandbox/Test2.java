import java.util.*;

class Test2 {
    public static void main(String[] args) {
        BitSet bitSet = new BitSet(10);
        bitSet.set(10, false);
        byte [] array = bitSet.toByteArray();
        System.out.println(array.length);
    }
}
