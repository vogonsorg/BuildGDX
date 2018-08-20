// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been ported to Java and modified by Alexander Makarov-[M210] (m210-2007@mail.ru)


package ru.m210projects.Build.FileHandle;

import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import ru.m210projects.Build.Types.LittleEndian;

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
	
	public void kdfwrite(byte[] buffer, int dasizeof, int count, int fil)
	{
		if (dasizeof > LZWSIZE) { count *= dasizeof; dasizeof = 1; }
		System.arraycopy(buffer, 0, lzwbuf4, 0, dasizeof);
		int k = dasizeof;
		
		int leng, ptr = 0;
		if (k > LZWSIZE-dasizeof)
		{
			leng = lzwcompress(lzwbuf4,k,lzwbuf5); 
			k = 0; 
			Bwrite(fil, leng, 2);
			Bwrite(fil, lzwbuf5, leng);
		}
		
		for(int i=1;i<count;i++)
		{
			for(int j=0;j<dasizeof;j++) 
				lzwbuf4[j+k] = (byte) ((buffer[ptr+j+dasizeof]-buffer[ptr+j])&255);
			k += dasizeof;
			if (k > LZWSIZE-dasizeof)
			{
				leng = lzwcompress(lzwbuf4,k,lzwbuf5); k = 0;
				Bwrite(fil, leng,2); 
				Bwrite(fil, lzwbuf5, leng);
			}
			ptr += dasizeof;
		}
		
		if (k > 0)
		{
			leng = lzwcompress(lzwbuf4,k,lzwbuf5); 
			Bwrite(fil, leng,2); 
			Bwrite(fil, lzwbuf5,leng);
		}
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
		for(int i = 1; i < count; i++)
		{
			if (k >= kgoal)
			{
				leng = kRead(fil,2);
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
	
	private int lzwcompress(byte[] lzwinbuf, int uncompleng, byte[] lzwoutbuf)
	{
		for(int i=255;i>=0;i--) { lzwbuf1[i] = (byte) i; lzwbuf3[i] = (short) ((i+1)&255); }
		Arrays.fill(lzwbuf2, 0, 256, (short)-1);
		Arrays.fill(lzwoutbuf,0,((uncompleng+15)+3),(byte)0);

		short addrcnt = 256; int bytecnt1 = 0; int bitcnt = (4<<3);
		int numbits = 8; int oneupnumbits = (1<<8);
		short addr = 0;
		do
		{
			addr = (short) (lzwinbuf[bytecnt1] & 0xFF);
			do
			{
				bytecnt1++;
				if (bytecnt1 == uncompleng) break;
				if (lzwbuf2[addr] < 0) {lzwbuf2[addr] = addrcnt; break;}
				short newaddr = lzwbuf2[addr];
				while (lzwbuf1[newaddr] != lzwinbuf[bytecnt1])
				{
					short zx = lzwbuf3[newaddr];
					if (zx < 0) {lzwbuf3[newaddr] = addrcnt; break;}
					newaddr = zx;
				}
				if (lzwbuf3[newaddr] == addrcnt) break;
				addr = newaddr;
			} while (addr >= 0);
			lzwbuf1[addrcnt] = lzwinbuf[bytecnt1];
			lzwbuf2[addrcnt] = -1;
			lzwbuf3[addrcnt] = -1;

			int intptr = LittleEndian.getInt(lzwoutbuf, bitcnt>>3);
			LittleEndian.putInt(lzwoutbuf, bitcnt>>3, intptr | (addr<<(bitcnt&7)));
			bitcnt += numbits;
			if ((addr&((oneupnumbits>>1)-1)) > ((addrcnt-1)&((oneupnumbits>>1)-1)))
				bitcnt--;

			addrcnt++;
			if (addrcnt > oneupnumbits) { numbits++; oneupnumbits <<= 1; }
		} while ((bytecnt1 < uncompleng) && (bitcnt < (uncompleng<<3)));

		int intptr = LittleEndian.getInt(lzwoutbuf, bitcnt>>3);
		LittleEndian.putInt(lzwoutbuf, bitcnt>>3, intptr | (addr<<(bitcnt&7)));
		bitcnt += numbits;
		if ((addr&((oneupnumbits>>1)-1)) > ((addrcnt-1)&((oneupnumbits>>1)-1)))
			bitcnt--;

		LittleEndian.putShort(lzwoutbuf, 0, (short) uncompleng);
		if (((bitcnt+7)>>3) < uncompleng)
		{
			LittleEndian.putShort(lzwoutbuf, 2, addrcnt);
			return((bitcnt+7)>>3);
		}
		
		LittleEndian.putShort(lzwoutbuf, 2, (short) 0);
		for(int i=0;i<uncompleng;i++) 
			lzwoutbuf[i+4] = lzwinbuf[i];

		return(uncompleng+4);
	}
	
	private int lzwuncompress(byte[] lzwinbuf, int compleng, byte[] lzwoutbuf)
	{
		ByteBuffer inbuf = ByteBuffer.wrap(lzwinbuf);
		inbuf.order( ByteOrder.LITTLE_ENDIAN);
		
		int strtot = inbuf.getShort(2);
		
		inbuf.position(4);
		if (strtot == 0)
		{
			inbuf.get(lzwoutbuf, 0, (compleng-4)+3);
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
