package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXWALLS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SECTOR {
	public static int sizeof = 40;

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
	public SECTOR() { }
	
	public SECTOR(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	
    	buildSector(bb);
	}
	
	public void buildSector(ByteBuffer bb)
	{
		wallptr = bb.getShort();
		if(wallptr < 0 || wallptr >= MAXWALLS) wallptr = 0;
    	wallnum = bb.getShort();
    	ceilingz = bb.getInt();
    	floorz = bb.getInt();
    	ceilingstat = bb.getShort();
    	floorstat = bb.getShort();
    	ceilingpicnum = bb.getShort();
    	if(ceilingpicnum < 0 || ceilingpicnum >= MAXTILES) ceilingpicnum = 0;
    	ceilingheinum = bb.getShort();
    	ceilingshade = bb.get();
    	ceilingpal = (short) (bb.get()&0xFF);
    	ceilingxpanning = (short) (bb.get()&0xFF);
    	ceilingypanning = (short) (bb.get()&0xFF);
    	floorpicnum = bb.getShort();
    	if(floorpicnum < 0 || floorpicnum >= MAXTILES) floorpicnum = 0;
    	floorheinum = bb.getShort();
    	floorshade = bb.get();
    	floorpal = (short) (bb.get()&0xFF);
    	floorxpanning = (short) (bb.get()&0xFF);
    	floorypanning = (short) (bb.get()&0xFF);
    	visibility = (short) (bb.get()&0xFF);
    	filler = bb.get();
      	lotag = bb.getShort();
    	hitag = bb.getShort();
    	extra = bb.getShort();
	}
	
	public byte[] getBytes()
	{
		ByteBuffer buffer = ByteBuffer.allocate(sizeof); 
		buffer.order(ByteOrder.LITTLE_ENDIAN);  
		
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
}