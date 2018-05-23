// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)


package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LZW {
	
	private final int LZWSIZE = 16384;
	private byte[] lzwbuf1, lzwbuf4, lzwbuf5;
	private short[] lzwbuf2, lzwbuf3;
	
	public LZW()
	{
		lzwbuf1 = new byte[LZWSIZE+(LZWSIZE>>4)];
		lzwbuf2 = new short[LZWSIZE+(LZWSIZE>>4)];
		lzwbuf3 = new short[LZWSIZE+(LZWSIZE>>4)];
		lzwbuf4 = new byte[LZWSIZE];
		lzwbuf5 = new byte[LZWSIZE+(LZWSIZE>>4)];
	}
	
	public int kdfread(byte[] buffer, int dasizeof, int count, int fil)
	{
		int j, k, kgoal, leng;

		if (dasizeof > LZWSIZE) { count *= dasizeof; dasizeof = 1; }
		
		leng = kRead(fil, 2);
		if (kRead(fil, lzwbuf5, leng) != leng) return -1;
		k = 0; kgoal = lzwuncompress(lzwbuf5, leng, lzwbuf4);
		System.arraycopy(lzwbuf4, 0, buffer, 0, dasizeof);
		k += dasizeof;

		int ptr = 0;
		for(int i = 1;i < count; i++)
		{
			if (k >= kgoal)
			{
				leng = kRead(fil,2);
				if(leng == 26290)
					return -1;
				if (kRead(fil,lzwbuf5, leng) != leng) return -1;
				k = 0; kgoal = lzwuncompress(lzwbuf5, leng, lzwbuf4);
			}
			for(j = 0; j < dasizeof; j++) 
				buffer[ptr+j+dasizeof] = (byte) ((buffer[ptr+j]+lzwbuf4[j+k])&255);
			k += dasizeof;
			ptr += dasizeof;
		}
		return count;
	}
	
	public int lzwuncompress(byte[] lzwinbuf, int compleng, byte[] lzwoutbuf)
	{
		ByteBuffer inbuf = ByteBuffer.wrap(lzwinbuf);
		inbuf.order( ByteOrder.LITTLE_ENDIAN);
		
		int strtot = inbuf.getShort(2);
		if (strtot == 0)
		{
			inbuf.get(lzwoutbuf, 4, ((compleng-4)+3)>>2);
			return inbuf.getShort(0); //uncompleng
		}
		for(int i=255;i>=0;i--) { lzwbuf2[i] = (short) i; lzwbuf3[i] = (short) i; }
		int currstr = 256, bitcnt = (4<<3), outbytecnt = 0;
		int numbits = 8, oneupnumbits = (1<<8), intptr, dat, leng;
		do
		{
			intptr = inbuf.getInt(bitcnt>>3);
			dat = ((intptr>>(bitcnt&7)) & (oneupnumbits-1));
			bitcnt += numbits;
			if ((dat&((oneupnumbits>>1)-1)) > ((currstr-1)&((oneupnumbits>>1)-1)))
				{ dat &= ((oneupnumbits>>1)-1); bitcnt--; }

			lzwbuf3[currstr] = (short) dat;

			for(leng=0;dat>=256;leng++,dat=lzwbuf3[dat])
				lzwbuf1[leng] = (byte) lzwbuf2[dat];

			lzwoutbuf[outbytecnt++] = (byte) dat;
			for(int i=leng-1;i>=0;i--) lzwoutbuf[outbytecnt++] = lzwbuf1[i];

			lzwbuf2[currstr-1] = (short) dat; lzwbuf2[currstr] = (short) dat;
			currstr++;
			if (currstr > oneupnumbits) { numbits++; oneupnumbits <<= 1; }
		} while (currstr < strtot);
		return(inbuf.getShort(0)); //uncompleng
	}
}
