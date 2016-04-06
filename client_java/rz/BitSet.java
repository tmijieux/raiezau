package rz;

class BitSet
{
    byte [] bits;
    
    public BitSet(int size)
    {
        bits = new byte [ (size+7) / 8 ];
        clearAll();
    }

    public void set(int i)
    {
        bits[i / 8] |= (1 << (i % 8));
    }

    public void clear(int i)
    {
        bits[i / 8] &= ~(1 << (i % 8));
    }

    public void setAll()
    {
        for (int i = 0; i < bits.length; ++i)
            bits[i] = (byte)0xFF;
    }

    public void clearAll()
    {
        for (int i = 0; i < bits.length; ++i)
            bits[i] = 0;
    }

    public boolean test(int i)
    {
        return (bits[i/8] & (1 << (i % 8))) != 0;
    }

    byte [] toByteArray()
    {
        byte [] copy = new byte[bits.length];
        for (int i = 0; i < bits.length; ++i)
            copy[i] = bits[i];
        return copy;
    }
}
