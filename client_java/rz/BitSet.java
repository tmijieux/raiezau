package rz;

class BitSet
{
    private byte[] bits;

    public BitSet(int size)
    {
        bits = new byte [ (size+7) / 8 ];
        clearAllBytes();
    }

    public void set(int i)
    {
        bits[i/8] |= (1 << (i%8));
    }

    public void clear(int i)
    {
        bits[i/8] &= ~(1 << (i%8));
    }

    public void setAll()
    {
        for (int i = 0; i < bits.length; ++i)
            bits[i] = (byte)0xFF;
    }

    private void clearAllBytes()
    {
        for (int i = 0; i < bits.length; ++i)
            bits[i] = (byte) 0;
    }

    public void clearAll()
    {
        clearAllBytes();
    }

    public boolean test(int i)
    {
        return (bits[i/8] & (1 << (i%8))) != 0;
    }

    byte[] toByteArray()
    {
        byte [] copy = new byte[bits.length];
        for (int i = 0; i < bits.length; ++i)
            copy[i] = bits[i];
        return copy;
    }

    public String toHexString()
    {
        String s = new String();
        for (int i = 0; i < bits.length; ++i)
            s += String.format("%02x", bits[i]);
        return s;
    }
    
    @Override
    public String toString()
    {
        return toHexString();
    }
}
