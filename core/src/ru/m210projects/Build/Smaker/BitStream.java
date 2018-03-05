package ru.m210projects.Build.Smaker;

import static ru.m210projects.Build.Engine.*;

public class BitStream {
	
	private static int offset;
	private static int index;
	private static byte[] data;
	private static int size;
	
	public static void init(byte[] data, int offset, int buffer_size)
	{
		BitStream.data = data;
		BitStream.size = buffer_size;
		BitStream.offset = offset;
		BitStream.index = 0;
	}
	
	public static byte getBit()
	{
		if((index >> 3) >= size) {
			System.err.println("BitStream::getBit() - ERROR: bitstream exhausted.");
			return -1;
		}

		return (((data[offset + (index >> 3)]) & pow2char[index++ & 7]) != 0) ? (byte) 1 : 0;
	}
	
	public static short get_bits8()
	{
		if((index >> 3) >= size) {
			System.err.println("BitStream::get_bits8() - ERROR: bitstream exhausted.");
			return -1;
		}
		
		short ret = 0;
		for (int i = 0; i < 8; i ++)
		{
			ret = (short) (ret >> 1);
			ret |= (getBit() << 7);
		}
		return ret;
	}
}
