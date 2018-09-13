// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been modified from Ken Silverman's original release
// by Jonathon Fowler (jf@jonof.id.au)
// by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.FileHandle.Cache1D.kClose;
import static ru.m210projects.Build.FileHandle.Cache1D.kOpen;
import static ru.m210projects.Build.FileHandle.Cache1D.kRead;
import static ru.m210projects.Build.Pragmas.scale;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import ru.m210projects.Build.Types.LittleEndian;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;

public class Board {

	private final Engine eng;
	protected final int mapversion;
	protected int numsectors, numwalls, numsprites;

	protected final short[] headspritesect, headspritestat;
	protected final short[] prevspritesect, prevspritestat;
	protected final short[] nextspritesect, nextspritestat;
	
	protected final SECTOR[] sector;
	protected final WALL[] wall;
	protected final SPRITE[] sprite;

	public int daposx, daposy, daposz;
	public short daang, dacursectnum;
	protected final String name;

	private final ByteBuffer buffer;

	//////////INITIALIZATION FUNCTIONS //////////
	
	protected void initspritelists()
	{
		Arrays.fill(headspritesect, (short) -1); //Init doubly-linked sprite sector lists
		headspritesect[MAXSECTORS] = 0;
		
		for(int i=0;i<MAXSPRITES;i++)
		{
			sprite[i] = new SPRITE();
			prevspritesect[i] = (short) (i-1);
			nextspritesect[i] = (short) (i+1);
			sprite[i].sectnum = (short) MAXSECTORS;
		}
		prevspritesect[0] = -1;
		nextspritesect[MAXSPRITES-1] = -1;

		Arrays.fill(headspritestat, (short) -1); //Init doubly-linked sprite status lists
		headspritestat[MAXSTATUS] = 0;
		for(int i=0;i<MAXSPRITES;i++)
		{
			prevspritestat[i] = (short) (i-1);
			nextspritestat[i] = (short) (i+1);
			sprite[i].statnum = (short) MAXSTATUS;
		}
		prevspritestat[0] = -1;
		nextspritestat[MAXSPRITES-1] = -1;
	}
	
	protected Board(Engine engine, String filename) throws Exception {
		this.eng = engine;
		
		int fil = kOpen(filename, 0);
		if (fil == -1) throw new Exception("Map file not found");

		this.name = filename;
		
		sector = new SECTOR[MAXSECTORS];
		wall = new WALL[MAXWALLS];
		sprite = new SPRITE[MAXSPRITES];
		
		headspritesect = new short[MAXSECTORS + 1]; 
		headspritestat = new short[MAXSTATUS + 1];
		prevspritesect = new short[MAXSPRITES]; 
		prevspritestat = new short[MAXSPRITES];
		nextspritesect = new short[MAXSPRITES]; 
		nextspritestat = new short[MAXSPRITES];
		
		initspritelists();
		
		mapversion = kRead(fil, 4);
		if(mapversion == 6) 
			loadv6(fil);
		else if(mapversion == 7)
			loadv7(fil);
		else {
			kClose(fil);
			throw new Exception("Invalid map version!");
		}

		kClose(fil);

		int size = 144 + 20 + 2 + (numwalls * WALL.sizeof) + 
		2 + (numsectors * SECTOR.sizeof) + 
		2 + (MAXSPRITES * SPRITE.sizeof) + 
		2 * (MAXSECTORS + 1) + 2 * (MAXSTATUS + 1) + 8 * MAXSPRITES; //MAXV7 = 520366 
		
		buffer = ByteBuffer.allocate(size); 
	}
	
