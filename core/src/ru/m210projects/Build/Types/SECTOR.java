/*
 *  Sector structure code originally written by Ken Silverman
 *	Ken Silverman's official web site: http://www.advsys.net/ken
 *
 *  See the included license file "BUILDLIC.TXT" for license info.
 *
 *  This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXWALLS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.m210projects.Build.FileHandle.DataResource;
import ru.m210projects.Build.FileHandle.Resource;

public class SECTOR {
	public static final int sizeof = 40;
	private static final ByteBuffer buffer = ByteBuffer.allocate(sizeof).order( ByteOrder.LITTLE_ENDIAN);

	public short wallptr, wallnum; //4
	public int ceilingz, floorz; //8
	public short ceilingstat, floorstat; //4
	public short ceilingpicnum, ceilingheinum; //4
	public byte ceilingshade; //1
	
	public short ceilingpal, ceilingxpanning, ceilingypanning; //3
	public short floorpicnum, floorheinum; //4
	public byte floorshade; //1
	public short floorpal, floorxpanning, floorypanning; //3
	public short visibility, filler; //2
	public short lotag, hitag, extra; //6
	
	public SECTOR() {}
	
	public SECTOR(byte[] data) {
		buildSector(new DataResource(data));
	}
	
	public SECTOR(Resource data) {
    	buildSector(data);
	}
	
	public void buildSector(Resource bb)
	{
		wallptr = bb.readShort();
		if(wallptr < 0 || wallptr >= MAXWALLS) wallptr = 0;
    	wallnum = bb.readShort();
    	ceilingz = bb.readInt();
    	floorz = bb.readInt();
    	ceilingstat = bb.readShort();
    	floorstat = bb.readShort();
    	ceilingpicnum = bb.readShort();
    	if(ceilingpicnum < 0 || ceilingpicnum >= MAXTILES) ceilingpicnum = 0;
    	ceilingheinum = bb.readShort();
    	ceilingshade = bb.readByte();
    	ceilingpal = (short) (bb.readByte()&0xFF);
    	ceilingxpanning = (short) (bb.readByte()&0xFF);
    	ceilingypanning = (short) (bb.readByte()&0xFF);
    	floorpicnum = bb.readShort();
    	if(floorpicnum < 0 || floorpicnum >= MAXTILES) floorpicnum = 0;
    	floorheinum = bb.readShort();
    	floorshade = bb.readByte();
    	floorpal = (short) (bb.readByte()&0xFF);
    	floorxpanning = (short) (bb.readByte()&0xFF);
    	floorypanning = (short) (bb.readByte()&0xFF);
    	visibility = (short) (bb.readByte()&0xFF);
    	filler = bb.readByte();
      	lotag = bb.readShort();
    	hitag = bb.readShort();
    	extra = bb.readShort();
	}
	
	public void set(SECTOR src)
	{
		wallptr = src.wallptr;
    	wallnum = src.wallnum;
    	ceilingz = src.ceilingz;
    	floorz = src.floorz;
    	ceilingstat = src.ceilingstat;
    	floorstat = src.floorstat;
    	ceilingpicnum = src.ceilingpicnum;
    	ceilingheinum = src.ceilingheinum;
    	ceilingshade = src.ceilingshade;
    	ceilingpal = src.ceilingpal;
    	ceilingxpanning = src.ceilingxpanning;
    	ceilingypanning = src.ceilingypanning;
    	floorpicnum = src.floorpicnum;
    	floorheinum = src.floorheinum;
    	floorshade = src.floorshade;
    	floorpal = src.floorpal;
    	floorxpanning = src.floorxpanning;
    	floorypanning = src.floorypanning;
    	visibility = src.visibility;
    	filler = src.filler;
      	lotag = src.lotag;
    	hitag = src.hitag;
    	extra = src.extra;
	}
	
	
	public byte[] getBytes()
	{
		buffer.clear();
		
		buffer.putShort(this.wallptr);
    	buffer.putShort(this.wallnum);
    	buffer.putInt(this.ceilingz);
    	buffer.putInt(this.floorz);
    	buffer.putShort(this.ceilingstat);
    	buffer.putShort(this.floorstat);
    	buffer.putShort(this.ceilingpicnum);
    	buffer.putShort(this.ceilingheinum);
    	buffer.put(this.ceilingshade);
    	buffer.put((byte)this.ceilingpal);
    	buffer.put((byte)this.ceilingxpanning);
    	buffer.put((byte)this.ceilingypanning);
    	buffer.putShort(this.floorpicnum);
    	buffer.putShort(this.floorheinum);
    	buffer.put(this.floorshade);
    	buffer.put((byte)this.floorpal);
    	buffer.put((byte)this.floorxpanning);
    	buffer.put((byte)this.floorypanning);
    	buffer.put((byte)this.visibility);
    	buffer.put((byte)this.filler);
    	buffer.putShort(this.lotag);
    	buffer.putShort(this.hitag);
    	buffer.putShort(this.extra);
		
    	return buffer.array();
	}

	public String toString()
	{
		String out = "wallptr " + wallptr + " \r\n";
		out += "wallnum " + wallnum + " \r\n";
		out += "ceilingz " + ceilingz + " \r\n";
		out += "floorz " + floorz + " \r\n";
		out += "ceilingstat " + ceilingstat + " \r\n";
		out += "floorstat " + floorstat + " \r\n";
		out += "ceilingpicnum " + ceilingpicnum + " \r\n";
		out += "ceilingheinum " + ceilingheinum + " \r\n";
		out += "ceilingshade " + ceilingshade + " \r\n";
		out += "ceilingpal " + ceilingpal + " \r\n";
		out += "ceilingxpanning " + ceilingxpanning + " \r\n";
		out += "ceilingypanning " + ceilingypanning + " \r\n";
		out += "floorpicnum " + floorpicnum + " \r\n";
		out += "floorheinum " + floorheinum + " \r\n";
		out += "floorshade " + floorshade + " \r\n";
		out += "floorpal " + floorpal + " \r\n";
		out += "floorxpanning " + floorxpanning + " \r\n";
		out += "floorypanning " + floorypanning + " \r\n";
		out += "visibility " + visibility + " \r\n";
		out += "filler " + filler + " \r\n";
		out += "lotag " + lotag + " \r\n";
		out += "hitag " + hitag + " \r\n";
		out += "extra " + extra + " \r\n";
    	
		return out;
	}
}