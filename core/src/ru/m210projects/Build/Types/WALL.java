package ru.m210projects.Build.Types;

import static ru.m210projects.Build.Engine.MAXSECTORS;
import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.MAXWALLS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WALL {
	public static int sizeof = 32;

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
}