	protected Board(Engine engine, byte[] buf) throws Exception {
		this.eng = engine;
		
		sector = new SECTOR[MAXSECTORS];
		wall = new WALL[MAXWALLS];
		sprite = new SPRITE[MAXSPRITES];
		
		headspritesect = new short[MAXSECTORS + 1]; 
		headspritestat = new short[MAXSTATUS + 1];
		prevspritesect = new short[MAXSPRITES]; 
		prevspritestat = new short[MAXSPRITES];
		nextspritesect = new short[MAXSPRITES]; 
		nextspritestat = new short[MAXSPRITES];
		
		ByteBuffer bb = ByteBuffer.wrap(buf, 144, buf.length);
		bb.order( ByteOrder.LITTLE_ENDIAN);
		
		this.name = new String(buf, 0, 144).trim();
		
		mapversion = bb.getInt();
		daposx = bb.getInt();
		daposy = bb.getInt();
		daposz = bb.getInt();
		daang = bb.getShort();
		dacursectnum = bb.getShort();

		numwalls = bb.getShort();
		for(int w = 0; w < numwalls; w++) {
			wall[w] = new WALL();
			wall[w].buildWall(bb);
		}

		numsectors = bb.getShort();
		for(int s = 0; s < numsectors; s++) {
			sector[s] = new SECTOR();
			sector[s].buildSector(bb);
		}
		
		numsprites = bb.getShort();
		for(int i = 0; i < MAXSPRITES; i++) {
			sprite[i] = new SPRITE();
			sprite[i].buildSprite(bb);
		}

		for(int i = 0; i <= MAXSECTORS; i++)
			headspritesect[i] = bb.getShort();
		for(int i = 0; i <= MAXSTATUS; i++)
			headspritestat[i] = bb.getShort();
		for(int i = 0; i < MAXSPRITES; i++) {
			prevspritesect[i] = bb.getShort();
			prevspritestat[i] = bb.getShort();
			nextspritesect[i] = bb.getShort();
			nextspritestat[i] = bb.getShort();
		}
		
		int size = 144 + 20 + 2 + (numwalls * WALL.sizeof) + 
		2 + (numsectors * SECTOR.sizeof) + 
		2 + (MAXSPRITES * SPRITE.sizeof) + 
		2 * (MAXSECTORS + 1) + 2 * (MAXSTATUS + 1) + 8 * MAXSPRITES;
		
		buffer = ByteBuffer.allocate(size); 
	}
	
