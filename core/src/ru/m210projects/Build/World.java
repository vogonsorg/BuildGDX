// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been modified from Ken Silverman's original release
// by Jonathon Fowler (jf@jonof.id.au)
// by the EDuke32 team (development@voidpoint.com)
// by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build;

import static ru.m210projects.Build.Engine.MAXSECTORS;
import static ru.m210projects.Build.Engine.MAXWALLS;
import static ru.m210projects.Build.Engine.MAXSPRITES;
import static ru.m210projects.Build.Engine.picanm;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.sintable;
import static ru.m210projects.Build.Gameutils.BClampAngle;
import static ru.m210projects.Build.Pragmas.*;

import java.util.Arrays;

import ru.m210projects.Build.Types.Hitscan;
import ru.m210projects.Build.Types.Neartag;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;

public class World {
	
	public static final byte CEIL = 0;
	public static final byte FLOOR = 1;
	
	public int SETSPRITEZ = 0;
	public int hitallsprites = 0;
	
	protected class Line {
		public int x1, y1, x2, y2;
	}
	
	protected class Clip {
		private int x, y, z;
		private short num;
		
		public int getX() { return x; }
		public int getY() { return y; }
		public int getZ() { return z; }
		public short getNum() { return num; }
		
		public Clip set(int x, int y, int z, short num)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.num = num;
			
