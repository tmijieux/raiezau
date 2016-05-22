package rz;

class TestBitSet
{
    public static void main(String[] args)
    {
        launchTests();
    }

    public static void launchTests()
    {
        for (int i = 1; i < 100; ++i)
            for (int j = 1; j < 100; ++j)
                testSetAndTest(i, j);
    }
    
    public static void testSetAndTest(int k, int n)
    {
        BitSet bitSet = new BitSet(n);
        if (k < n)
            bitSet.set(k);
        for (int i = 0; i < n && i < k; ++i)
            assert bitSet.test(i) == false;
        
        if (k < n)
            assert bitSet.test(k) == true;
        
        for (int i = k+1; i < n; ++i)
            assert bitSet.test(i) == false;
    }
}