	protected void loadv6(int fil)
	{
		byte[] buf = new byte[4];
		
		kRead(fil, buf, 4); daposx = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposy = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposz = LittleEndian.getInt(buf);
		kRead(fil, buf, 2); daang = LittleEndian.getShort(buf);
		kRead(fil, buf, 2); dacursectnum = LittleEndian.getShort(buf);
		
		int sizeof = 37;
		kRead(fil, buf, 2); numsectors = LittleEndian.getShort(buf);
		byte[] sectors = new byte[sizeof*numsectors];
		kRead(fil, sectors, sizeof* numsectors);
		ByteBuffer bb = ByteBuffer.wrap(sectors);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	
		for(int sectorid = 0; sectorid < numsectors; sectorid++) {
			SECTOR sec = new SECTOR();
			
			sec.wallptr = bb.getShort(0 + sizeof * sectorid);
			sec.wallnum = bb.getShort(2 + sizeof * sectorid);
			sec.ceilingpicnum = bb.getShort(4 + sizeof * sectorid);
			sec.floorpicnum = bb.getShort(6 + sizeof * sectorid);
			int ceilingheinum = bb.getShort(8 + sizeof * sectorid);
			sec.ceilingheinum = (short) max(min(ceilingheinum<<5,32767),-32768);
			int floorheinum = bb.getShort(10 + sizeof * sectorid);
			sec.floorheinum = (short) max(min(floorheinum<<5,32767),-32768);
			sec.ceilingz = bb.getInt(12 + sizeof * sectorid);
			sec.floorz = bb.getInt(16 + sizeof * sectorid);
			sec.ceilingshade = bb.get(20 + sizeof * sectorid);
			sec.floorshade = bb.get(21 + sizeof * sectorid);
			sec.ceilingxpanning = (short) (bb.get(22 + sizeof * sectorid) & 0xFF);
			sec.floorxpanning = (short) (bb.get(23 + sizeof * sectorid) & 0xFF);
			sec.ceilingypanning = (short) (bb.get(24 + sizeof * sectorid) & 0xFF);
			sec.floorypanning = (short) (bb.get(25 + sizeof * sectorid) & 0xFF);
			sec.ceilingstat = bb.get(26 + sizeof * sectorid);
			if ((sec.ceilingstat&2) == 0) sec.ceilingheinum = 0;
			sec.floorstat = bb.get(27 + sizeof * sectorid);
			if ((sec.floorstat&2) == 0) sec.floorheinum = 0;
			sec.ceilingpal = bb.get(28 + sizeof * sectorid);
			sec.floorpal = bb.get(29 + sizeof * sectorid);
			sec.visibility = bb.get(30 + sizeof * sectorid);
			sec.lotag = bb.getShort(31 + sizeof * sectorid);
			sec.hitag = bb.getShort(33 + sizeof * sectorid);
			sec.extra = bb.getShort(35 + sizeof * sectorid);
			
			sector[sectorid] = sec;
		}
		
		sizeof = WALL.sizeof;
		kRead(fil, buf, 2); numwalls = LittleEndian.getShort(buf);
		byte[] walls = new byte[sizeof * numwalls];
		kRead(fil, walls, sizeof * numwalls);
		bb = ByteBuffer.wrap(walls);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
		
		for(int wallid = 0; wallid < numwalls; wallid++) {
			WALL wal = new WALL();
			
			wal.x = bb.getInt(0 + sizeof * wallid);
			wal.y = bb.getInt(4 + sizeof * wallid);
			wal.point2 = bb.getShort(8 + sizeof * wallid);
			wal.nextsector = bb.getShort(10 + sizeof * wallid);
			wal.nextwall = bb.getShort(12 + sizeof * wallid);
			wal.picnum = bb.getShort(14 + sizeof * wallid);
			wal.overpicnum = bb.getShort(16 + sizeof * wallid);
			wal.shade = bb.get(18 + sizeof * wallid);
			wal.pal = (short) (bb.get(19 + sizeof * wallid)&0xFF);
			wal.cstat = bb.getShort(20 + sizeof * wallid);
			wal.xrepeat = (short) (bb.get(22 + sizeof * wallid) & 0xFF);
			wal.yrepeat = (short) (bb.get(23 + sizeof * wallid) & 0xFF);
			wal.xpanning = (short) (bb.get(24 + sizeof * wallid) & 0xFF);
			wal.ypanning = (short) (bb.get(25 + sizeof * wallid) & 0xFF);
			wal.lotag = bb.getShort(26 + sizeof * wallid);
			wal.hitag = bb.getShort(28 + sizeof * wallid);
			wal.extra = bb.getShort(30 + sizeof * wallid);
			
			wall[wallid] = wal;
		}

		sizeof = 43;
		kRead(fil, buf, 2); numsprites = LittleEndian.getShort(buf);
		byte[] sprites = new byte[sizeof*numsprites];
		kRead(fil, sprites, sizeof*numsprites);

		bb = ByteBuffer.wrap(sprites);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
		
		for(int spriteid = 0; spriteid < numsprites; spriteid++) {
			SPRITE spr = sprite[spriteid];
			
			spr.x = bb.getInt(0 + sizeof * spriteid);
			spr.y = bb.getInt(4 + sizeof * spriteid);
			spr.z = bb.getInt(8 + sizeof * spriteid);
			spr.cstat = bb.getShort(12 + sizeof * spriteid);
			spr.shade = bb.get(14 + sizeof * spriteid);
			spr.pal = bb.get(15 + sizeof * spriteid);
			spr.clipdist = bb.get(16 + sizeof * spriteid);
			spr.xrepeat = (short) (bb.get(17 + sizeof * spriteid) & 0xFF);
			spr.yrepeat = (short) (bb.get(18 + sizeof * spriteid) & 0xFF);
			spr.xoffset = (short) (bb.get(19 + sizeof * spriteid) & 0xFF);
			spr.yoffset = (short) (bb.get(20 + sizeof * spriteid) & 0xFF);
			spr.picnum = bb.getShort(21 + sizeof * spriteid);
			spr.ang = bb.getShort(23 + sizeof * spriteid);
			spr.xvel = bb.getShort(25 + sizeof * spriteid);
			spr.yvel = bb.getShort(27 + sizeof * spriteid);
			spr.zvel = bb.getShort(29 + sizeof * spriteid);
			spr.owner = bb.getShort(31 + sizeof * spriteid);
			spr.sectnum = bb.getShort(33 + sizeof * spriteid);
			spr.statnum = bb.getShort(35 + sizeof * spriteid);
			spr.lotag = bb.getShort(37 + sizeof * spriteid);
			spr.hitag = bb.getShort(39 + sizeof * spriteid);
			spr.extra = bb.getShort(41 + sizeof * spriteid);
		}

		for(int i=0;i<numsprites;i++) 
			insertsprite(sprite[i].sectnum, sprite[i].statnum);
	}
	
