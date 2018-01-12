/*
	libsmacker - A C library for decoding .smk Smacker Video files
	Copyright (C) 2012-2013 Greg Kennedy

	See smacker.h for more information.

	smk_bitstream.h
		SMK bitstream structure. Presents a block of raw bytes one
		bit at a time, and protects against over-read.
*/

package ru.m210projects.Build.Smaker;

/*
Bitstream structure
Pointer to raw block of data and a size limit.
Maintains internal pointers to byte_num and bit_number.
*/
class smk_bit {
	byte[] bitstream;
	int size;

	int byte_num;
	byte bit_num;
}

public class Smk_bitstream {

	/* This macro interrogates return code from bs_read_1 and
	jumps to error label if problems occur. */
	protected static int smk_bs_safe_read_1(smk_bit t, int uc) {
		if ((char)(uc = smk_bs_read_1(t)) < 0) 
		{ 
			System.out.println("libsmacker::smk_bs_safe_read_1(" +t+ "," +uc +") - ERROR (file: %s, line: %lu)");
			return -1;
		} 
		return uc;
	}
	
	protected static int smk_bs_safe_read_8(smk_bit t, int s) {
		if ((s = smk_bs_read_8(t)) < 0) 
		{ 	
			System.out.println("libsmacker::smk_bs_safe_read_8(" +t+ "," +s+ ") - ERROR (file: %s, line: %lu)");
			return -1;
		} 
		return s;
	}
	
	/* BITSTREAM Functions */
	private static smk_bit ret;
	protected static smk_bit smk_bs_init(byte[] b, int offset, int size)
	{
		/* allocate a bitstream struct */
		if(ret == null)
			ret = new smk_bit();
		
		/* set up the pointer to bitstream, and the size counter */
		if(ret.bitstream == null)
			ret.bitstream = new byte[size];
		if(ret.bitstream.length < size)
			ret.bitstream = new byte[size];

		for(int i = 0; i < size; i++)
			ret.bitstream[i] = b[i + offset];
		
		ret.size = size;

		/* point to initial byte */
		ret.byte_num = -1;
		ret.bit_num = 7;
		return ret;
	}
	
	/* returns "enough bits left for a byte?" */
	protected static boolean smk_bs_query_8(smk_bit bs)
	{
		/* sanity check */
		if(bs == null)
			return false;
		return (bs.byte_num + 1 < bs.size);
	}
	
	/* returns "any bits left?" */
	protected static boolean smk_bs_query_1(smk_bit bs)
	{
		/* sanity check */
		if(bs == null)
			return false;
		return (bs.bit_num < 7 || smk_bs_query_8(bs));
	}
	
	/* Reads a bit
	Returns -1 if error encountered */
	protected static int smk_bs_read_1(smk_bit bs)
	{
		/* sanity check */
		if(bs == null)
			return -1;
	
		/* don't die when running out of bits, but signal */
		if (!smk_bs_query_1(bs))
		{
//			System.err.println("libsmacker::smk_bs_read_1(bs) - ERROR: bitstream exhausted.");
			return -1;
		}
	
		/* advance to next bit */
		bs.bit_num++;

		/* Out of bits in this byte: next! */
		if (bs.bit_num > 7)
		{
			bs.byte_num ++;
			bs.bit_num = 0;
		}
	
		return (((bs.bitstream[bs.byte_num]) & (0x01 << bs.bit_num)) != 0)?1:0;
	}
	
	/* Reads a byte
	Returns -1 if error. */
	protected static int  smk_bs_read_8(smk_bit bs)
	{
		int ret = 0, i;
	
		/* sanity check */
		if(bs == null)
			return -1;
	
		/* don't die when running out of bits, but signal */
		if (!smk_bs_query_8(bs))
		{
			System.out.println("libsmacker::smk_bs_read_8(bs) - ERROR: bitstream exhausted.");
			return -1;
		}
	
		for (i = 0; i < 8; i ++)
		{
			ret = ret >> 1;
			ret |= (smk_bs_read_1(bs) << 7);
		}
		return ret;
	}
}
