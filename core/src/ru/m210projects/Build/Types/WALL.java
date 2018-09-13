/*
 *  Wall structure code originally written by Ken Silverman
 *	Ken Silverman's official web site: http://www.advsys.net/ken
 *
 *  See the included license file "BUILDLIC.TXT" for license info.
 *
 *  This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Engine.MAXSECTORS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXWALLS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WALL {
	public static final int sizeof = 32;

	public int x, y; //8
	public short point2, nextwall, nextsector, cstat; //8
	public short picnum, overpicnum; //4
	public byte shade; //1
	public short pal, xrepeat, yrepeat, xpanning, ypanning; //5
	public short lotag, hitag, extra; //6

	public WALL() {}
	
	public WALL(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	
    	buildWall(bb);
	}
	
	public void buildWall(ByteBuffer bb)
	{
		x = bb.getInt();
    	y = bb.getInt();
    	point2 = bb.getShort();
    	if(point2 < 0 || point2 >= MAXWALLS) point2 = 0;
    	nextwall = bb.getShort();
    	if(nextwall < 0 || nextwall >= MAXWALLS) nextwall = -1;
    	nextsector = bb.getShort();
    	if(nextsector < 0 || nextsector >= MAXSECTORS) nextsector = -1;
    	cstat = bb.getShort();
    	picnum = bb.getShort();
    	if(picnum < 0 || picnum >= MAXTILES) picnum = 0;
    	overpicnum = bb.getShort();
    	if(overpicnum < 0 || overpicnum >= MAXTILES) overpicnum = 0;
    	shade = bb.get();
    	pal = (short) (bb.get()&0xFF);
    	xrepeat = (short) (bb.get()&0xFF);
    	yrepeat = (short) (bb.get()&0xFF);
    	xpanning = (short) (bb.get()&0xFF);
    	ypanning = (short) (bb.get()&0xFF);
    	lotag = bb.getShort();
    	hitag = bb.getShort();
    	extra = bb.getShort();
	}
	
	public void set(WALL src) {
		x = src.x;
    	y = src.y;
    	point2 = src.point2;
    	nextwall = src.nextwall;
    	nextsector = src.nextsector;
    	cstat = src.cstat;
    	picnum = src.picnum;
    	overpicnum = src.overpicnum;
    	shade = src.shade;
    	pal = src.pal;
    	xrepeat = src.xrepeat;
    	yrepeat = src.yrepeat;
    	xpanning = src.xpanning;
    	ypanning = src.ypanning;
    	lotag = src.lotag;
    	hitag = src.hitag;
    	extra = src.extra;
	}
	
	private ByteBuffer buffer;
	public byte[] getBytes()
	{
		if(buffer == null) {
			buffer = ByteBuffer.allocate(sizeof); 
			buffer.order( ByteOrder.LITTLE_ENDIAN);
		}
		
		buffer.clear();
		buffer.putInt(this.x);
    	buffer.putInt(this.y);
    	buffer.putShort(this.point2);
    	buffer.putShort(this.nextwall);
    	buffer.putShort(this.nextsector);
    	buffer.putShort(this.cstat);
    	buffer.putShort(this.picnum);
    	buffer.putShort(this.overpicnum);
    	buffer.put(this.shade);
    	buffer.put((byte)this.pal);
    	buffer.put((byte)this.xrepeat);
    	buffer.put((byte)this.yrepeat);
    	buffer.put((byte)this.xpanning);
    	buffer.put((byte)this.ypanning);
    	buffer.putShort(this.lotag);
    	buffer.putShort(this.hitag);
    	buffer.putShort(this.extra);
		
    	return buffer.array();
	}

	public String toString()
	{
		String out = "x " + x + " \r\n";
		out += "y " + y + " \r\n";
		out += "point2 " + point2 + " \r\n";
		out += "nextwall " + nextwall + " \r\n";
		out += "nextsector " + nextsector + " \r\n";
		out += "cstat " + cstat + " \r\n";
		out += "picnum " + picnum + " \r\n";
		out += "overpicnum " + overpicnum + " \r\n";
		out += "shade " + shade + " \r\n";
		out += "pal " + pal + " \r\n";
		out += "xrepeat " + xrepeat + " \r\n";
		out += "yrepeat " + yrepeat + " \r\n";
		out += "xpanning " + xpanning + " \r\n";
		out += "ypanning " + ypanning + " \r\n";
		out += "type " + lotag + " \r\n";
		out += "flags " + hitag + " \r\n";
		out += "extra " + extra + " \r\n";

		return out;
	}
}