	protected void loadv7(int fil)
	{
		byte[] buf = new byte[4];
		kRead(fil, buf, 4); daposx = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposy = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposz = LittleEndian.getInt(buf);
		kRead(fil, buf, 2); daang = LittleEndian.getShort(buf);
		kRead(fil, buf, 2); dacursectnum = LittleEndian.getShort(buf);
		
		kRead(fil, buf, 2); numsectors = LittleEndian.getShort(buf);
		byte[] sectors = new byte[SECTOR.sizeof * numsectors];
		kRead(fil, sectors, sectors.length);
		ByteBuffer bb = ByteBuffer.wrap(sectors);
		byte[] sectorReader = new byte[SECTOR.sizeof];
		for (int i = 0; i < numsectors; i++) {
			bb.get(sectorReader);
			sector[i] = new SECTOR(sectorReader);
		}
		
		kRead(fil, buf, 2); numwalls = LittleEndian.getShort(buf);
		byte[] walls = new byte[WALL.sizeof * numwalls];
		kRead(fil, walls, walls.length);
		bb = ByteBuffer.wrap(walls);
		byte[] wallReader = new byte[WALL.sizeof];
		
		for(int w = 0; w < numwalls; w++) {
			bb.get(wallReader);
			wall[w] = new WALL(wallReader);
		}
		
		kRead(fil, buf, 2); numsprites = LittleEndian.getShort(buf);
		byte[] sprites = new byte[SPRITE.sizeof*numsprites];
		kRead(fil, sprites, SPRITE.sizeof*numsprites);
		bb = ByteBuffer.wrap(sprites);
		byte[] spriteReader = new byte[SPRITE.sizeof];
		for(int s = 0; s < numsprites; s++) {
			bb.get(spriteReader);
			sprite[s].init(spriteReader);
		}

		for(int i=0;i<numsprites;i++) 
			insertsprite(sprite[i].sectnum, sprite[i].statnum);
	}
	
	//////////SPRITE LIST MANIPULATION FUNCTIONS //////////
	
	public short insertspritesect(short sectnum)
	{
		if ((sectnum >= MAXSECTORS) || (headspritesect[MAXSECTORS] == -1))
			return(-1);  //list full
	
		short blanktouse = headspritesect[MAXSECTORS];
	
		headspritesect[MAXSECTORS] = nextspritesect[blanktouse];
		if (headspritesect[MAXSECTORS] >= 0)
			prevspritesect[headspritesect[MAXSECTORS]] = -1;
	
		prevspritesect[blanktouse] = -1;
		nextspritesect[blanktouse] = headspritesect[sectnum];
		if (headspritesect[sectnum] >= 0)
			prevspritesect[headspritesect[sectnum]] = blanktouse;
		headspritesect[sectnum] = blanktouse;
	
		sprite[blanktouse].sectnum = sectnum;
	
		return(blanktouse);
	}
	