			return this;
		}
	}
	
	protected class Point {
		private int x, y, z;
		
		public int getX() { return x; }
		public int getY() { return y; }
		public int getZ() { return z; }
		
		public Point set(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			
			return this;
		}
	}

	public int hitscangoalx = (1 << 29) - 1, hitscangoaly = (1 << 29) - 1;
	
	public boolean inpreparemirror = false;
	public int mirrorx, mirrory;
	public float mirrorang;

	private byte[] sectbitmap; //XXX
	
	protected final int MAXCLIPNUM = 1024;
	protected final int MAXCLIPDIST = 1024;
	public int clipmoveboxtracenum = 3;
	private int[] rxi;
	private int[] ryi;
	private int[] hitwalls;
	private short clipnum;
	private Line[] clipit;
	private short[] clipsectorlist;
	private short clipsectnum;
	private int[] clipobjectval;
	
	private Point intersect;
	private Point keep;
	private Clip ray;
	
	public byte[] show2dsector;
	public byte[] show2dwall;
	public byte[] show2dsprite;
	
	private final int[] zofslope;
	
	protected Engine eng;
	protected Board map;
	
	public World(Engine engine)
	{
		this.eng = engine;
		zofslope = new int[2];
		
		clipit = new Line[MAXCLIPNUM];
		clipsectorlist = new short[MAXCLIPNUM];
		clipobjectval = new int[MAXCLIPNUM];
		for(int i = 0; i < MAXCLIPNUM; i++)
			clipit[i] = new Line();
		
		hitwalls = new int[clipmoveboxtracenum + 1];
		rxi = new int[4];
		ryi = new int[4];
		
		sectbitmap = new byte[MAXSECTORS >> 3];
		show2dsector = new byte[(MAXSECTORS + 7) >> 3];
		show2dwall = new byte[(MAXWALLS + 7) >> 3];
		show2dsprite = new byte[(MAXSPRITES + 7) >> 3];
		
		keep = new Point();
		intersect = new Point();
		ray = new Clip();
	}
	
	public Board getMap() {
		return map;
	}
	
	public void changeMap(Board map)
	{
		this.map = map;
	}

	public Board loadboard(String filename) throws Exception
	{
		map = new Board(eng, filename);
		map.dacursectnum = updatesector(map.daposx, map.daposy, map.dacursectnum);
		
		Arrays.fill(show2dsector, (byte)0);
		Arrays.fill(show2dsprite, (byte)0);
		Arrays.fill(show2dwall, (byte)0);
		
		return map;
	}
	
	public Board loadboard(byte[] data) throws Exception
	{
		Arrays.fill(show2dsector, (byte)0);
		Arrays.fill(show2dsprite, (byte)0);
		Arrays.fill(show2dwall, (byte)0);
		
		return (map = new Board(eng, data));
	}
	
	public short updatesector(int x, int y, short sectnum) {
		if (inside(x, y, sectnum) == 1)
			return sectnum;

		if ((sectnum >= 0) && (sectnum < map.numsectors)) {
			short wallid = map.sector[sectnum].wallptr, i;
			int j = map.sector[sectnum].wallnum;
			if(wallid < 0) return -1;
			do {
				if(wallid >= MAXWALLS) break;
				WALL wal = map.wall[wallid];
				if(wal == null) { wallid++; j--; continue; }
				i = wal.nextsector;
				if (i >= 0)
					if (inside(x, y, i) == 1) {
						return i;
					}
				wallid++;
				j--;
			} while (j != 0);
		}

		for (short i = (short) (map.numsectors - 1); i >= 0; i--)
			if (inside(x, y, i) == 1) {
				return i;
			}

		return -1;
	}

	public short updatesectorz(int x, int y, int z, short sectnum) {
		getzsofslope(sectnum, x, y, zofslope);
		if ((z >= zofslope[CEIL]) && (z <= zofslope[FLOOR]))
			if (inside(x, y, sectnum) != 0)
				return sectnum;

		if ((sectnum >= 0) && (sectnum < map.numsectors)) {
			if(map.sector[sectnum] == null) return -1;
			short wallid = map.sector[sectnum].wallptr, i;
			int j = map.sector[sectnum].wallnum;
			do {
				if(wallid >= MAXWALLS) break;
				WALL wal = map.wall[wallid];
				if(wal == null) { wallid++; j--; continue; }
				i = wal.nextsector;
				if (i >= 0) {
					getzsofslope(i, x, y, zofslope);
					if ((z >= zofslope[CEIL]) && (z <= zofslope[FLOOR]))
						if (inside(x, y, i) == 1) {
							return i;
						}
				}
				wallid++;
				j--;
			} while (j != 0);
		}

		for (short i = (short) (map.numsectors - 1); i >= 0; i--) {
			getzsofslope( i, x, y, zofslope);
			if ((z >= zofslope[CEIL]) && (z <= zofslope[FLOOR]))
				if (inside(x, y, i) == 1) {
					return i;
				}
		}

		return -1;
	}
	
	public boolean setsprite(short spritenum, int newx, int newy, int newz) 
	{
		map.sprite[spritenum].x = newx;
		map.sprite[spritenum].y = newy;
		map.sprite[spritenum].z = newz;

		short tempsectnum = map.sprite[spritenum].sectnum;
		if(SETSPRITEZ == 1)
			tempsectnum = updatesectorz(newx,newy,newz,tempsectnum);
		else
			tempsectnum = updatesector(newx,newy,tempsectnum);
		if (tempsectnum < 0) return false;
		if (tempsectnum != map.sprite[spritenum].sectnum)
			map.changespritesect(spritenum,tempsectnum);

		return true;
	}
	
	public int getceilzofslope(short sectnum, int dax, int day) { 
		if(sectnum == -1 || map.sector[sectnum] == null) return 0;
		if ((map.sector[sectnum].ceilingstat & 2) == 0)
			return (map.sector[sectnum].ceilingz);

		WALL wal = map.wall[map.sector[sectnum].wallptr];
		int dx = map.wall[wal.point2].x - wal.x;
		int dy = map.wall[wal.point2].y - wal.y;
		int i = (eng.ksqrt(dx * dx + dy * dy) << 5);
		if (i == 0) return (map.sector[sectnum].ceilingz);
		long j = dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);
		
		return map.sector[sectnum].ceilingz + (scale(map.sector[sectnum].ceilingheinum, j, i));
	}

	public int getflorzofslope(short sectnum, int dax, int day) { 
		if(sectnum == -1 || map.sector[sectnum] == null) return 0;
		if ((map.sector[sectnum].floorstat & 2) == 0)
			return (map.sector[sectnum].floorz);

		WALL wal = map.wall[map.sector[sectnum].wallptr];
		int dx = map.wall[wal.point2].x - wal.x;
		int dy = map.wall[wal.point2].y - wal.y;
		int i = eng.ksqrt(dx * dx + dy * dy) << 5;
		if (i == 0) return (map.sector[sectnum].floorz);
		long j = dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);
		return map.sector[sectnum].floorz + (scale(map.sector[sectnum].floorheinum, j, i));
	}

	public int inside(int x, int y, short sectnum) {
		if ((sectnum < 0) || (sectnum >= map.numsectors))
			return (-1);
	
		int cnt = 0;
		int wallid = map.sector[sectnum].wallptr;
		if(wallid < 0) return -1;
		int i = map.sector[sectnum].wallnum;
		int x1, y1, x2, y2;
		
		do {
			WALL wal = map.wall[wallid];
			if (wal == null || wal.point2 < 0 || map.wall[wal.point2] == null)
				return -1;
			y1 = wal.y - y;
			y2 = map.wall[wal.point2].y - y;
	
			if ((y1 ^ y2) < 0) {
				x1 = wal.x - x;
				x2 = map.wall[wal.point2].x - x;
				if ((x1 ^ x2) >= 0)
					cnt ^= x1;
				else
					cnt ^= (x1 * y2 - x2 * y1) ^ y2;
			}
			wallid++;
			i--;
		} while (i != 0);
	
		return (cnt >>> 31);
	}
	
	public void getzsofslope(short sectnum, int dax, int day, int[] outz) {
		if(sectnum == -1 || map.sector[sectnum] == null) 
			return;
	
		SECTOR sec = map.sector[sectnum];
		if(sec == null) return;
		outz[CEIL] = sec.ceilingz;
		outz[FLOOR] = sec.floorz;
		if (((sec.ceilingstat | sec.floorstat) & 2) != 0) {
			WALL wal = map.wall[sec.wallptr];
			WALL wal2 = map.wall[wal.point2];
			int dx = wal2.x - wal.x;
			int dy = wal2.y - wal.y;
			int i = (eng.ksqrt(dx * dx + dy * dy) << 5);
			if (i == 0) return;
			long j = dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);
	
			if ((sec.ceilingstat & 2) != 0)
				outz[CEIL] += scale(sec.ceilingheinum, j, i);
			if ((sec.floorstat & 2) != 0)
				outz[FLOOR] += scale(sec.floorheinum, j, i);
		}
	}
	
	public int clipinsidebox(int x, int y, short wallnum, int walldist) {
		WALL wal;
		int x1, y1, x2, y2, r;

		r = (walldist << 1);
		wal = map.wall[wallnum];
		if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) return 0;
		x1 = wal.x + walldist - x;
		y1 = wal.y + walldist - y;
		wal = map.wall[wal.point2];
		if(wal == null) return 0;
		x2 = wal.x + walldist - x;
		y2 = wal.y + walldist - y;

		if ((x1 < 0) && (x2 < 0))
			return (0);
		if ((y1 < 0) && (y2 < 0))
			return (0);
		if ((x1 >= r) && (x2 >= r))
			return (0);
		if ((y1 >= r) && (y2 >= r))
			return (0);

		x2 -= x1;
		y2 -= y1;
		if (x2 * (walldist - y1) >= y2 * (walldist - x1)) //Front
		{
			if (x2 > 0)
				x2 *= (0 - y1);
			else
				x2 *= (r - y1);
			if (y2 > 0)
				y2 *= (r - x1);
			else
				y2 *= (0 - x1);
			return (x2 < y2 ? 1 : 0);
		}
		if (x2 > 0)
			x2 *= (r - y1);
		else
			x2 *= (0 - y1);
		if (y2 > 0)
			y2 *= (0 - x1);
		else
			y2 *= (r - x1);
		return ((x2 >= y2 ? 1 : 0) << 1);
	}

	public int clipinsideboxline(int x, int y, int x1, int y1, int x2, int y2, int walldist) { 
		int r = walldist << 1;

		x1 += walldist - x;
		x2 += walldist - x;

		if (((x1 < 0) && (x2 < 0)) || ((x1 >= r) && (x2 >= r)))
			return 0;

		y1 += walldist - y;
		y2 += walldist - y;

		if (((y1 < 0) && (y2 < 0)) || ((y1 >= r) && (y2 >= r)))
			return 0;

		x2 -= x1;
		y2 -= y1;

		if (x2 * (walldist - y1) >= y2 * (walldist - x1)) // Front
		{
			x2 *= ((x2 > 0) ? (0 - y1) : (r - y1));
			y2 *= ((y2 > 0) ? (r - x1) : (0 - x1));
			return x2 < y2 ? 1 : 0;
		}

		x2 *= ((x2 > 0) ? (r - y1) : (0 - y1));
		y2 *= ((y2 > 0) ? (0 - x1) : (r - x1));
		return (x2 >= y2 ? 1 : 0) << 1;
	}
	
	public int nextsectorneighborz(int sectnum, int thez, int topbottom, int direction) {
		WALL wal;
		int i, testz, nextz;
		short sectortouse;

		if (direction == 1)
			nextz = 0x7fffffff;
		else
			nextz = 0x80000000;

		sectortouse = -1;

		int wallid = map.sector[sectnum].wallptr;
		i = map.sector[sectnum].wallnum;
		do {
			wal = map.wall[wallid];
			if (wal.nextsector >= 0) {
				if (topbottom == 1) {
					testz = map.sector[wal.nextsector].floorz;
					if (direction == 1) {
						if ((testz > thez) && (testz < nextz)) {
							nextz = testz;
							sectortouse = wal.nextsector;
						}
					} else {
						if ((testz < thez) && (testz > nextz)) {
							nextz = testz;
							sectortouse = wal.nextsector;
						}
					}
				} else {
					testz = map.sector[wal.nextsector].ceilingz;
					if (direction == 1) {
						if ((testz > thez) && (testz < nextz)) {
							nextz = testz;
							sectortouse = wal.nextsector;
						}
					} else {
						if ((testz < thez) && (testz > nextz)) {
							nextz = testz;
							sectortouse = wal.nextsector;
						}
					}
				}
			}
			wallid++;
			i--;
		} while (i != 0);

		return (sectortouse);
	}

	public int cansee(int x1, int y1, int z1, short sect1, int x2, int y2, int z2, short sect2) { //eduke32 sectbitmap
		SECTOR sec;
		WALL wal, wal2;
		int nexts, x, y, z, dasectnum, dacnt, danum;
		int x21, y21, z21, x31, y31, x34, y34, bot, t;

		Arrays.fill(sectbitmap, (byte) 0);
		
		if(sect1 < 0 || sect1 >= MAXSECTORS) return 0;
		if(sect2 < 0 || sect2 >= MAXSECTORS) return 0;

		if ((x1 == x2) && (y1 == y2))
			return (sect1 == sect2 ? 1 : 0);

		x21 = x2 - x1;
		y21 = y2 - y1;
		z21 = z2 - z1;

		sectbitmap[sect1 >> 3] |= (1 << (sect1 & 7));
		clipsectorlist[0] = sect1;
		danum = 1;

		for (dacnt = 0; dacnt < danum; dacnt++) {
			dasectnum = clipsectorlist[dacnt];
			sec = map.sector[dasectnum];

			if(sec == null) continue;
			int startwall = sec.wallptr;
			int endwall = startwall + sec.wallnum - 1;
			if(startwall < 0 || endwall < 0) continue;
			for (int w = startwall; w <= endwall; w++) {
				wal = map.wall[w];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = map.wall[wal.point2];
				if(wal2 == null) continue;
				x31 = wal.x - x1;
				x34 = wal.x - wal2.x;
				y31 = wal.y - y1;
				y34 = wal.y - wal2.y;

				bot = y21 * x34 - x21 * y34;
				if (bot <= 0)
					continue;
				t = y21 * x31 - x21 * y31;
				if ((t & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
					continue;
				t = y31 * x34 - x31 * y34;
				if ((t & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
					continue;

				nexts = wal.nextsector;
				if ((nexts < 0) || ((wal.cstat & 32) != 0))
					return (0);

				t = (int) divscale(t, bot, 24);
				x = x1 + mulscale(x21, t, 24);
				y = y1 + mulscale(y21, t, 24);
				z = z1 + mulscale(z21, t, 24);

				getzsofslope((short)dasectnum, x, y, zofslope);
				if ((z <= zofslope[CEIL]) || (z >= zofslope[FLOOR]))
					return (0);
				getzsofslope((short)nexts, x, y, zofslope);
				if ((z <= zofslope[CEIL]) || (z >= zofslope[FLOOR]))
					return (0);

				//				for(i=danum-1;i>=0;i--) if (clipsectorlist[i] == nexts) break;
				//				if (i < 0) clipsectorlist[danum++] = (short) nexts;

				if ((sectbitmap[nexts >> 3] & (1 << (nexts & 7))) == 0) {
					sectbitmap[nexts >> 3] |= (1 << (nexts & 7));
					clipsectorlist[danum++] = (short) nexts;
				}
			}
		}
		//		for(i=danum-1;i>=0;i--) if (clipsectorlist[i] == sect2) return(1);
		if ((sectbitmap[sect2 >> 3] & (1 << (sect2 & 7))) != 0)
			return 1;

		return (0);
	}

	public Point lintersect(int x1, int y1, int z1, int x2, int y2, int z2, int x3,
			int y3, int x4, int y4) {
		
		// p1 to p2 is a line segment
		int x21 = x2 - x1, x34 = x3 - x4;
	    int y21 = y2 - y1, y34 = y3 - y4;
	    int bot = x21 * y34 - y21 * x34;
	    
	    if (bot == 0) return null;
	    
	    int x31 = x3 - x1, y31 = y3 - y1;
	    int topt = x31 * y34 - y31 * x34;

		if (bot > 0) {
			if ((topt < 0) || (topt >= bot))
				return null;
			int topu = x21 * y31 - y21 * x31;
			if ((topu < 0) || (topu >= bot))
				return null;
		} else {
			if ((topt > 0) || (topt <= bot))
				return null;
			int topu = x21 * y31 - y21 * x31;
			if ((topu > 0) || (topu <= bot))
				return null;
		}
		long t = divscale(topt, bot, 24);

		intersect.x = x1 + mulscale(x21, t, 24);
		intersect.y = y1 + mulscale(y21, t, 24);
		intersect.z = z1 + mulscale(z2 - z1, t, 24);

		return intersect;
	}
	
	public Point rintersect(int x1, int y1, int z1, int vx, int vy, int vz, int x3,
			int y3, int x4, int y4) { //p1 towards p2 is a ray
		int x34, y34, x31, y31, bot, topt, topu;

		x34 = x3 - x4;
		y34 = y3 - y4;
		bot = vx * y34 - vy * x34;
		if (bot == 0) return null;
		
		if (bot > 0) {
			
			x31 = x3 - x1;
			y31 = y3 - y1;
			topt = x31 * y34 - y31 * x34;
			if (topt < 0) return null;
			topu = vx * y31 - vy * x31;
			if ((topu < 0) || (topu >= bot)) 
				return null;
		} else {
			x31 = x3 - x1;
			y31 = y3 - y1;
			topt = x31 * y34 - y31 * x34;
			if (topt > 0) return null;
			topu = vx * y31 - vy * x31;
			if ((topu > 0) || (topu <= bot))
				return null;
		}
		
		long t = divscale(topt, bot, 16);
		intersect.x = x1 + mulscale(vx, t, 16);
		intersect.y = y1 + mulscale(vy, t, 16);
		intersect.z = z1 + mulscale(vz, t, 16);
		
		return intersect;
	}

	private Clip raytrace(int x3, int y3, int x4, int y4) {
		int x1, y1, x2, y2, bot, topu, nintx, ninty, cnt;
		int x21, y21, x43, y43;

		int rayx = x4;
		int rayy = y4;
		short hitwall = -1;
		
		for (short z = (short) (clipnum - 1); z >= 0; z--) {
			x1 = clipit[z].x1;
			x2 = clipit[z].x2;
			x21 = x2 - x1;
			y1 = clipit[z].y1;
			y2 = clipit[z].y2;
			y21 = y2 - y1;

			topu = x21 * (y3 - y1) - (x3 - x1) * y21;
			if (topu <= 0)
				continue;
			if (x21 * (rayy - y1) > (rayx - x1) * y21)
				continue;
			x43 = rayx - x3;
			y43 = rayy - y3;
			if (x43 * (y1 - y3) > (x1 - x3) * y43)
				continue;
			if (x43 * (y2 - y3) <= (x2 - x3) * y43)
				continue;
			bot = x43 * y21 - x21 * y43;
			if (bot == 0)
				continue;

			cnt = 256;
			do {
				cnt--;
				if (cnt < 0) {
					rayx = x3;
					rayy = y3;
					return ray.set(rayx, rayy, 0, hitwall);
				}
				nintx = x3 + scale(x43, topu, bot);
				ninty = y3 + scale(y43, topu, bot);
				topu--;
			} while (x21 * (ninty - y1) <= (nintx - x1) * y21);

			if (klabs(x3 - nintx) + klabs(y3 - ninty) < klabs(x3 - rayx) + klabs(y3 - rayy)) {
				rayx = nintx;
				rayy = ninty;
				hitwall = z;
				ray.set(rayx, rayy, 0, hitwall);
			}
		}
		return ray.set(rayx, rayy, 0, hitwall);
	}
	
	public int hitscan(int xs, int ys, int zs, short sectnum, int vx, int vy, int vz,
			Hitscan hit, int cliptype) {
		SECTOR sec;
		WALL wal, wal2;
		SPRITE spr;
		int zz, x1, y1 = 0, z1 = 0, x2, y2, x3, y3, x4, y4;
		int intx, inty, intz;
		short z;

		int topt, topu, bot, dist, offx, offy, cstat;
		int i, j, k, l, tilenum, xoff, yoff, dax, day;
		int ang, cosang, sinang, xspan, yspan, xrepeat, yrepeat;
		long dawalclipmask, dasprclipmask;
		short tempshortcnt, tempshortnum, dasector, startwall, endwall;
		short nextsector;
		int clipyou;

		hit.hitsect = -1;
		hit.hitwall = -1;
		hit.hitsprite = -1;
		if (sectnum < 0)
			return (-1);

		hit.hitx = hitscangoalx;
		hit.hity = hitscangoaly;

		dawalclipmask = (cliptype & 65535);
		dasprclipmask = (cliptype >> 16);

		clipsectorlist[0] = sectnum;
		tempshortcnt = 0;
		tempshortnum = 1;
		do {
			dasector = clipsectorlist[tempshortcnt];
			sec = map.sector[dasector];
			if(sec == null) break;
			x1 = 0x7fffffff;
			if ((sec.ceilingstat & 2) != 0) {
				wal = map.wall[sec.wallptr];
				wal2 = map.wall[wal.point2];
				dax = wal2.x - wal.x;
				day = wal2.y - wal.y;
				i = eng.ksqrt(dax * dax + day * day);
				if (i == 0)
					continue;
				i = (int) divscale(sec.ceilingheinum, i, 15);
				dax *= i;
				day *= i;

				j = (vz << 8) - dmulscale(dax, vy, -day, vx, 15);
				if (j != 0) {
					i = ((sec.ceilingz - zs) << 8) + dmulscale(dax, ys - wal.y, -day, xs - wal.x, 15);
					if (((i ^ j) >= 0) && ((klabs(i) >> 1) < klabs(j))) {
						i = (int) divscale(i, j, 30);
						x1 = xs + mulscale(vx, i, 30);
						y1 = ys + mulscale(vy, i, 30);
						z1 = zs + mulscale(vz, i, 30);
					}
				}
			} else if ((vz < 0) && (zs >= sec.ceilingz)) {
				z1 = sec.ceilingz;
				i = z1 - zs;
				if ((klabs(i) >> 1) < -vz) {
					i = (int) divscale(i, vz, 30);
					x1 = xs + mulscale(vx, i, 30);
					y1 = ys + mulscale(vy, i, 30);
				}
			}
			if ((x1 != 0x7fffffff) && (klabs(x1 - xs) + klabs(y1 - ys) < klabs((hit.hitx) - xs) + klabs((hit.hity) - ys)))
				if (inside(x1, y1, dasector) != 0) {
					hit.hitsect = dasector;
					hit.hitwall = -1;
					hit.hitsprite = -1;
					hit.hitx = x1;
					hit.hity = y1;
					hit.hitz = z1;
				}

			x1 = 0x7fffffff;
			if ((sec.floorstat & 2) != 0) {
				wal = map.wall[sec.wallptr];
				wal2 = map.wall[wal.point2];
				dax = wal2.x - wal.x;
				day = wal2.y - wal.y;
				i = eng.ksqrt(dax * dax + day * day);
				if (i == 0)
					continue;
				i = (int) divscale(sec.floorheinum, i, 15);
				dax *= i;
				day *= i;

				j = (vz << 8) - dmulscale(dax, vy, -day, vx, 15);
				if (j != 0) {
					i = ((sec.floorz - zs) << 8) + dmulscale(dax, ys - wal.y, -day, xs - wal.x, 15);
					if (((i ^ j) >= 0) && ((klabs(i) >> 1) < klabs(j))) {
						i = (int) divscale(i, j, 30);
						x1 = xs + mulscale(vx, i, 30);
						y1 = ys + mulscale(vy, i, 30);
						z1 = zs + mulscale(vz, i, 30);
					}
				}
			} else if ((vz > 0) && (zs <= sec.floorz)) {
				z1 = sec.floorz;
				i = z1 - zs;
				if ((klabs(i) >> 1) < vz) {
					i = (int) divscale(i, vz, 30);
					x1 = xs + mulscale(vx, i, 30);
					y1 = ys + mulscale(vy, i, 30);
				}
			}
			if ((x1 != 0x7fffffff) && (klabs(x1 - xs) + klabs(y1 - ys) < klabs((hit.hitx) - xs) + klabs((hit.hity) - ys)))
				if (inside(x1, y1, dasector) != 0) {
					hit.hitsect = dasector;
					hit.hitwall = -1;
					hit.hitsprite = -1;
					hit.hitx = x1;
					hit.hity = y1;
					hit.hitz = z1;
				}

			startwall = sec.wallptr;
			endwall = (short) (startwall + sec.wallnum);
			
			if(startwall < 0 || endwall < 0) { tempshortcnt++; continue; }
			Point out = null;
			for (z = startwall; z < endwall; z++) {
				wal = map.wall[z];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = map.wall[wal.point2];
				if(wal2 == null) continue;
				x1 = wal.x;
				y1 = wal.y;
				x2 = wal2.x;
				y2 = wal2.y;

				if ((x1 - xs) * (y2 - ys) < (x2 - xs) * (y1 - ys))
					continue;

				if ((out = rintersect(xs, ys, zs, vx, vy, vz, x1, y1, x2, y2)) == null)
					continue;
				
				intx = out.getX();
				inty = out.getY();
				intz = out.getZ();

				if (klabs(intx - xs) + klabs(inty - ys) >= klabs((hit.hitx) - xs) + klabs((hit.hity) - ys))
					continue;

				nextsector = wal.nextsector;
				if ((nextsector < 0) || ((wal.cstat & dawalclipmask) != 0)) {
					hit.hitsect = dasector;
					hit.hitwall = z;
					hit.hitsprite = -1;
					hit.hitx = intx;
					hit.hity = inty;
					hit.hitz = intz;
					continue;
				}
				
				getzsofslope(nextsector, intx, inty, zofslope);
				if ((intz <= zofslope[CEIL]) || (intz >= zofslope[FLOOR])) {
					hit.hitsect = dasector;
					hit.hitwall = z;
					hit.hitsprite = -1;
					hit.hitx = intx;
					hit.hity = inty;
					hit.hitz = intz;
					continue;
				}

				for (zz = tempshortnum - 1; zz >= 0; zz--)
					if (clipsectorlist[zz] == nextsector)
						break;
				if (zz < 0)
					clipsectorlist[tempshortnum++] = nextsector;
			}

			for (z = map.headspritesect[dasector]; z >= 0; z = map.nextspritesect[z]) {
				spr = map.sprite[z];
				cstat = spr.cstat;

				if (hitallsprites == 0)
					if ((cstat & dasprclipmask) == 0)
						continue;

				x1 = spr.x;
				y1 = spr.y;
				z1 = spr.z;
				switch (cstat & 48) {
				case 0:
					topt = vx * (x1 - xs) + vy * (y1 - ys);
					if (topt <= 0)
						continue;
					bot = vx * vx + vy * vy;
					if (bot == 0)
						continue;

					intz = zs + scale(vz, topt, bot);

					i = (tilesizy[spr.picnum] * spr.yrepeat << 2);
					if ((cstat & 128) != 0)
						z1 += (i >> 1);
					if ((picanm[spr.picnum] & 0x00ff0000) != 0)
						z1 -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
					if ((intz > z1) || (intz < z1 - i))
						continue;
					topu = vx * (y1 - ys) - vy * (x1 - xs);

					offx = scale(vx, topu, bot);
					offy = scale(vy, topu, bot);
					dist = offx * offx + offy * offy;
					i = tilesizx[spr.picnum] * spr.xrepeat;
					i *= i;
					if (dist > (i >> 7))
						continue;
					intx = xs + scale(vx, topt, bot);
					inty = ys + scale(vy, topt, bot);

					if (klabs(intx - xs) + klabs(inty - ys) > klabs((hit.hitx) - xs) + klabs((hit.hity) - ys))
						continue;

					hit.hitsect = dasector;
					hit.hitwall = -1;
					hit.hitsprite = z;
					hit.hitx = intx;
					hit.hity = inty;
					hit.hitz = intz;
					break;
				case 16:
					//These lines get the 2 points of the rotated sprite
					//Given: (x1, y1) starts out as the center point
					tilenum = spr.picnum;
					xoff = (int) ((byte) ((picanm[tilenum] >> 8) & 255)) + ((int) spr.xoffset);
					if ((cstat & 4) > 0)
						xoff = -xoff;
					k = spr.ang;
					l = spr.xrepeat;
					dax = sintable[k & 2047] * l;
					day = sintable[(k + 1536) & 2047] * l;
					l = tilesizx[tilenum];
					k = (l >> 1) + xoff;
					x1 -= mulscale(dax, k, 16);
					x2 = x1 + mulscale(dax, l, 16);
					y1 -= mulscale(day, k, 16);
					y2 = y1 + mulscale(day, l, 16);

					if ((cstat & 64) != 0) //back side of 1-way sprite
						if ((x1 - xs) * (y2 - ys) < (x2 - xs) * (y1 - ys))
							continue;

					if ((out = rintersect(xs, ys, zs, vx, vy, vz, x1, y1, x2, y2)) == null)
						continue;
					
					intx = out.getX();
					inty = out.getY();
					intz = out.getZ();

					if (klabs(intx - xs) + klabs(inty - ys) > klabs((hit.hitx) - xs) + klabs((hit.hity) - ys))
						continue;

					k = ((tilesizy[spr.picnum] * spr.yrepeat) << 2);
					if ((cstat & 128) != 0)
						zofslope[CEIL] = spr.z + (k >> 1);
					else
						zofslope[CEIL] = spr.z;
					if ((picanm[spr.picnum] & 0x00ff0000) != 0)
						zofslope[CEIL] -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
					if ((intz < zofslope[CEIL]) && (intz > zofslope[CEIL] - k)) {
						hit.hitsect = dasector;
						hit.hitwall = -1;
						hit.hitsprite = z;
						hit.hitx = intx;
						hit.hity = inty;
						hit.hitz = intz;
					}
					break;
				case 32:
					if (vz == 0)
						continue;
					intz = z1;
					if (((intz - zs) ^ vz) < 0)
						continue;
					if ((cstat & 64) != 0)
						if ((zs > intz) == ((cstat & 8) == 0))
							continue;

					intx = xs + scale(intz - zs, vx, vz);
					inty = ys + scale(intz - zs, vy, vz);

					if (klabs(intx - xs) + klabs(inty - ys) > klabs((hit.hitx) - xs) + klabs((hit.hity) - ys))
						continue;

					tilenum = spr.picnum;
					xoff = (int) ((byte) ((picanm[tilenum] >> 8) & 255)) + ((int) spr.xoffset);
					yoff = (int) ((byte) ((picanm[tilenum] >> 16) & 255)) + ((int) spr.yoffset);
					if ((cstat & 4) > 0)
						xoff = -xoff;
					if ((cstat & 8) > 0)
						yoff = -yoff;

					ang = spr.ang;
					cosang = sintable[(ang + 512) & 2047];
					sinang = sintable[ang & 2047];
					xspan = tilesizx[tilenum];
					xrepeat = spr.xrepeat;
					yspan = tilesizy[tilenum];
					yrepeat = spr.yrepeat;

					dax = ((xspan >> 1) + xoff) * xrepeat;
					day = ((yspan >> 1) + yoff) * yrepeat;
					x1 += dmulscale(sinang, dax, cosang, day, 16) - intx;
					y1 += dmulscale(sinang, day, -cosang, dax, 16) - inty;
					l = xspan * xrepeat;
					x2 = x1 - mulscale(sinang, l, 16);
					y2 = y1 + mulscale(cosang, l, 16);
					l = yspan * yrepeat;
					k = -mulscale(cosang, l, 16);
					x3 = x2 + k;
					x4 = x1 + k;
					k = -mulscale(sinang, l, 16);
					y3 = y2 + k;
					y4 = y1 + k;

					clipyou = 0;
					if ((y1 ^ y2) < 0) {
						if ((x1 ^ x2) < 0)
							clipyou ^= (x1 * y2 < x2 * y1 ? 1 : 0) ^ (y1 < y2 ? 1 : 0);
						else if (x1 >= 0)
							clipyou ^= 1;
					}
					if ((y2 ^ y3) < 0) {
						if ((x2 ^ x3) < 0)
							clipyou ^= (x2 * y3 < x3 * y2 ? 1 : 0) ^ (y2 < y3 ? 1 : 0);
						else if (x2 >= 0)
							clipyou ^= 1;
					}
					if ((y3 ^ y4) < 0) {
						if ((x3 ^ x4) < 0)
							clipyou ^= (x3 * y4 < x4 * y3 ? 1 : 0) ^ (y3 < y4 ? 1 : 0);
						else if (x3 >= 0)
							clipyou ^= 1;
					}
					if ((y4 ^ y1) < 0) {
						if ((x4 ^ x1) < 0)
							clipyou ^= (x4 * y1 < x1 * y4 ? 1 : 0) ^ (y4 < y1 ? 1 : 0);
						else if (x4 >= 0)
							clipyou ^= 1;
					}

					if (clipyou != 0) {
						hit.hitsect = dasector;
						hit.hitwall = -1;
						hit.hitsprite = z;
						hit.hitx = intx;
						hit.hity = inty;
						hit.hitz = intz;
					}
					break;
				}
			}
			tempshortcnt++;
		} while (tempshortcnt < tempshortnum);
		return (0);
	}
	
	public int neartag(int xs, int ys, int zs, short sectnum, short ange, Neartag near, int neartagrange, int tagsearch) {
		WALL wal, wal2;
		SPRITE spr;
		int i, z, zz, xe, ye, ze, x1, y1, z1, x2, y2;

		int topt, topu, bot, dist, offx, offy, vx, vy, vz;
		short tempshortcnt, tempshortnum, dasector, startwall, endwall;
		short nextsector, good;

		near.tagsector = -1;
		near.tagwall = -1;
		near.tagsprite = -1;
		near.taghitdist = 0;

		if (sectnum < 0 || (tagsearch & 3) == 0)
			return 0;

		vx = mulscale(sintable[(ange + 2560) & 2047], neartagrange, 14);
		xe = xs + vx;
		vy = mulscale(sintable[(ange + 2048) & 2047], neartagrange, 14);
		ye = ys + vy;
		vz = 0;
		ze = 0;

		clipsectorlist[0] = sectnum;
		tempshortcnt = 0;
		tempshortnum = 1;

		Point out = null;
		do {
			dasector = clipsectorlist[tempshortcnt];

			startwall = map.sector[dasector].wallptr;
			endwall = (short) (startwall + map.sector[dasector].wallnum - 1);
			for (z = startwall; z <= endwall; z++) {
				wal = map.wall[z];
				wal2 = map.wall[wal.point2];
				x1 = wal.x;
				y1 = wal.y;
				x2 = wal2.x;
				y2 = wal2.y;

				nextsector = wal.nextsector;

				good = 0;
				if (nextsector >= 0) {
					if (((tagsearch & 1) != 0) && map.sector[nextsector].lotag != 0)
						good |= 1;
					if (((tagsearch & 2) != 0) && map.sector[nextsector].hitag != 0)
						good |= 1;
				}
				if (((tagsearch & 1) != 0) && wal.lotag != 0)
					good |= 2;
				if (((tagsearch & 2) != 0) && wal.hitag != 0)
					good |= 2;

				if ((good == 0) && (nextsector < 0))
					continue;
				if ((x1 - xs) * (y2 - ys) < (x2 - xs) * (y1 - ys))
					continue;

				if ((out = lintersect(xs, ys, zs, xe, ye, ze, x1, y1, x2, y2)) != null) {
					if (good != 0) {
						if ((good & 1) != 0)
							near.tagsector = nextsector;
						if ((good & 2) != 0)
							near.tagwall = z;
						near.taghitdist = dmulscale(out.getX() - xs, sintable[(ange + 2560) & 2047], out.getY() - ys, sintable[(ange + 2048) & 2047], 14);
						xe = out.getX();
						ye = out.getY();
						ze = out.getZ();
					}
					if (nextsector >= 0) {
						for (zz = tempshortnum - 1; zz >= 0; zz--)
							if (clipsectorlist[zz] == nextsector)
								break;
						if (zz < 0)
							clipsectorlist[tempshortnum++] = nextsector;
					}
				}
			}

			for (z = map.headspritesect[dasector]; z >= 0; z = map.nextspritesect[z]) {
				spr = map.sprite[z];

				good = 0;
				if (((tagsearch & 1) != 0) && spr.lotag != 0)
					good |= 1;
				if (((tagsearch & 2) != 0) && spr.hitag != 0)
					good |= 1;
				if (good != 0) {
					x1 = spr.x;
					y1 = spr.y;
					z1 = spr.z;

					topt = vx * (x1 - xs) + vy * (y1 - ys);
					if (topt > 0) {
						bot = vx * vx + vy * vy;
						if (bot != 0) {
							int intz = zs + scale(vz, topt, bot);
							i = tilesizy[spr.picnum] * spr.yrepeat;
							if ((spr.cstat & 128) != 0)
								z1 += (i << 1);
							if ((picanm[spr.picnum] & 0x00ff0000) != 0)
								z1 -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
							if ((intz <= z1) && (intz >= z1 - (i << 2))) {
								topu = vx * (y1 - ys) - vy * (x1 - xs);
								offx = scale(vx, topu, bot);
								offy = scale(vy, topu, bot);
								dist = offx * offx + offy * offy;
								i = (tilesizx[spr.picnum] * spr.xrepeat);
								i *= i;
								if (dist <= (i >> 7)) {
									int intx = xs + scale(vx, topt, bot);
									int inty = ys + scale(vy, topt, bot);
									if (klabs(intx - xs) + klabs(inty - ys) < klabs(xe - xs) + klabs(ye - ys)) {
										near.tagsprite = z;
										near.taghitdist = dmulscale(intx - xs, sintable[(ange + 2560) & 2047], inty - ys, sintable[(ange + 2048) & 2047], 14);
										xe = intx;
										ye = inty;
										ze = intz;
									}
								}
							}
						}
					}
				}
			}

			tempshortcnt++;
		} while (tempshortcnt < tempshortnum);
		return (0);
	}
	
	private void addclipline(int dax1, int day1, int dax2, int day2, int daoval) {
		if (clipnum < MAXCLIPNUM) {
			clipit[clipnum].x1 = dax1;
			clipit[clipnum].y1 = day1;
			clipit[clipnum].x2 = dax2;
			clipit[clipnum].y2 = day2;
			clipobjectval[clipnum] = daoval;
			clipnum++;
		}
	}
	
	private Point keepaway(int x, int y, int w) { 
		int x1 = clipit[w].x1;
		int dx = clipit[w].x2 - x1;
		int y1 = clipit[w].y1;
		int dy = clipit[w].y2 - y1;
		int ox = ksgn(-dy);
		int oy = ksgn(dx);
		int first = (klabs(dx) <= klabs(dy) ? 1 : 0);
		
		while (true) {
			if (dx * (y - y1) > (x - x1) * dy)
				return keep.set(x, y, 0);
			if (first == 0)
				x += ox;
			else
				y += oy;
			first ^= 1;
		}
	}
	
	public static int clipmove_x, clipmove_y, clipmove_z;
	public static short clipmove_sectnum;
	public int clipmove(int x, int y, int z, short sectnum,
			long xvect, long yvect,
			int walldist, int ceildist, int flordist, int cliptype) {
		clipmove_x = x;
		clipmove_y = y;
		clipmove_z = z;
		clipmove_sectnum = sectnum;
		WALL wal, wal2;
		SPRITE spr;
		SECTOR sec, sec2;
		int i, j, templong1, templong2;
		long oxvect, oyvect;
		int lx, ly, retval;
		int intx, inty, goalx, goaly;

		int k, l, clipsectcnt, startwall, endwall, cstat, dasect;
		int x1, y1, x2, y2, cx, cy, rad, xmin, ymin, xmax, ymax;
		int bsz, xoff, yoff, xspan, yspan, cosang, sinang, tilenum;
		int xrepeat, yrepeat, gx, gy, dx, dy, dasprclipmask, dawalclipmask;
		int hitwall, cnt, clipyou;

		int dax, day, daz, daz2;

		if (((xvect | yvect) == 0) || (clipmove_sectnum < 0))
			return (0);
		retval = 0;

		oxvect = xvect;
		oyvect = yvect;

		goalx = clipmove_x + (int) (xvect >> 14);
		goaly = clipmove_y + (int) (yvect >> 14);

		clipnum = 0;

		cx = (clipmove_x + goalx) >> 1;
		cy = (clipmove_y + goaly) >> 1;
		//Extra walldist for sprites on sector lines
		gx = goalx - clipmove_x;
		gy = goaly - clipmove_y;
		rad = (eng.ksqrt(gx * gx + gy * gy) + MAXCLIPDIST + walldist + 8);
		xmin = cx - rad;
		ymin = cy - rad;
		xmax = cx + rad;
		ymax = cy + rad;

		dawalclipmask = (cliptype & 65535); //CLIPMASK0 = 0x00010001
		dasprclipmask = (cliptype >> 16); //CLIPMASK1 = 0x01000040

		clipsectorlist[0] = (short) clipmove_sectnum;
		clipsectcnt = 0;
		clipsectnum = 1;
		do {
			dasect = clipsectorlist[clipsectcnt++];
			sec = map.sector[dasect];
			startwall = sec.wallptr;
			endwall = startwall + sec.wallnum;
			if(startwall < 0 || endwall < 0) { clipsectcnt++; continue; }
			for (j = startwall; j < endwall; j++) {
				wal = map.wall[j];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = map.wall[wal.point2];
				if(wal2 == null) continue;
				if ((wal.x < xmin) && (wal2.x < xmin))
					continue;
				if ((wal.x > xmax) && (wal2.x > xmax))
					continue;
				if ((wal.y < ymin) && (wal2.y < ymin))
					continue;
				if ((wal.y > ymax) && (wal2.y > ymax))
					continue;

				x1 = wal.x;
				y1 = wal.y;
				x2 = wal2.x;
				y2 = wal2.y;

				dx = x2 - x1;
				dy = y2 - y1;
				if (dx * ((clipmove_y) - y1) < ((clipmove_x) - x1) * dy)
					continue; //If wall's not facing you

				if (dx > 0)
					dax = dx * (ymin - y1);
				else
					dax = dx * (ymax - y1);
				if (dy > 0)
					day = dy * (xmax - x1);
				else
					day = dy * (xmin - x1);
				if (dax >= day)
					continue;

				clipyou = 0;
				if ((wal.nextsector < 0) || ((wal.cstat & dawalclipmask) != 0)) {
					clipyou = 1;
				} else {
					Point out = rintersect(clipmove_x, clipmove_y, 0, gx, gy, 0, x1, y1, x2, y2);
					if (out == null) {
						dax = clipmove_x;
						day = clipmove_y;
					} else {
						dax = out.getX();
						day = out.getY();
						daz = out.getZ();
					}
					
					daz = getflorzofslope((short) dasect, dax, day);
					daz2 = getflorzofslope(wal.nextsector, dax, day);

					sec2 = map.sector[wal.nextsector];
					if(sec2 == null) continue;
					if (daz2 < daz - (1 << 8))
						if ((sec2.floorstat & 1) == 0)
							if ((clipmove_z) >= daz2 - (flordist - 1))
								clipyou = 1;
					if (clipyou == 0) {
						daz = getceilzofslope((short) dasect, dax, day);
						daz2 = getceilzofslope(wal.nextsector, dax, day);
						if (daz2 > daz + (1 << 8))
							if ((sec2.ceilingstat & 1) == 0)
								if ((clipmove_z) <= daz2 + (ceildist - 1))
									clipyou = 1;
					}
				}

				if (clipyou == 1) {
					//Add 2 boxes at endpoints
					bsz = walldist;
					if (gx < 0)
						bsz = -bsz;
					addclipline(x1 - bsz, y1 - bsz, x1 - bsz, y1 + bsz, (short) j + 32768);
					addclipline(x2 - bsz, y2 - bsz, x2 - bsz, y2 + bsz, (short) j + 32768);
					bsz = walldist;
					if (gy < 0)
						bsz = -bsz;
					addclipline(x1 + bsz, y1 - bsz, x1 - bsz, y1 - bsz, (short) j + 32768);
					addclipline(x2 + bsz, y2 - bsz, x2 - bsz, y2 - bsz, (short) j + 32768);

					dax = walldist;
					if (dy > 0)
						dax = -dax;
					day = walldist;
					if (dx < 0)
						day = -day;
					addclipline(x1 + dax, y1 + day, x2 + dax, y2 + day, (short) j + 32768);
				} else {
					for (i = clipsectnum - 1; i >= 0; i--)
						if (wal.nextsector == clipsectorlist[i])
							break;
					if (i < 0)
						clipsectorlist[clipsectnum++] = wal.nextsector;
				}
			}

			for (j = map.headspritesect[dasect]; j >= 0; j = map.nextspritesect[j]) {
				spr = map.sprite[j];

				cstat = spr.cstat;

				if ((cstat & dasprclipmask) == 0)
					continue;

				x1 = spr.x;
				y1 = spr.y;
				switch (cstat & 48) {
				case 0:

					if ((x1 >= xmin) && (x1 <= xmax) && (y1 >= ymin) && (y1 <= ymax)) {
						k = ((tilesizy[spr.picnum] * spr.yrepeat) << 2);
						if ((cstat & 128) != 0)
							daz = spr.z + (k >> 1);
						else
							daz = spr.z;
						if ((picanm[spr.picnum] & 0x00ff0000) != 0)
							daz -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);

						if ((clipmove_z < (daz + ceildist)) && (clipmove_z > (daz - k - flordist))) {
							bsz = (spr.clipdist << 2) + walldist;
							if (gx < 0)
								bsz = -bsz;
							addclipline(x1 - bsz, y1 - bsz, x1 - bsz, y1 + bsz, (short) j + 49152);
							bsz = (spr.clipdist << 2) + walldist;
							if (gy < 0)
								bsz = -bsz;
							addclipline(x1 + bsz, y1 - bsz, x1 - bsz, y1 - bsz, (short) j + 49152);
						}
					}
					break;
				case 16:
					k = ((tilesizy[spr.picnum] * spr.yrepeat) << 2);
					if ((cstat & 128) != 0)
						daz = spr.z + (k >> 1);
					else
						daz = spr.z;
					if ((picanm[spr.picnum] & 0x00ff0000) != 0)
						daz -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
					daz2 = daz - k;
					daz += ceildist;
					daz2 -= flordist;
					if (((clipmove_z) < daz) && ((clipmove_z) > daz2)) {
						//These lines get the 2 points of the rotated sprite
						//Given: (x1, y1) starts out as the center point
						tilenum = spr.picnum;
						xoff = (byte) ((picanm[tilenum] >> 8) & 255) + spr.xoffset;
						if ((cstat & 4) > 0)
							xoff = -xoff;
						k = spr.ang;
						l = spr.xrepeat;
						dax = sintable[k & 2047] * l;
						day = sintable[(k + 1536) & 2047] * l;
						l = tilesizx[tilenum];
						k = (l >> 1) + xoff;
						x1 -= mulscale(dax, k, 16);
						x2 = x1 + mulscale(dax, l, 16);
						y1 -= mulscale(day, k, 16);
						y2 = y1 + mulscale(day, l, 16);

						if (clipinsideboxline(cx, cy, x1, y1, x2, y2, rad) != 0) {
							dax = mulscale(sintable[(spr.ang + 256 + 512) & 2047], walldist, 14);
							day = mulscale(sintable[(spr.ang + 256) & 2047], walldist, 14);

							if ((x1 - (clipmove_x)) * (y2 - (clipmove_y)) >= (x2 - (clipmove_x)) * (y1 - (clipmove_y))) //Front
							{
								addclipline(x1 + dax, y1 + day, x2 + day, y2 - dax, (short) j + 49152);
							} else {
								if ((cstat & 64) != 0)
									continue;
								addclipline(x2 - dax, y2 - day, x1 - day, y1 + dax, (short) j + 49152);
							}

							//Side blocker
							if ((x2 - x1) * ((clipmove_x) - x1) + (y2 - y1) * ((clipmove_y) - y1) < 0) {
								addclipline(x1 - day, y1 + dax, x1 + dax, y1 + day, (short) j + 49152);
							} else if ((x1 - x2) * ((clipmove_x) - x2) + (y1 - y2) * ((clipmove_y) - y2) < 0) {
								addclipline(x2 + day, y2 - dax, x2 - dax, y2 - day, (short) j + 49152);
							}
						}
					}

					break;
				case 32:
					daz = spr.z + ceildist;
					daz2 = spr.z - flordist;
					if (((clipmove_z) < daz) && ((clipmove_z) > daz2)) {
						if ((cstat & 64) != 0)
							if (((clipmove_z) > spr.z) == ((cstat & 8) == 0))
								continue;

						tilenum = spr.picnum;
						xoff = (int) ((byte) ((picanm[tilenum] >> 8) & 255)) + ((int) spr.xoffset);
						yoff = (int) ((byte) ((picanm[tilenum] >> 16) & 255)) + ((int) spr.yoffset);
						if ((cstat & 4) > 0)
							xoff = -xoff;
						if ((cstat & 8) > 0)
							yoff = -yoff;

						k = spr.ang;
						cosang = sintable[(k + 512) & 2047];
						sinang = sintable[k & 2047];
						xspan = tilesizx[tilenum];
						xrepeat = spr.xrepeat;
						yspan = tilesizy[tilenum];
						yrepeat = spr.yrepeat;

						dax = ((xspan >> 1) + xoff) * xrepeat;
						day = ((yspan >> 1) + yoff) * yrepeat;
						rxi[0] = x1 + dmulscale(sinang, dax, cosang, day, 16);
						ryi[0] = y1 + dmulscale(sinang, day, -cosang, dax, 16);
						l = xspan * xrepeat;
						rxi[1] = rxi[0] - mulscale(sinang, l, 16);
						ryi[1] = ryi[0] + mulscale(cosang, l, 16);
						l = yspan * yrepeat;
						k = -mulscale(cosang, l, 16);
						rxi[2] = rxi[1] + k;
						rxi[3] = rxi[0] + k;
						k = -mulscale(sinang, l, 16);
						ryi[2] = ryi[1] + k;
						ryi[3] = ryi[0] + k;

						dax = mulscale(sintable[(spr.ang - 256 + 512) & 2047], walldist, 14);
						day = mulscale(sintable[(spr.ang - 256) & 2047], walldist, 14);

						if ((rxi[0] - (clipmove_x)) * (ryi[1] - (clipmove_y)) < (rxi[1] - (clipmove_x)) * (ryi[0] - (clipmove_y))) {
							if (clipinsideboxline(cx, cy, rxi[1], ryi[1], rxi[0], ryi[0], rad) != 0)
								addclipline(rxi[1] - day, ryi[1] + dax, rxi[0] + dax, ryi[0] + day, (short) j + 49152);
						} else if ((rxi[2] - (clipmove_x)) * (ryi[3] - (clipmove_y)) < (rxi[3] - (clipmove_x)) * (ryi[2] - (clipmove_y))) {
							if (clipinsideboxline(cx, cy, rxi[3], ryi[3], rxi[2], ryi[2], rad) != 0)
								addclipline(rxi[3] + day, ryi[3] - dax, rxi[2] - dax, ryi[2] - day, (short) j + 49152);
						}

						if ((rxi[1] - (clipmove_x)) * (ryi[2] - (clipmove_y)) < (rxi[2] - (clipmove_x)) * (ryi[1] - (clipmove_y))) {
							if (clipinsideboxline(cx, cy, rxi[2], ryi[2], rxi[1], ryi[1], rad) != 0)
								addclipline(rxi[2] - dax, ryi[2] - day, rxi[1] - day, ryi[1] + dax, (short) j + 49152);
						} else if ((rxi[3] - (clipmove_x)) * (ryi[0] - (clipmove_y)) < (rxi[0] - (clipmove_x)) * (ryi[3] - (clipmove_y))) {
							if (clipinsideboxline(cx, cy, rxi[0], ryi[0], rxi[3], ryi[3], rad) != 0)
								addclipline(rxi[0] + dax, ryi[0] + day, rxi[3] + day, ryi[3] - dax, (short) j + 49152);
						}
					}
					break;
				}
			}
		} while (clipsectcnt < clipsectnum);

		hitwall = 0;
		cnt = clipmoveboxtracenum;
		do {
			Clip out = raytrace(clipmove_x, clipmove_y, goalx, goaly);
			intx = out.getX();
			inty = out.getY();
			hitwall = out.getNum();
			
			if (hitwall >= 0) {
				lx = clipit[hitwall].x2 - clipit[hitwall].x1;
				ly = clipit[hitwall].y2 - clipit[hitwall].y1;
				templong2 = lx * lx + ly * ly;
				if (templong2 > 0) {
					templong1 = (goalx - intx) * lx + (goaly - inty) * ly;

					if ((klabs(templong1) >> 11) < templong2)
						i = (int) divscale(templong1, templong2, 20);
					else
						i = 0;
					goalx = mulscale(lx, i, 20) + intx;
					goaly = mulscale(ly, i, 20) + inty;
				}

				templong1 = dmulscale(lx, oxvect, ly, oyvect, 6);
				for (i = cnt + 1; i <= clipmoveboxtracenum; i++) {
					j = hitwalls[i];
					templong2 = dmulscale(clipit[j].x2 - clipit[j].x1, oxvect, clipit[j].y2 - clipit[j].y1, oyvect, 6);
					if ((templong1 ^ templong2) < 0) {
						clipmove_sectnum = updatesector(clipmove_x, clipmove_y, (short) clipmove_sectnum);
						return (retval);
					}
				}

				Point goal = keepaway(goalx, goaly, hitwall);
				goalx = goal.getX();
				goaly = goal.getY();
				xvect = ((goalx - intx) << 14);
				yvect = ((goaly - inty) << 14);

				if (cnt == clipmoveboxtracenum)
					retval = clipobjectval[hitwall];
				hitwalls[cnt] = hitwall;
			}
			cnt--;

			clipmove_x = intx;
			clipmove_y = inty;
		} while (((xvect | yvect) != 0) && (hitwall >= 0) && (cnt > 0));

		for (j = 0; j < clipsectnum; j++)
			if (inside(clipmove_x, clipmove_y, clipsectorlist[j]) == 1) {
				clipmove_sectnum = clipsectorlist[j];
				return (retval);
			}

		clipmove_sectnum = -1;
		templong1 = 0x7fffffff;
		for (short jj = (short) (map.numsectors - 1); jj >= 0; jj--)
			if (inside(clipmove_x, clipmove_y, jj) == 1) {
				if ((map.sector[jj].ceilingstat & 2) != 0)
					templong2 = getceilzofslope(jj, clipmove_x, clipmove_y) - (clipmove_z);
				else
					templong2 = (map.sector[jj].ceilingz - (clipmove_z));

				if (templong2 > 0) {
					if (templong2 < templong1) {
						clipmove_sectnum = jj;
						templong1 = templong2;
					}
				} else {
					if ((map.sector[jj].floorstat & 2) != 0)
						templong2 = (clipmove_z) - getflorzofslope(jj, clipmove_x, clipmove_y);
					else
						templong2 = ((clipmove_z) - map.sector[jj].floorz);

					if (templong2 <= 0) {
						clipmove_sectnum = jj;
						return (retval);
					}
					if (templong2 < templong1) {
						clipmove_sectnum = jj;
						templong1 = templong2;
					}
				}
			}

		return (retval);
	}

	public static int pushmove_x, pushmove_y, pushmove_z;
	public static short pushmove_sectnum;
	public int pushmove(int x, int y, int z, short sectnum,
			int walldist, int ceildist, int flordist, int cliptype) { 
		pushmove_x = x;
		pushmove_y = y;
		pushmove_z = z;
		pushmove_sectnum = sectnum;

		SECTOR sec, sec2;
		WALL wal;
		int i, j, k, t, dx, dy, dax, day, daz, daz2, bad, dir;
		int dawalclipmask;
		short startwall, endwall, clipsectcnt;
		int bad2;

		if (pushmove_sectnum < 0)
			return (-1);

		dawalclipmask = (cliptype & 65535);

		k = 32;
		dir = 1;
		do {
			bad = 0;
			clipsectorlist[0] = (short) pushmove_sectnum;
			clipsectcnt = 0;
			clipsectnum = 1;
			do {
				if (clipsectorlist[clipsectcnt] == -1)
					continue;

				sec = map.sector[clipsectorlist[clipsectcnt]];
				if (dir > 0) {
					startwall = sec.wallptr;
					endwall = (short) (startwall + sec.wallnum);
				} else {
					endwall = sec.wallptr;
					startwall = (short) (endwall + sec.wallnum);
				}

				if(startwall < 0 || endwall < 0) { clipsectcnt++; continue; }
				for (i = startwall; i != endwall; i += dir) {
					if(i >= MAXWALLS) break;
					wal = map.wall[i];
					if (clipinsidebox(pushmove_x, pushmove_y, (short) i, walldist - 4) == 1) {
						j = 0;
						if (wal.nextsector < 0)
							j = 1;
						if ((wal.cstat & dawalclipmask) != 0)
							j = 1;
						if (j == 0) {
							sec2 = map.sector[wal.nextsector];

							//Find closest point on wall (dax, day) to (*x, *y)
							dax = map.wall[wal.point2].x - wal.x;
							day = map.wall[wal.point2].y - wal.y;
							daz = dax * ((pushmove_x) - wal.x) + day * ((pushmove_y) - wal.y);
							if (daz <= 0)
								t = 0;
							else {
								daz2 = dax * dax + day * day;
								if (daz >= daz2)
									t = (1 << 30);
								else
									t = (int) divscale(daz, daz2, 30);
							}
							dax = wal.x + mulscale(dax, t, 30);
							day = wal.y + mulscale(day, t, 30);

							daz = getflorzofslope(clipsectorlist[clipsectcnt], dax, day);
							daz2 = getflorzofslope(wal.nextsector, dax, day);
							if(sec2 == null) continue;
							if ((daz2 < daz - (1 << 8)) && ((sec2.floorstat & 1) == 0))
								if (pushmove_z >= daz2 - (flordist - 1))
									j = 1;

							daz = getceilzofslope(clipsectorlist[clipsectcnt], dax, day);
							daz2 = getceilzofslope(wal.nextsector, dax, day);
							if ((daz2 > daz + (1 << 8)) && ((sec2.ceilingstat & 1) == 0))
								if (pushmove_z <= daz2 + (ceildist - 1))
									j = 1;
						}
						if (j != 0) {
							j = eng.getangle(map.wall[wal.point2].x - wal.x, map.wall[wal.point2].y - wal.y);
							dx = (sintable[(j + 1024) & 2047] >> 11);
							dy = (sintable[(j + 512) & 2047] >> 11);
							bad2 = 16;
							do {
								pushmove_x += dx;
								pushmove_y += dy;
								bad2--;
								if (bad2 == 0)
									break;
							} while (clipinsidebox(pushmove_x, pushmove_y, (short) i, walldist - 4) != 0);
							bad = -1;
							k--;
							if (k <= 0)
								return (bad);
							pushmove_sectnum = updatesector(pushmove_x, pushmove_y, (short) pushmove_sectnum);
							if (pushmove_sectnum < 0)
								return -1;
						} else {
							for (j = clipsectnum - 1; j >= 0; j--)
								if (wal.nextsector == clipsectorlist[j])
									break;
							if (j < 0)
								clipsectorlist[clipsectnum++] = wal.nextsector;
						}
					}
				}

				clipsectcnt++;
			} while (clipsectcnt < clipsectnum);
			dir = -dir;
		} while (bad != 0);

		return (bad);
	}
	
	public int sectorofwall(short theline) {
		int i, gap;

		if ((theline < 0) || (theline >= map.numwalls))
			return (-1);
		i = map.wall[theline].nextwall;
		if (i >= 0)
			return (map.wall[i].nextsector);

		gap = (map.numsectors >> 1);
		i = gap;
		while (gap > 1) {
			gap >>= 1;
			if (map.sector[i].wallptr < theline)
				i += gap;
			else
				i -= gap;
		}
		while (map.sector[i].wallptr > theline)
			i--;
		while (map.sector[i].wallptr + map.sector[i].wallnum <= theline)
			i++;
		return (i);
	}
	
	public int clockdir(short wallstart) //Returns: 0 is CW, 1 is CCW
	{
		int i, themin;
		long minx, templong, x0, x1, x2, y0, y1, y2;

		minx = 0x7fffffff;
		themin = -1;
		i = wallstart - 1;
		do {
			i++;
			if (map.wall[map.wall[i].point2].x < minx) {
				minx = map.wall[map.wall[i].point2].x;
				themin = i;
			}
		} while ((map.wall[i].point2 != wallstart) && (i < MAXWALLS));

		x0 = map.wall[themin].x;
		y0 = map.wall[themin].y;
		x1 = map.wall[map.wall[themin].point2].x;
		y1 = map.wall[map.wall[themin].point2].y;
		x2 = map.wall[map.wall[map.wall[themin].point2].point2].x;
		y2 = map.wall[map.wall[map.wall[themin].point2].point2].y;

		if ((y1 >= y2) && (y1 <= y0))
			return (0);
		if ((y1 >= y0) && (y1 <= y2))
			return (1);

		templong = (x0 - x1) * (y2 - y1) - (x2 - x1) * (y0 - y1);
		if (templong < 0)
			return (0);
		else
			return (1);
	}

	public int loopinside(int x, int y, short startwall) {
		int x1, y1, x2, y2, templong;
		int i, cnt;

		cnt = clockdir(startwall);
		i = startwall;
		do {
			x1 = map.wall[i].x;
			x2 = map.wall[map.wall[i].point2].x;
			if ((x1 >= x) || (x2 >= x)) {
				y1 = map.wall[i].y;
				y2 = map.wall[map.wall[i].point2].y;
				if (y1 > y2) {
					templong = x1;
					x1 = x2;
					x2 = templong;
					templong = y1;
					y1 = y2;
					y2 = templong;
				}
				if ((y1 <= y) && (y2 > y))
					if (x1 * (y - y2) + x2 * (y1 - y) <= x * (y1 - y2))
						cnt ^= 1;
			}
			i = map.wall[i].point2;
		} while (i != startwall);
		return (cnt);
	}
	
	public static int zr_ceilz, zr_ceilhit, zr_florz, zr_florhit;
	public void getzrange(int x, int y, int z, short sectnum,
			int walldist, int cliptype) {
		SECTOR sec;
		WALL wal, wal2;
		SPRITE spr;
		int clipsectcnt, startwall, endwall, tilenum, xoff, yoff, dax, day;
		int xmin, ymin, xmax, ymax, i, j, k, l, dx, dy;
		int x1, y1, x2, y2, x3, y3, x4, y4, ang, cosang, sinang;
		int xspan, yspan, xrepeat, yrepeat, dasprclipmask, dawalclipmask;

		short cstat;
		int clipyou;

		if (sectnum < 0) {
			zr_ceilz = 0x80000000;
			zr_ceilhit = -1;
			zr_florz = 0x7fffffff;
			zr_florhit = -1;
			return;
		}

		//Extra walldist for sprites on sector lines
		i = walldist + MAXCLIPDIST + 1;
		xmin = x - i;
		ymin = y - i;
		xmax = x + i;
		ymax = y + i;

		getzsofslope(sectnum, x, y, zofslope);
		zr_ceilz = zofslope[CEIL];
		zr_florz = zofslope[FLOOR];

		zr_ceilhit = sectnum + 16384;
		zr_florhit = sectnum + 16384;

		dawalclipmask = (cliptype & 65535);
		dasprclipmask = (cliptype >> 16);

		clipsectorlist[0] = sectnum;
		clipsectcnt = 0;
		clipsectnum = 1;

		do //Collect sectors inside your square first
		{
			sec = map.sector[clipsectorlist[clipsectcnt]];
			startwall = sec.wallptr;
			endwall = startwall + sec.wallnum;
			if(startwall < 0 || endwall < 0) { clipsectcnt++; continue; }
			for (j = startwall; j < endwall; j++) {
				wal = map.wall[j];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				k = wal.nextsector;
				if (k >= 0) {
					wal2 = map.wall[wal.point2];
					if(wal2 == null) continue;
					x1 = wal.x;
					x2 = wal2.x;
					if ((x1 < xmin) && (x2 < xmin))
						continue;
					if ((x1 > xmax) && (x2 > xmax))
						continue;
					y1 = wal.y;
					y2 = wal2.y;
					if ((y1 < ymin) && (y2 < ymin))
						continue;
					if ((y1 > ymax) && (y2 > ymax))
						continue;

					dx = x2 - x1;
					dy = y2 - y1;
					if (dx * (y - y1) < (x - x1) * dy)
						continue; //back
					if (dx > 0)
						dax = dx * (ymin - y1);
					else
						dax = dx * (ymax - y1);
					if (dy > 0)
						day = dy * (xmax - x1);
					else
						day = dy * (xmin - x1);
					if (dax >= day)
						continue;

					if ((wal.cstat & dawalclipmask) != 0)
						continue;
					sec = map.sector[k];
					if(sec == null) continue;
					
					if (((sec.ceilingstat & 1) == 0) && (z <= sec.ceilingz + (3 << 8)))
						continue;
					if (((sec.floorstat & 1) == 0) && (z >= sec.floorz - (3 << 8)))
						continue;
					
					for (i = clipsectnum - 1; i >= 0; i--)
						if (clipsectorlist[i] == k)
							break;
					if (i < 0)
						clipsectorlist[clipsectnum++] = (short) k;

					if ((x1 < xmin + MAXCLIPDIST) && (x2 < xmin + MAXCLIPDIST))
						continue;
					if ((x1 > xmax - MAXCLIPDIST) && (x2 > xmax - MAXCLIPDIST))
						continue;
					if ((y1 < ymin + MAXCLIPDIST) && (y2 < ymin + MAXCLIPDIST))
						continue;
					if ((y1 > ymax - MAXCLIPDIST) && (y2 > ymax - MAXCLIPDIST))
						continue;
					if (dx > 0)
						dax += dx * MAXCLIPDIST;
					else
						dax -= dx * MAXCLIPDIST;
					if (dy > 0)
						day -= dy * MAXCLIPDIST;
					else
						day += dy * MAXCLIPDIST;
					if (dax >= day)
						continue;

					//It actually got here, through all the continue's!!!
					getzsofslope((short) k, x, y, zofslope);

					if (zofslope[CEIL] > zr_ceilz) {
						zr_ceilz = zofslope[CEIL];
						zr_ceilhit = k + 16384;
					}
					if (zofslope[FLOOR] < zr_florz) {
						zr_florz = zofslope[FLOOR];
						zr_florhit = k + 16384;
					}
				}
			}
			clipsectcnt++;
		} while (clipsectcnt < clipsectnum);

		for (i = 0; i < clipsectnum; i++) {
			for (j = map.headspritesect[clipsectorlist[i]]; j >= 0; j = map.nextspritesect[j]) {
				spr = map.sprite[j];
				cstat = spr.cstat;
				if ((cstat & dasprclipmask) != 0) {
					x1 = spr.x;
					y1 = spr.y;

					clipyou = 0;
					switch (cstat & 48) {
					case 0:
						k = walldist + (spr.clipdist << 2) + 1;
						if ((klabs(x1 - x) <= k) && (klabs(y1 - y) <= k)) {
							zofslope[CEIL] = spr.z;
							k = ((tilesizy[spr.picnum] * spr.yrepeat) << 1);
							if ((cstat & 128) != 0)
								zofslope[CEIL] += k;
							if ((picanm[spr.picnum] & 0x00ff0000) != 0)
								zofslope[CEIL] -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
							zofslope[FLOOR] = zofslope[CEIL] - (k << 1);
							clipyou = 1;
						}
						break;
					case 16:
						tilenum = spr.picnum;
						xoff = (int) ((byte) ((picanm[tilenum] >> 8) & 255)) + ((int) spr.xoffset);
						if ((cstat & 4) > 0)
							xoff = -xoff;
						k = spr.ang;
						l = spr.xrepeat;
						dax = sintable[k & 2047] * l;
						day = sintable[(k + 1536) & 2047] * l;
						l = tilesizx[tilenum];
						k = (l >> 1) + xoff;
						x1 -= mulscale(dax, k, 16);
						x2 = x1 + mulscale(dax, l, 16);
						y1 -= mulscale(day, k, 16);
						y2 = y1 + mulscale(day, l, 16);
						if (clipinsideboxline(x, y, x1, y1, x2, y2, walldist + 1) != 0) {
							zofslope[CEIL] = spr.z;
							k = ((tilesizy[spr.picnum] * spr.yrepeat) << 1);
							if ((cstat & 128) != 0)
								zofslope[CEIL] += k;
							if ((picanm[spr.picnum] & 0x00ff0000) != 0)
								zofslope[CEIL] -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
							zofslope[FLOOR] = zofslope[CEIL] - (k << 1);
							clipyou = 1;
						}
						break;
					case 32:
						zofslope[CEIL] = spr.z;
						zofslope[FLOOR] = zofslope[CEIL];

						if ((cstat & 64) != 0)
							if ((z > zofslope[CEIL]) == ((cstat & 8) == 0))
								continue;

						tilenum = spr.picnum;
						xoff = (int) ((byte) ((picanm[tilenum] >> 8) & 255)) + ((int) spr.xoffset);
						yoff = (int) ((byte) ((picanm[tilenum] >> 16) & 255)) + ((int) spr.yoffset);
						if ((cstat & 4) > 0)
							xoff = -xoff;
						if ((cstat & 8) > 0)
							yoff = -yoff;

						ang = spr.ang;
						cosang = sintable[(ang + 512) & 2047];
						sinang = sintable[ang & 2047];
						xspan = tilesizx[tilenum];
						xrepeat = spr.xrepeat;
						yspan = tilesizy[tilenum];
						yrepeat = spr.yrepeat;

						dax = ((xspan >> 1) + xoff) * xrepeat;
						day = ((yspan >> 1) + yoff) * yrepeat;
						x1 += dmulscale(sinang, dax, cosang, day, 16) - x;
						y1 += dmulscale(sinang, day, -cosang, dax, 16) - y;
						l = xspan * xrepeat;
						x2 = x1 - mulscale(sinang, l, 16);
						y2 = y1 + mulscale(cosang, l, 16);
						l = yspan * yrepeat;
						k = -mulscale(cosang, l, 16);
						x3 = x2 + k;
						x4 = x1 + k;
						k = -mulscale(sinang, l, 16);
						y3 = y2 + k;
						y4 = y1 + k;

						dax = mulscale(sintable[(spr.ang - 256 + 512) & 2047], walldist + 4, 14);
						day = mulscale(sintable[(spr.ang - 256) & 2047], walldist + 4, 14);
						x1 += dax;
						x2 -= day;
						x3 -= dax;
						x4 += day;
						y1 += day;
						y2 += dax;
						y3 -= day;
						y4 -= dax;

						if ((y1 ^ y2) < 0) {
							if ((x1 ^ x2) < 0)
								clipyou ^= (x1 * y2 < x2 * y1 ? 1 : 0) ^ (y1 < y2 ? 1 : 0);
							else if (x1 >= 0)
								clipyou ^= 1;
						}
						if ((y2 ^ y3) < 0) {
							if ((x2 ^ x3) < 0)
								clipyou ^= (x2 * y3 < x3 * y2 ? 1 : 0) ^ (y2 < y3 ? 1 : 0);
							else if (x2 >= 0)
								clipyou ^= 1;
						}
						if ((y3 ^ y4) < 0) {
							if ((x3 ^ x4) < 0)
								clipyou ^= (x3 * y4 < x4 * y3 ? 1 : 0) ^ (y3 < y4 ? 1 : 0);
							else if (x3 >= 0)
								clipyou ^= 1;
						}
						if ((y4 ^ y1) < 0) {
							if ((x4 ^ x1) < 0)
								clipyou ^= (x4 * y1 < x1 * y4 ? 1 : 0) ^ (y4 < y1 ? 1 : 0);
							else if (x4 >= 0)
								clipyou ^= 1;
						}
						break;
					}

					if (clipyou != 0) {
						if ((z > zofslope[CEIL]) && (zofslope[CEIL] > zr_ceilz)) {
							zr_ceilz = zofslope[CEIL];
							zr_ceilhit = j + 49152;
						}
						if ((z < zofslope[FLOOR]) && (zofslope[FLOOR] < zr_florz)) {
							zr_florz = zofslope[FLOOR];
							zr_florhit = j + 49152;
						}
					}
				}
			}
		}
	}
	
	public void preparemirror(int dax, int day, int daz, float daang, float dahoriz, int dawall, int dasector) {
		int i, j, x, y, dx, dy;

		x = map.wall[dawall].x;
		dx = map.wall[map.wall[dawall].point2].x - x;
		y = map.wall[dawall].y;
		dy = map.wall[map.wall[dawall].point2].y - y;
		j = dx * dx + dy * dy;
		if (j == 0)
			return;
		i = (((dax - x) * dx + (day - y) * dy) << 1);
		mirrorx = (x << 1) + scale(dx, i, j) - dax;
		mirrory = (y << 1) + scale(dy, i, j) - day;
		mirrorang = BClampAngle((eng.getangle(dx, dy) << 1) - daang);

		inpreparemirror = true;
	}

	public void completemirror() { 
		//Software render
	}

	public SPRITE getsprite(int num)
	{
		return map.sprite[num];
	}
	
	public SECTOR getsector(int num)
	{
		return map.sector[num];
	}
	
	public WALL getwall(int num)
	{
		return map.wall[num];
	}
	
	public short headspritesect(int sectnum)
	{
		return map.headspritesect[sectnum];
	}
	
	public short headspritestat(int statnum)
	{
		return map.headspritestat[statnum];
	}
	
	public short nextspritesect(int spritenum)
	{
		return map.nextspritesect[spritenum];
	}
	
	public short nextspritestat(int spritenum)
	{
		return map.nextspritestat[spritenum];
	}
}