	public short insertspritestat(short newstatnum)
	{
		if ((newstatnum >= MAXSTATUS) || (headspritestat[MAXSTATUS] == -1))
			return(-1);  //list full
	
		short blanktouse = headspritestat[MAXSTATUS];
	
		headspritestat[MAXSTATUS] = nextspritestat[blanktouse];
		if (headspritestat[MAXSTATUS] >= 0)
			prevspritestat[headspritestat[MAXSTATUS]] = -1;
	
		prevspritestat[blanktouse] = -1;
		nextspritestat[blanktouse] = headspritestat[newstatnum];
		if (headspritestat[newstatnum] >= 0)
			prevspritestat[headspritestat[newstatnum]] = blanktouse;
		headspritestat[newstatnum] = blanktouse;
	
		sprite[blanktouse].statnum = newstatnum;
	
		return(blanktouse);
	}
	
	public short insertsprite(short sectnum, short statnum)
	{
		insertspritestat(statnum);
		return(insertspritesect(sectnum));
	}
	
	public boolean deletesprite(short spritenum)
	{
		deletespritestat(spritenum);
		return(deletespritesect(spritenum));
	}
	
	public boolean changespritesect(short spritenum, short newsectnum)
	{
		if ((newsectnum < 0) || (newsectnum > MAXSECTORS)) return false;
		if (sprite[spritenum].sectnum == newsectnum) return true;
		if (sprite[spritenum].sectnum == MAXSECTORS) return false;
		if (!deletespritesect(spritenum)) return false;
		insertspritesect(newsectnum);
		return true;
	}
	
	public boolean changespritestat(short spritenum, short newstatnum)
	{
		if ((newstatnum < 0) || (newstatnum > MAXSTATUS)) return false;
		if (sprite[spritenum].statnum == newstatnum) return true;
		if (sprite[spritenum].statnum == MAXSTATUS) return false;
		if (!deletespritestat(spritenum)) return false;
		insertspritestat(newstatnum);
		return true;
	}
	
	public boolean deletespritesect(short spritenum)
	{
		if (sprite[spritenum].sectnum == MAXSECTORS)
			return false;
	
		if (headspritesect[sprite[spritenum].sectnum] == spritenum)
			headspritesect[sprite[spritenum].sectnum] = nextspritesect[spritenum];
	
		if (prevspritesect[spritenum] >= 0) nextspritesect[prevspritesect[spritenum]] = nextspritesect[spritenum];
		if (nextspritesect[spritenum] >= 0) prevspritesect[nextspritesect[spritenum]] = prevspritesect[spritenum];
	
		if (headspritesect[MAXSECTORS] >= 0) prevspritesect[headspritesect[MAXSECTORS]] = spritenum;
		prevspritesect[spritenum] = -1;
		nextspritesect[spritenum] = headspritesect[MAXSECTORS];
		headspritesect[MAXSECTORS] = spritenum;
	
		sprite[spritenum].sectnum = (short) MAXSECTORS;
		return true;
	}
	
	public boolean deletespritestat (short spritenum)
	{
		if (sprite[spritenum].statnum == MAXSTATUS)
			return false;
	
		if (headspritestat[sprite[spritenum].statnum] == spritenum)
			headspritestat[sprite[spritenum].statnum] = nextspritestat[spritenum];
	
		if (prevspritestat[spritenum] >= 0) nextspritestat[prevspritestat[spritenum]] = nextspritestat[spritenum];
		if (nextspritestat[spritenum] >= 0) prevspritestat[nextspritestat[spritenum]] = prevspritestat[spritenum];
	
		if (headspritestat[MAXSTATUS] >= 0) prevspritestat[headspritestat[MAXSTATUS]] = spritenum;
		prevspritestat[spritenum] = -1;
		nextspritestat[spritenum] = headspritestat[MAXSTATUS];
		headspritestat[MAXSTATUS] = spritenum;
	
		sprite[spritenum].statnum = MAXSTATUS;
		return true;
	}

	//////////WALL LIST MANIPULATION FUNCTIONS //////////
	
	public void dragpoint(short pointhighlight, int dax, int day) { 
		wall[pointhighlight].x = dax;
		wall[pointhighlight].y = day;

		int cnt = MAXWALLS;
		short tempshort = pointhighlight; //search points CCW
		do {
			if (wall[tempshort].nextwall >= 0) {
				tempshort = wall[wall[tempshort].nextwall].point2;
				wall[tempshort].x = dax;
				wall[tempshort].y = day;
			} else {
				tempshort = pointhighlight; //search points CW if not searched all the way around
				do {
					if (wall[lastwall(tempshort)].nextwall >= 0) {
						tempshort = wall[lastwall(tempshort)].nextwall;
						wall[tempshort].x = dax;
						wall[tempshort].y = day;
					} else {
						break;
					}
					cnt--;
				} while ((tempshort != pointhighlight) && (cnt > 0));
				break;
			}
			cnt--;
		} while ((tempshort != pointhighlight) && (cnt > 0));
	}
	
	protected int lastwall(int point) {
		int i, j, cnt;

		if ((point > 0) && (wall[point - 1].point2 == point))
			return (point - 1);
		i = point;
		cnt = MAXWALLS;
		do {
			j = wall[i].point2;
			if (j == point)
				return (i);
			i = j;
			cnt--;
		} while (cnt > 0);
		return (point);
	}

	public void setfirstwall(short sectnum, short newfirstwall) {
		short startwall = sector[sectnum].wallptr;
		int danumwalls = sector[sectnum].wallnum;
		int endwall = startwall + danumwalls;
		if ((newfirstwall < startwall) || (newfirstwall >= startwall + danumwalls))
			return;
		for (int i = 0; i < danumwalls; i++) {
			if (wall[i + numwalls] == null)
				wall[i + numwalls] = new WALL();
			wall[i + numwalls].set(wall[i + startwall]);
		}

		int numwallsofloop = 0, k;
		short i = newfirstwall;
		do {
			numwallsofloop++;
			i = wall[i].point2;
		} while (i != newfirstwall);

		//Put correct loop at beginning
		int dagoalloop = loopnumofsector(sectnum, newfirstwall);
		if (dagoalloop > 0) {
			int j = 0;
			while (loopnumofsector(sectnum, (short) (j + startwall)) != dagoalloop)
				j++;
			for (i = 0; i < danumwalls; i++) {
				k = i + j;
				if (k >= danumwalls)
					k -= danumwalls;
				if (wall[startwall + i] == null)
					wall[startwall + i] = new WALL();
				wall[startwall + i].set(wall[numwalls + k]);

				wall[startwall + i].point2 += danumwalls - startwall - j;
				if (wall[startwall + i].point2 >= danumwalls)
					wall[startwall + i].point2 -= danumwalls;
				wall[startwall + i].point2 += startwall;
			}
			newfirstwall += danumwalls - j;
			if (newfirstwall >= startwall + danumwalls)
				newfirstwall -= danumwalls;
		}

		for (i = 0; i < numwallsofloop; i++) {
			if (wall[i + numwalls] == null)
				wall[i + numwalls] = new WALL();
			wall[i + numwalls].set(wall[i + startwall]);
		}
		for (i = 0; i < numwallsofloop; i++) {
			k = i + newfirstwall - startwall;
			if (k >= numwallsofloop)
				k -= numwallsofloop;
			if (wall[startwall + i] == null)
				wall[startwall + i] = new WALL();
			wall[startwall + i].set(wall[numwalls + k]);

			wall[startwall + i].point2 += numwallsofloop - newfirstwall;
			if (wall[startwall + i].point2 >= numwallsofloop)
				wall[startwall + i].point2 -= numwallsofloop;
			wall[startwall + i].point2 += startwall;
		}

		for (i = startwall; i < endwall; i++)
			if (wall[i].nextwall >= 0)
				wall[wall[i].nextwall].nextwall = i;
	}

	private int loopnumofsector(short sectnum, short wallnum) { 
		int numloops = 0;
		int startwall = sector[sectnum].wallptr;
		int endwall = startwall + sector[sectnum].wallnum;
		for (int i = startwall; i < endwall; i++) {
			if (i == wallnum)
				return (numloops);
			if (wall[i].point2 < i)
				numloops++;
		}
		return (-1);
	}
	
	public void flipwalls(int numwalls, int newnumwalls) {
		int i, j, nume, tempint;

		nume = newnumwalls - numwalls;

		for (i = numwalls; i < numwalls + (nume >> 1); i++) {
			j = numwalls + newnumwalls - i - 1;
			tempint = wall[i].x;
			wall[i].x = wall[j].x;
			wall[j].x = tempint;
			tempint = wall[i].y;
			wall[i].y = wall[j].y;
			wall[j].y = tempint;
		}
	}
	
	//////////SECTOR LIST MANIPULATION FUNCTIONS //////////

	public void alignceilslope(short dasect, int x, int y, int z) { 
		WALL wal = wall[sector[dasect].wallptr];
		int dax = wall[wal.point2].x - wal.x;
		int day = wall[wal.point2].y - wal.y;

		int i = (y - wal.y) * dax - (x - wal.x) * day;
		if (i == 0) return;
		sector[dasect].ceilingheinum = (short) scale((z - sector[dasect].ceilingz) << 8, eng.ksqrt(dax * dax + day * day), i);

		if (sector[dasect].ceilingheinum == 0)
			sector[dasect].ceilingstat &= ~2;
		else
			sector[dasect].ceilingstat |= 2;
	}

	public void alignflorslope(short dasect, int x, int y, int z) {
		WALL wal = wall[sector[dasect].wallptr];
		int dax = wall[wal.point2].x - wal.x;
		int day = wall[wal.point2].y - wal.y;

		int i = (y - wal.y) * dax - (x - wal.x) * day;
		if (i == 0)
			return;
		sector[dasect].floorheinum = (short) scale((z - sector[dasect].floorz) << 8, eng.ksqrt(dax * dax + day * day), i);

		if (sector[dasect].floorheinum == 0)
			sector[dasect].floorstat &= ~2;
		else
			sector[dasect].floorstat |= 2;
	}

	public byte[] getBytes()
	{
		buffer.clear();
		buffer.put(name.getBytes(), 0, Math.max(name.length(), 144));
		buffer.position(144);

		buffer.putInt(mapversion);
		buffer.putInt(daposx);
		buffer.putInt(daposy);
		buffer.putInt(daposz);
		buffer.putShort(daang);
		buffer.putShort(dacursectnum);

		buffer.putShort((short) numwalls);
		for(int w = 0; w < numwalls; w++)
			buffer.put(wall[w].getBytes());
		
		buffer.putShort((short) numsectors);
		for(int s = 0; s < numsectors; s++)
			buffer.put(sector[s].getBytes());
		
		buffer.putShort((short) numsprites);
		for(int i = 0; i < MAXSPRITES; i++)
			buffer.put(sprite[i].getBytes());

		for(int i = 0; i <= MAXSECTORS; i++) 
			buffer.putShort(headspritesect[i]);
		for(int i = 0; i <= MAXSTATUS; i++)
			buffer.putShort(headspritestat[i]);
		for(int i = 0; i < MAXSPRITES; i++) {
			buffer.putShort(prevspritesect[i]);
			buffer.putShort(prevspritestat[i]);
			buffer.putShort(nextspritesect[i]);
			buffer.putShort(nextspritestat[i]);
		}
		
		return buffer.array();
	}
	
	public String getName()
	{
		return name;
	}
}
