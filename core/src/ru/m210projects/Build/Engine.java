// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been modified from Ken Silverman's original release
// by Jonathon Fowler (jf@jonof.id.au)
// by the EDuke32 team (development@voidpoint.com)
// by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build;

import static java.lang.Math.*;
import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.Pragmas.*;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Net.Mmulti.uninitmultiplayer;
import static ru.m210projects.Build.Render.Types.Hightile.*;
import static ru.m210projects.Build.Strhandler.*;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

import ru.m210projects.Build.Audio.BAudio;
import ru.m210projects.Build.FileHandle.DirectoryEntry;
import ru.m210projects.Build.Input.KeyInput;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.OnSceenDisplay.DEFOSDFUNC;
import ru.m210projects.Build.OnSceenDisplay.OSDCOMMAND;
import ru.m210projects.Build.OnSceenDisplay.OSDCVARFUNC;
import ru.m210projects.Build.Render.Renderer;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Render.Types.GL10;
import ru.m210projects.Build.Render.Types.Spriteext;
import ru.m210projects.Build.Render.Types.Spritesmooth;
import ru.m210projects.Build.Types.Hitscan;
import ru.m210projects.Build.Types.LittleEndian;
import ru.m210projects.Build.Types.Message;
import ru.m210projects.Build.Types.Neartag;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;
import ru.m210projects.Build.Types.Palette;
import ru.m210projects.Build.Types.Tile2model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;

public abstract class Engine {
	
	protected class linetype {
		public int x1, y1, x2, y2;
	}

	private boolean releasedEngine;
	public boolean compatibleMode;
	public static boolean UseBloodPal = false;
	
	public Renderer render;
	private Message message;
//	public Sound fx;
//	public Music mx;
	private BAudio audio;
	private static KeyInput input;
	
	public static boolean offscreenrendering;
	
	public static float TRANSLUSCENT1 = 0.66f;
	public static float TRANSLUSCENT2 = 0.33f;
	public static float MAXDRUNKANGLE = 2.5f;

	public static int setviewcnt = 0; // interface layers use this now
	public static int[] bakwindowx1, bakwindowy1;
	public static int[] bakwindowx2, bakwindowy2;
	public static int baktile;
	
	public static final int CLIPMASK0 = (((1) << 16) + 1);
	public static final int CLIPMASK1 = (((256) << 16) + 64);
	public static final int MAXPSKYTILES = 256;
	public static final int MAXPALOOKUPS = 256;
	public static int USERTILES = 256;
	public static int MAXTILES = 9216 + USERTILES;
	public static final int MAXSTATUS = 1024;
	public static final int DETAILPAL = (MAXPALOOKUPS - 1);
	public static final int GLOWPAL = (MAXPALOOKUPS - 2);
	public static final int SPECULARPAL = (MAXPALOOKUPS - 3);
	public static final int NORMALPAL = (MAXPALOOKUPS - 4);
	public static final int RESERVEDPALS = 4; // don't forget to increment this when adding reserved pals
	public static final int MAXSECTORSV8 = 4096;
	public static final int MAXWALLSV8 = 16384;
	public static final int MAXSPRITESV8 = 16384;
	public static final int MAXSECTORSV7 = 1024;
	public static final int MAXWALLSV7 = 8192;
	public static final int MAXSPRITESV7 = 4096;
	public static int MAXSECTORS = MAXSECTORSV7;
	public static int MAXWALLS = MAXWALLSV7;
	public static int MAXSPRITES = MAXSPRITESV7;
	public static final int MAXSPRITESONSCREEN = 1024;
	public static final int MAXVOXELS = MAXSPRITES;
	public static final int EXTRATILES = (MAXTILES / 8);
	public static final int MAXUNIQHUDID = 256; //Extra slots so HUD models can store animation state without messing game sprites
	public static final int MAXPSKYMULTIS = 8;
	public static final int MAXPLAYERS = 16;
	public static final int MAXXDIM = 4096;
	public static final int MAXYDIM = 3072;
	public static short numshades;
	public static byte[] palette;
	public static short numsectors, numwalls, numsprites;
	public static int totalclock;
	public static short pskyoff[], zeropskyoff[], pskybits;
	public static Spriteext[] spriteext;
	public static Spritesmooth[] spritesmooth;
	public static byte parallaxtype;
	public static boolean showinvisibility;
	public static int visibility, parallaxvisibility;
	public static int parallaxyoffs, parallaxyscale;
	public static byte[][] palookup;
	public static byte[][] palookupfog;
	public static int timerticspersec;
	public static short[] sintable;
	public static byte automapping;
	public static short[] tilesizx, tilesizy;
	public static int numtiles, picanm[];
	public static byte waloff[][];
	public static byte[] show2dsector;
	public static byte[] show2dwall;
	public static byte[] show2dsprite;

	public static SECTOR[] sector;
	public static WALL[] wall;
	public static SPRITE[] sprite;
	public static SPRITE[] tsprite;

	public static short[] headspritesect, headspritestat;
	public static short[] prevspritesect, prevspritestat;
	public static short[] nextspritesect, nextspritestat;
	
	public static byte[] gotpic;
	public static byte[] gotsector;
	public static int spritesortcnt;
	public static int windowx1, windowy1, windowx2, windowy2;
	public static int xdim, ydim;
	public static int yxaspect, viewingrange;
	
	//OUTPUT VALUES
	public static int floorzsofslope, ceilzsofslope;
	public static int mirrorx, mirrory;
	public static float mirrorang;
	public static int intx;
	public static int inty;
	public static int intz;
	public static int rayx = 0;
	public static int rayy = 0;
	public static Palette returnpal;
	public static Hitscan pHitInfo;
	public static Neartag neartag;
	
	public static int fullscreen;
	public static int paletteloaded = 0;
	public static int tablesloaded = 0;
	public static byte[][] britable; // JBF 20040207: full 8bit precision
	public static int curbrightness = 0;
	public static int gammabrightness = 0; //FIXME newer changes
//	public static boolean usecustomarts;
//	public static final String[] customartfile = new String[MAXTILES];
	public static int[] picsiz;
	public static int xdimen = -1, halfxdimen, xdimenscale, xdimscale;
	public static int wx1, wy1, wx2, wy2, ydimen;
	public static final short pow2char[] = { 1, 2, 4, 8, 16, 32, 64, 128 };
	public static final int[] pow2long = {
		1, 2, 4, 8,
		16, 32, 64, 128,
		256, 512, 1024, 2048,
		4096, 8192, 16384, 32768,
		65536, 131072, 262144, 524288,
		1048576, 2097152, 4194304, 8388608,
		16777216, 33554432, 67108864, 134217728,
		268435456, 536870912, 1073741824, 2147483647,
	};
	public static Palette[] curpalette;
	public static FadeEffect palfadergb;

	public static Tile2model[] tile2model;
	public static int clipmoveboxtracenum = 3;
	public static int hitscangoalx = (1 << 29) - 1, hitscangoaly = (1 << 29) - 1;
	public static final int MAXVOXMIPS = 5;
//	public static byte[][][] voxoff;
	public static int[] tiletovox;
	public static boolean[] voxrotate;
	public static int globalposx, globalposy, globalposz; //polymost
	public static float globalhoriz, globalang;
	public static float pitch;
	public static short globalcursectnum;
	public static long globalvisibility;
	public static int globalshade, globalpal, cosglobalang, singlobalang;
	public static int cosviewingrangeglobalang, sinviewingrangeglobalang;
	public static int beforedrawrooms = 1, indrawroomsandmasks = 0;
	public static int xyaspect, viewingrangerecip;
	public static boolean inpreparemirror = false;
	public static char[] textfont;
	public static char[] smalltextfont;
	
	//high resources
	public static boolean usehightile = true;
	public static boolean usevoxels = true;
	public static boolean usemodels = true;
	
	private byte[] sectbitmap;
	protected int timerfreq;
	protected long timerlastsample;

	private int newaspect_enable = 1;
	private int setaspect_new_use_dimen;

	private final char[] artfilename = new char[12];
//	private int artsize = 0;
	public int numtilefiles;
	public static int artfil = -1;
	public int artfilnum;
	public int artfilplc;
	private int[] tilefilenum;
	private int[] tilefileoffs;
	private int artversion;
//	private int mapversion;
	private long totalclocklock;
	protected short[] sqrtable;
	protected short[] shlookup;
	private int hitallsprites = 0;
	private final int MAXCLIPNUM = 1024;
	protected final int MAXCLIPDIST = 1024;
	
//	private int[] lookups;
	protected short clipnum;

	protected int[] rxi;
	protected int[] ryi;
	protected int[] hitwalls;

	protected linetype[] clipit;
	protected short[] clipsectorlist;
	protected short clipsectnum;
	protected int[] clipobjectval;

	private int[] rdist, gdist, bdist;
	private final int FASTPALGRIDSIZ = 8;
	
	private byte[] colhere;
	private byte[] colhead;
	private byte[] colnext;
	private final byte[] coldist = { 0, 1, 2, 3, 4, 3, 2, 1 };
	private int[] colscan;
	private int randomseed = 1;

	private short[] radarang;
	private byte[] transluc;

	//Renderer preset XXX
	public static int r_parallaxskyclamping = 1; //OSD CVAR XXX
	public static int r_parallaxskypanning = 0; //XXX
	public static int r_usenewaspect = 1;
	public static int r_glowmapping = 1;
	// Vertex Array model drawing cvar
	public static int r_vertexarrays = 1;

	// Vertex Buffer Objects model drawing cvars
	public static int r_vbos = 1;
	public static int r_npotwallmode;
	// model animation smoothing cvar
	public static int r_animsmoothing = 1;
	public static int glanisotropy = 1; // 0 = maximum supported by card
		
		
	//Engine.c

	public long getkensmessagecrc(long b) {
		return 0x56c764d4l;
	}

	public int getpalookup(int davis, int dashade)
	{
	    return(min(max(dashade+(davis>>8),0),numshades-1));
	}

	public int addtsprite(int z) {
		if (spritesortcnt >= MAXSPRITESONSCREEN)
			return 1;

		if (tsprite[spritesortcnt] == null)
			tsprite[spritesortcnt] = new SPRITE();
		tsprite[spritesortcnt].set(sprite[z]);

		spriteext[z].tspr = tsprite[spritesortcnt];
		tsprite[spritesortcnt++].owner = (short) z;

		return 0;
	}

	public int animateoffs(short tilenum, int nInfo) {
		long clock, index = 0;

		int speed = (picanm[tilenum] >> 24) & 15; //picanm[nTile].speed
		if ((nInfo & 0xC000) == 0x8000) // sprite
		{
			// hash sprite frame by info variable
			clock = (totalclocklock + CRC32.getCRC(CRC32.getBytes((short) nInfo), 2)) >> speed;
		} else
			clock = totalclocklock >> speed;

		int frames = picanm[tilenum] & 63;

		if (frames > 0) {
			switch (picanm[tilenum] & 192) //picanm[nTile].type
			{
			case 64: // Oscil
				index = clock % (frames * 2);
				if (index >= frames)
					index = frames * 2 - index;
				break;
			case 128: // Forward
				index = clock % (frames + 1);
				break;
			case 192: // Backward
				index = -(clock % (frames + 1));
			}
		}
		return (int) index;
	}

	public void initksqrt() {
		int i, j = 1, k = 0;
		for (i = 0; i < 4096; i++) {
			if (i >= j) { j <<= 2; k++; }

			sqrtable[i] = (short)((int)sqrt(((i << 18) + 131072)) << 1);
			shlookup[i] = (short) ((k << 1) + ((10 - k) << 8));
			if (i < 256) shlookup[i + 4096] = (short) (((k + 6) << 1) + ((10 - (k + 6)) << 8));
		}
	}

	public void calcbritable() {
		int i, j;
		double a, b;
		for (i = 0; i < 16; i++) {
			a = 8 / (i + 8);
			b = 255 / pow(255, a);
			for (j = 0; j < 256; j++) {// JBF 20040207: full 8bit precision
				britable[i][j] = (byte) (pow(j, a) * b);
			}
		}
	}

	public void loadtables() throws Exception {
		int fil;

		if (tablesloaded == 0) {
			initksqrt();
			
			sintable = new short[2048];
			textfont = new char[2048];
			smalltextfont = new char[2048];

			if ((fil = kOpen("tables.dat", 0)) != -1) {
				byte[] buf = new byte[2048 * 2];
				
				kRead(fil, buf, buf.length);
				ByteBuffer.wrap(buf)
					.order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(sintable);

				if (releasedEngine) {
					buf = new byte[640 * 2];
					kRead(fil, buf, buf.length);
					ByteBuffer.wrap(buf)
						.order(ByteOrder.LITTLE_ENDIAN)
						.asShortBuffer().get(radarang, 0, 640);
					for (int i = 0; i < 640; i++) 
						radarang[1279 - i] = (short) -radarang[i];
				} else {
					kRead(fil, new byte[4096], 4096); //tantable
					buf = new byte[640];
					kRead(fil, buf, buf.length);
					ByteBuffer.wrap(buf)
						.order(ByteOrder.LITTLE_ENDIAN)
						.asShortBuffer().get(radarang, 0, 320);
					radarang[320] = 0x4000;
				}

				buf = new byte[1024];
				kRead(fil, buf, 1024);
				for (int i = 0; i < 1024; i++) 
					textfont[i] = (char) (buf[i] & 0xff);
				
				kRead(fil, buf, 1024);
				for (int i = 0; i < 1024; i++)
					smalltextfont[i] = (char) (buf[i] & 0xff);
				
				/* kread(fil, britable, 1024); */

				calcbritable();
				kClose(fil);
			} else 
				throw new Exception("ERROR: Failed to load TABLES.DAT!");
			
			tablesloaded = 1;
		}
	}

	public void initfastcolorlookup(int rscale, int gscale, int bscale) {
		int i, j, x, y, z;
		int pal1;

		j = 0;
		for (i = 64; i >= 0; i--) {
			rdist[i] = rdist[128 - i] = j * rscale;
			gdist[i] = gdist[128 - i] = j * gscale;
			bdist[i] = bdist[128 - i] = j * bscale;
			j += 129 - (i << 1);
		}

		Arrays.fill(colhere, 0, colhere.length, (byte) 0);
		Arrays.fill(colhead, 0, colhead.length, (byte) 0);

		pal1 = 768 - 3;
		for (i = 255; i >= 0; i--, pal1 -= 3) {
			int r = palette[pal1] & 0xFF;
			int g = palette[pal1 + 1] & 0xFF;
			int b = palette[pal1 + 2] & 0xFF;
			j = (r >> 3) * FASTPALGRIDSIZ * FASTPALGRIDSIZ
					+ (g >> 3) * FASTPALGRIDSIZ
					+ (b >> 3)
					+ FASTPALGRIDSIZ * FASTPALGRIDSIZ
					+ FASTPALGRIDSIZ + 1;

			if ((colhere[j >> 3] & pow2char[j & 7]) != 0)
				colnext[i] = colhead[j];
			else colnext[i] = -1;
			
			colhead[j] = (byte) i;
			colhere[j >> 3] |= pow2char[j & 7];
		}

		i = 0;
		for (x = -FASTPALGRIDSIZ * FASTPALGRIDSIZ; x <= FASTPALGRIDSIZ * FASTPALGRIDSIZ; x += FASTPALGRIDSIZ * FASTPALGRIDSIZ)
			for (y = -FASTPALGRIDSIZ; y <= FASTPALGRIDSIZ; y += FASTPALGRIDSIZ)
				for (z = -1; z <= 1; z++)
					colscan[i++] = x + y + z;
		i = colscan[13];
		colscan[13] = colscan[26];
		colscan[26] = i;
	}

	public void loadpalette() throws Exception
	{
		int fil;
		if (paletteloaded != 0) return;
		
		Console.Println("Loading palettes");
		if ((fil = kOpen("palette.dat", 0)) == -1) 
			throw new Exception("Failed to load \"palette.dat\"!");
	
		kRead(fil, palette, 768);

		if(releasedEngine) {
			byte[] buf = new byte[2];
			kRead(fil, buf, 2); numshades = LittleEndian.getShort(buf);
		} else {
			int file_len = kFileLength(fil);
			numshades = (short) ((file_len - 768) >> 7);
		    if ( (((file_len - 768) >> 7) & 1) <= 0 )
		    	numshades >>= 1;
		    else
		    	numshades = (short) ((numshades - 255) >> 1);
		}
		if (palookup[0] == null) 
			palookup[0] = new byte[numshades<<8];
		if (transluc == null)
			transluc = new byte[65536];

		globalpal = 0;
		Console.Println("Loading gamma correcion tables");
		kRead(fil, palookup[globalpal], numshades<<8);
		Console.Println("Loading translucency table");
		kRead(fil,transluc, 65536);

		kClose(fil);

		initfastcolorlookup(30,59,11);

		paletteloaded = 1;
	}

	public byte getclosestcol(int r, int g, int b) {
		int i, k, dist;
		byte retcol;
		int pal1;
		
		r >>= 2;
		g >>= 2;
		b >>= 2;

		int j = (r>>3)*FASTPALGRIDSIZ*FASTPALGRIDSIZ+(g>>3)*FASTPALGRIDSIZ+(b>>3)+FASTPALGRIDSIZ*FASTPALGRIDSIZ+FASTPALGRIDSIZ+1;
		int mindist = min(rdist[(coldist[r&7] & 0xFF)+64+8],gdist[(coldist[g&7] & 0xFF)+64+8]);
		mindist = min(mindist,bdist[(coldist[b&7] & 0xFF)+64+8]);
		mindist++;

		r = 64-r; g = 64-g; b = 64-b;
		
		retcol = -1;
		for(k=26;k>=0;k--)
		{
			i = colscan[k] + j; 
			if ((colhere[i >> 3] & pow2char[i & 7]) == 0)
				continue;
			
			i = colhead[i] & 0xFF;
			do
			{
				pal1 = i * 3;
				dist = gdist[(palette[pal1 + 1] & 0xFF)+g];
				if (dist < mindist)
				{
					dist += rdist[(palette[pal1] & 0xFF)+r];
					if (dist < mindist)
					{
						dist += bdist[(palette[pal1 + 2] & 0xFF)+b];
						if (dist < mindist) { mindist = dist; retcol = (byte)i; }
					}
				}
				i = colnext[i];
			} while (i >= 0);
		}
		if (retcol >= 0) 
			return retcol;

		mindist = 0x7fffffff;
		for(i=255;i>=0;i--,pal1-=3)
		{
			pal1 = i * 3;
			dist = gdist[(palette[pal1 + 1] & 0xFF) + g]; 
			if (dist >= mindist) 
				continue;

			dist += rdist[(palette[pal1] & 0xFF) + r]; 
			if (dist >= mindist) 
				continue;
			
			dist += bdist[(palette[pal1 + 2] & 0xFF) + b]; 
			if (dist >= mindist) 
				continue;
			
			mindist = dist; 
			retcol = (byte) i;
		}
		
		return retcol;
	}

	//////////SPRITE LIST MANIPULATION FUNCTIONS //////////

	public int insertspritesect(int sectnum)
	{
		short blanktouse;

		if ((sectnum >= MAXSECTORS) || (headspritesect[MAXSECTORS] == -1))
			return(-1);  //list full

		blanktouse = headspritesect[MAXSECTORS];

		headspritesect[MAXSECTORS] = nextspritesect[blanktouse];
		if (headspritesect[MAXSECTORS] >= 0)
			prevspritesect[headspritesect[MAXSECTORS]] = -1;

		prevspritesect[blanktouse] = -1;
		nextspritesect[blanktouse] = headspritesect[sectnum];
		if (headspritesect[sectnum] >= 0)
			prevspritesect[headspritesect[sectnum]] = blanktouse;
		headspritesect[sectnum] = blanktouse;

		sprite[blanktouse].sectnum = (short) sectnum;

		return(blanktouse);
	}

	public short insertspritestat(int newstatnum)
	{
		short blanktouse;

		if ((newstatnum >= MAXSTATUS) || (headspritestat[MAXSTATUS] == -1))
			return(-1);  //list full

		blanktouse = headspritestat[MAXSTATUS];

		headspritestat[MAXSTATUS] = nextspritestat[blanktouse];
		if (headspritestat[MAXSTATUS] >= 0)
			prevspritestat[headspritestat[MAXSTATUS]] = -1;

		prevspritestat[blanktouse] = -1;
		nextspritestat[blanktouse] = headspritestat[newstatnum];
		if (headspritestat[newstatnum] >= 0)
			prevspritestat[headspritestat[newstatnum]] = blanktouse;
		headspritestat[newstatnum] = blanktouse;

		sprite[blanktouse].statnum = (short) newstatnum;

		return(blanktouse);
	}

	public int insertsprite(int sectnum, int statnum)
	{
		insertspritestat(statnum);
		return(insertspritesect(sectnum));
	}

	public int deletesprite(int spritenum)
	{
		deletespritestat(spritenum);
		return(deletespritesect(spritenum));
	}

	public short changespritesect(int spritenum, int newsectnum)
	{
		if ((newsectnum < 0) || (newsectnum > MAXSECTORS)) return(-1);
		if (sprite[spritenum].sectnum == newsectnum) return(0);
		if (sprite[spritenum].sectnum == MAXSECTORS) return(-1);
		if (deletespritesect(spritenum) < 0) return(-1);
		insertspritesect(newsectnum);
		return(0);
	}
	
	public short changespritestat(int spritenum, int newstatnum)
	{
		if ((newstatnum < 0) || (newstatnum > MAXSTATUS)) return(-1);
		if (sprite[spritenum].statnum == newstatnum) return(0);
		if (sprite[spritenum].statnum == MAXSTATUS) return(-1);
		if (deletespritestat(spritenum) < 0) return(-1);
		insertspritestat(newstatnum);
		return(0);
	}

	public short deletespritesect(int spritenum)
	{
		if (sprite[spritenum].sectnum == MAXSECTORS)
			return(-1);

		if (headspritesect[sprite[spritenum].sectnum] == spritenum)
			headspritesect[sprite[spritenum].sectnum] = nextspritesect[spritenum];

		if (prevspritesect[spritenum] >= 0) nextspritesect[prevspritesect[spritenum]] = nextspritesect[spritenum];
		if (nextspritesect[spritenum] >= 0) prevspritesect[nextspritesect[spritenum]] = prevspritesect[spritenum];

		if (headspritesect[MAXSECTORS] >= 0) prevspritesect[headspritesect[MAXSECTORS]] = (short) spritenum;
		prevspritesect[spritenum] = -1;
		nextspritesect[spritenum] = headspritesect[MAXSECTORS];
		headspritesect[MAXSECTORS] = (short) spritenum;

		sprite[spritenum].sectnum = (short) MAXSECTORS;
		return(0);
	}
	
	public short deletespritestat (int spritenum)
	{
		if (sprite[spritenum].statnum == MAXSTATUS)
			return(-1);

		if (headspritestat[sprite[spritenum].statnum] == spritenum)
			headspritestat[sprite[spritenum].statnum] = nextspritestat[spritenum];

		if (prevspritestat[spritenum] >= 0) nextspritestat[prevspritestat[spritenum]] = nextspritestat[spritenum];
		if (nextspritestat[spritenum] >= 0) prevspritestat[nextspritestat[spritenum]] = prevspritestat[spritenum];

		if (headspritestat[MAXSTATUS] >= 0) prevspritestat[headspritestat[MAXSTATUS]] = (short) spritenum;
		prevspritestat[spritenum] = -1;
		nextspritestat[spritenum] = headspritestat[MAXSTATUS];
		headspritestat[MAXSTATUS] = (short) spritenum;

		sprite[spritenum].statnum = MAXSTATUS;
		return(0);
	}

	public boolean lintersect(int x1, int y1, int z1, int x2, int y2, int z2, int x3,
			int y3, int x4, int y4) { 
		
		// p1 to p2 is a line segment
		int x21 = x2 - x1, x34 = x3 - x4;
	    int y21 = y2 - y1, y34 = y3 - y4;
	    int bot = x21 * y34 - y21 * x34;
	    
	    if (bot == 0)
	        return false;
	    
	    int x31 = x3 - x1, y31 = y3 - y1;
	    int topt = x31 * y34 - y31 * x34;

		if (bot > 0) {
			if ((topt & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
				return false;
			int topu = x21 * y31 - y21 * x31;
			if ((topu & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
				return false;
		} else {
			if ((topt & 0xFFFFFFFFL) <= (bot & 0xFFFFFFFFL))
				return false;
			int topu = x21 * y31 - y21 * x31;
			if ((topu & 0xFFFFFFFFL) <= (bot & 0xFFFFFFFFL))
				return false;
		}
		long t = divscale(topt, bot, 24);

		intx = x1 + mulscale(x21, t, 24);
		inty = y1 + mulscale(y21, t, 24);
		intz = z1 + mulscale(z2 - z1, t, 24);

		return true;
	}
	
	public boolean lintersect(int x1, int y1, int x2, int y2,
			int x3, int y3, int x4, int y4)
	{
		// p1 to p2 is a line segment
		int x21 = x2 - x1, x34 = x3 - x4;
		int y21 = y2 - y1, y34 = y3 - y4;
		int bot = x21 * y34 - y21 * x34;
		int x31 = x3 - x1, y31 = y3 - y1;
		int topt = x31 * y34 - y31 * x34;
	
		if (bot == 0)
			return false;
		
		else if (bot > 0)
		{
			if ((topt & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
				return false;
			int topu = x21 * y31 - y21 * x31;
			if ((topu & 0xFFFFFFFFL) >= (bot & 0xFFFFFFFFL))
				return false;
		}
		else
		{
			if ((topt & 0xFFFFFFFFL) <= (bot & 0xFFFFFFFFL))
				return false;
			
			int topu = x21 * y31 - y21 * x31;
			if ((topu & 0xFFFFFFFFL) <= (bot & 0xFFFFFFFFL))
				return false;
		}
		
		return true;
	}

	public int rintersect(int x1, int y1, int z1, int vx, int vy, int vz, int x3,
			int y3, int x4, int y4) { //p1 towards p2 is a ray
		int x34, y34, x31, y31, bot, topt, topu, t;

		x34 = x3 - x4;
		y34 = y3 - y4;
		bot = vx * y34 - vy * x34;
		if (bot >= 0) {
			if (bot == 0)
				return (0);
			x31 = x3 - x1;
			y31 = y3 - y1;
			topt = x31 * y34 - y31 * x34;
			if (topt < 0)
				return (0);
			topu = vx * y31 - vy * x31;
			if ((topu < 0) || (topu >= bot))
				return (0);
		} else {
			x31 = x3 - x1;
			y31 = y3 - y1;
			topt = x31 * y34 - y31 * x34;
			if (topt > 0)
				return (0);
			topu = vx * y31 - vy * x31;
			if ((topu > 0) || (topu <= bot))
				return (0);
		}
		t = (int) divscale(topt, bot, 16);
		intx = x1 + mulscale(vx, t, 16);
		inty = y1 + mulscale(vy, t, 16);
		intz = z1 + mulscale(vz, t, 16);
		return (1);
	}

	public int keepaway_x, keepaway_y;

	public void keepaway(int x, int y, int w) {
		int dx, dy, ox, oy, x1, y1;
		int first;

		x1 = clipit[w].x1;
		dx = clipit[w].x2 - x1;
		y1 = clipit[w].y1;
		dy = clipit[w].y2 - y1;
		ox = ksgn(-dy);
		oy = ksgn(dx);
		first = (klabs(dx) <= klabs(dy) ? 1 : 0);
		keepaway_x = x;
		keepaway_y = y;
		while (true) {
			if (dx * (keepaway_y - y1) > (keepaway_x - x1) * dy)
				return;
			if (first == 0)
				keepaway_x += ox;
			else
				keepaway_y += oy;
			first ^= 1;
		}
	}

	public int raytrace(int x3, int y3, int x4, int y4) {
		int x1, y1, x2, y2, bot, topu, nintx, ninty, cnt, z, hitwall;
		int x21, y21, x43, y43;

		rayx = x4;
		rayy = y4;

		hitwall = -1;
		for (z = clipnum - 1; z >= 0; z--) {
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
					return (z);
				}
				nintx = x3 + scale(x43, topu, bot);
				ninty = y3 + scale(y43, topu, bot);
				topu--;
			} while (x21 * (ninty - y1) <= (nintx - x1) * y21);

			if (klabs(x3 - nintx) + klabs(y3 - ninty) < klabs(x3 - rayx) + klabs(y3 - rayy)) {
				rayx = nintx;
				rayy = ninty;
				hitwall = z;
			}
		}
		return (hitwall);
	}

	//
	// Exported Engine Functions
	//
	
	public void InitArrays()
	{
		sectbitmap = new byte[MAXSECTORS >> 3];
		palookupfog = new byte[MAXPALOOKUPS][3];
		pskyoff = new short[MAXPSKYTILES];
		zeropskyoff = new short[MAXPSKYTILES];
		spriteext = new Spriteext[MAXSPRITES + MAXUNIQHUDID];
		spritesmooth = new Spritesmooth[MAXSPRITES+MAXUNIQHUDID];
		tilesizx = new short[MAXTILES]; 
		tilesizy = new short[MAXTILES];
		picanm = new int[MAXTILES];
		show2dsector = new byte[(MAXSECTORS + 7) >> 3];
		show2dwall = new byte[(MAXWALLS + 7) >> 3];
		show2dsprite = new byte[(MAXSPRITES + 7) >> 3];
		sector = new SECTOR[MAXSECTORS];
		wall = new WALL[MAXWALLS];
		sprite = new SPRITE[MAXSPRITES];
		tsprite = new SPRITE[MAXSPRITESONSCREEN + 1];
		headspritesect = new short[MAXSECTORS + 1]; 
		headspritestat = new short[MAXSTATUS + 1];
		prevspritesect = new short[MAXSPRITES]; 
		prevspritestat = new short[MAXSPRITES];
		nextspritesect = new short[MAXSPRITES]; 
		nextspritestat = new short[MAXSPRITES];
		gotpic = new byte[(MAXTILES + 7) >> 3];
		gotsector = new byte[(MAXSECTORS + 7) >> 3];

		returnpal = new Palette();
		pHitInfo = new Hitscan();
		neartag = new Neartag();
		britable = new byte[16][256];
		picsiz = new int[MAXTILES];
		tilefilenum = new int[MAXTILES];
		tilefileoffs = new int[MAXTILES];
		sqrtable = new short[4096];
		shlookup = new short[4096 + 256];
		curpalette = new Palette[256];
		palfadergb = new FadeEffect(GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) {
			@Override
			public void update(int intensive) {}
		};
		tile2model = new Tile2model[MAXTILES + EXTRATILES];
		rxi = new int[8]; 
		ryi = new int[8];
		hitwalls = new int[4];
		clipit = new linetype[MAXCLIPNUM];
		clipsectorlist = new short[MAXCLIPNUM];
		clipobjectval = new int[MAXCLIPNUM];
		rdist = new int[129]; 
		gdist = new int[129]; 
		bdist = new int[129];
		colhere = new byte[((FASTPALGRIDSIZ + 2) * (FASTPALGRIDSIZ + 2) * (FASTPALGRIDSIZ + 2)) >> 3];
		colhead = new byte[(FASTPALGRIDSIZ + 2) * (FASTPALGRIDSIZ + 2) * (FASTPALGRIDSIZ + 2)];
		colnext = new byte[256];
		colscan = new int[27];
		radarang = new short[1280]; //1024
//		voxoff = new byte[MAXVOXELS][MAXVOXMIPS][];
		tiletovox = new int[MAXTILES];
		voxrotate = new boolean[MAXTILES]; 
		palette = new byte[768];
	
		for (int i = 0; i < spriteext.length; i++)
			spriteext[i] = new Spriteext();
		for (int i = 0; i < spritesmooth.length; i++)
			spritesmooth[i] = new Spritesmooth();
		
		palookup = new byte[MAXPALOOKUPS][];
		waloff = new byte[MAXTILES][];
		
		Arrays.fill(show2dsector, (byte)0);
		Arrays.fill(show2dsprite, (byte)0);
		Arrays.fill(show2dwall, (byte)0);
		Arrays.fill(tiletovox, -1);
//		Arrays.fill(voxscale, 65536);
		
		bakwindowx1 = new int[4]; 
		bakwindowy1 = new int[4];
		bakwindowx2 = new int[4]; 
		bakwindowy2 = new int[4];
	}
	
	public Engine(Message message, BAudio audio, boolean releasedEngine) throws Exception {
		this.releasedEngine = releasedEngine;
		this.message = message;
		if(audio == null) new Exception("BAudio == null!");
		this.audio = audio;
		InitArrays();

		loadtables();
		
		parallaxtype = 2;
		parallaxyoffs = 0;
		parallaxyscale = 65536;
		showinvisibility = false;

		pskybits = 0;
		paletteloaded = 0;
		automapping = 0;
		totalclock = 0;
		visibility = 512;
		parallaxvisibility = 512;

		loadpalette();

		if (!hicfirstinit) hicinit();
		
		initkeys();

		Console.setFunction(new DEFOSDFUNC(this));
		
		RegisterCvar(new OSDCOMMAND("usemodels",
			"usemodels: use md2 / md3 models", usemodels?1:0, 
			new OSDCVARFUNC() {
			@Override
			public void execute() {
				usemodels = Integer.parseInt(osd_argv[1]) == 1;
			}
		}, 0, 1));
		
		RegisterCvar(new OSDCOMMAND("usevoxels",
			"usevoxels: use voxels models", usevoxels?1:0, 
			new OSDCVARFUNC() {
			@Override
			public void execute() {
				usevoxels = Integer.parseInt(osd_argv[1]) == 1;
			}
		}, 0, 1));
		
		RegisterCvar(new OSDCOMMAND("usehightile",
				"usevoxels: use high tiles", usehightile?1:0, 
				new OSDCVARFUNC() {
				@Override
				public void execute() {
					usehightile = Integer.parseInt(osd_argv[1]) == 1;
					render.gltexinvalidateall(1);
				}
			}, 0, 1));
		
		OSDCOMMAND R_texture = new OSDCOMMAND( "r_texturemode", "r_texturemode: changes the texture filtering settings", new OSDCVARFUNC() { 
			@Override
			public void execute() {
				int gltexfiltermode = Console.Geti("r_texturemode");
				if (Console.osd_argc != 2) {
					Console.Println("Current texturing mode is " + gltexfiltermode);
					return;
				}
				try {
					int value = Integer.parseInt(osd_argv[1]);
					if(value >= 2) value = 5; //set to trilinear
					if(Console.Set("r_texturemode", value)) {
						render.gltexapplyprops();
						Console.Println("Texture filtering mode changed to " + value);
					} else Console.Println("Texture filtering mode out of range");
				} 
				catch(Exception e)
				{
					Console.Println("r_texturemode: Out of range");
				}
			} });
		R_texture.setRange(0, 5);
		Console.RegisterCvar(R_texture);
		
		Console.RegisterCvar(new  OSDCOMMAND( "r_detailmapping", "r_detailmapping: enable/disable detail mapping", 1, 0, 1));
		Console.RegisterCvar(new  OSDCOMMAND( "r_vbocount", "r_vbocount: sets the number of Vertex Buffer Objects to use when drawing models", 64, 0, 256));

		randomseed = 1; //random for voxels
	}

	public void uninit()
	{
		int i;

		if(render != null)
			render.uninit();

		if (artfil != -1)
			kClose(artfil);

		for (i = 0; i < MAXPALOOKUPS; i++)
			if (palookup[i] != null) 
				palookup[i] = null;
		if(message != null)
			message.dispose();
		
		audio.dispose();
		
		uninitmultiplayer();
	}

	public void initspritelists()
	{
		int i;

		for (i=0;i<MAXSECTORS;i++)     //Init doubly-linked sprite sector lists
			headspritesect[i] = -1;
		headspritesect[MAXSECTORS] = 0;
		
		for(i=0;i<MAXSPRITES;i++)
		{
			sprite[i] = new SPRITE();
			prevspritesect[i] = (short) (i-1);
			nextspritesect[i] = (short) (i+1);
			sprite[i].sectnum = (short) MAXSECTORS;
		}
		prevspritesect[0] = -1;
		nextspritesect[MAXSPRITES-1] = -1;


		for(i=0;i<MAXSTATUS;i++)      //Init doubly-linked sprite status lists
			headspritestat[i] = -1;
		headspritestat[MAXSTATUS] = 0;
		for(i=0;i<MAXSPRITES;i++)
		{
			prevspritestat[i] = (short) (i-1);
			nextspritestat[i] = (short) (i+1);
			sprite[i].statnum = (short) MAXSTATUS;
		}
		prevspritestat[0] = -1;
		nextspritestat[MAXSPRITES-1] = -1;
		
		for(i=0;i<MAXSPRITESONSCREEN;i++)
		{
			tsprite[i] = new SPRITE();
		}
	}

	public int drawrooms(float daposx, float daposy, float daposz,
			float daang, float dahoriz, int dacursectnum) {

		beforedrawrooms = 0;
		indrawroomsandmasks = 1;

		globalposx = (int) daposx;
		globalposy = (int) daposy;
		globalposz = (int) daposz;

		globalang = BClampAngle(daang);

		globalhoriz = ((dahoriz - 100) * xdimenscale / viewingrange) + (ydimen >> 1);
		pitch = (float)(-getangle(160, (int)(dahoriz-100))) / (2048.0f / 360.0f);

		globalvisibility = scale(visibility<<2, xdimen, 1680);

		globalcursectnum = (short) dacursectnum;
		totalclocklock = totalclock;

		cosglobalang = (int) BCosAngle(globalang);
		singlobalang = (int) BSinAngle(globalang);
		 
		cosviewingrangeglobalang = mulscale(cosglobalang, viewingrange, 16);
		sinviewingrangeglobalang = mulscale(singlobalang, viewingrange, 16);

		Arrays.fill(gotpic, (byte)0);
		Arrays.fill(gotsector, (byte)0);

		render.drawrooms();
		return 0;
	}

	public void drawmasks() {
		render.drawmasks();
	}

	public void drawmapview(int dax, int day, int zoome, int ang) {
		render.drawmapview(dax, day, zoome, ang);
	}

	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		render.drawoverheadmap(cposx, cposy, czoom, cang);
	}

	public int mapversion;
	public int loadboard(String filename, int[] daposx, int[] daposy, int[] daposz,
			short[] daang, short[] dacursectnum) {
		int fil, i;

		i = 0;
		
		if ((fil = kOpen(filename, i)) == -1)
			{ mapversion = 7; return(-1); }

		byte[] buf = new byte[4];
		kRead(fil, buf, 4); mapversion = LittleEndian.getInt(buf);
		if(mapversion == 6) 
			return loadoldboard(fil, daposx, daposy, daposz, daang, dacursectnum);
		
		if(mapversion != 7)
		{
			Console.Println("Invalid map version!");
			kClose(fil);
			return(-1);
		}
		
		initspritelists();
			
		Arrays.fill(show2dsector, (byte)0);
		Arrays.fill(show2dsprite, (byte)0);
		Arrays.fill(show2dwall, (byte)0);

		kRead(fil, buf, 4); daposx[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposy[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposz[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 2); daang[0] = LittleEndian.getShort(buf);
		kRead(fil, buf, 2); dacursectnum[0] = LittleEndian.getShort(buf);
		
		kRead(fil, buf, 2); numsectors = LittleEndian.getShort(buf);
		byte[] sectors = new byte[SECTOR.sizeof * numsectors];
		kRead(fil, sectors, sectors.length);
		ByteBuffer bb = ByteBuffer.wrap(sectors);
		byte[] sectorReader = new byte[SECTOR.sizeof];
		for (i = 0; i < numsectors; i++) {
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

		for(i=0;i<numsprites;i++) 
			insertsprite(sprite[i].sectnum, sprite[i].statnum);

		//Must be after loading sectors, etc!
		dacursectnum[0] = updatesector(daposx[0], daposy[0], (short) dacursectnum[0]);
	
		kClose(fil);
		
		return(0);
	}
	
	public int loadoldboard(int fil, int[] daposx, int[] daposy, int[] daposz,
			short[] daang, short[] dacursectnum) {

		initspritelists();
		
		Arrays.fill(show2dsector, (byte)0);
		Arrays.fill(show2dsprite, (byte)0);
		Arrays.fill(show2dwall, (byte)0);
		byte[] buf = new byte[4];
		
		kRead(fil, buf, 4); daposx[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposy[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 4); daposz[0] = LittleEndian.getInt(buf);
		kRead(fil, buf, 2); daang[0] = LittleEndian.getShort(buf);
		kRead(fil, buf, 2); dacursectnum[0] = LittleEndian.getShort(buf);
		
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

		for(int i=0;i<numsprites;i++) {
			insertsprite(sprite[i].sectnum, sprite[i].statnum);
		}
	
		//Must be after loading sectors, etc!
		dacursectnum[0] = updatesector(daposx[0], daposy[0], (short) dacursectnum[0]);
	
		kClose(fil);

		return 0;
	}

	// JBF: davidoption now functions as a windowed-mode flag (0 == windowed, 1 == fullscreen)
//	public byte videomodereset;

	public boolean setgamemode(int davidoption, int daxdim, int daydim) {
		daxdim = max(320, daxdim);
		daydim = max(200, daydim);

		if (/*(videomodereset == 0) &&*/
				(davidoption == fullscreen) && (xdim == daxdim) && (ydim == daydim))
			return true;

		//	    g_lastpalettesum = 0;

		// Workaround possible bugs in the GL driver
		//	    glrendmode = rendmode;
		//	    rendmode = glrendmode;    // GL renderer

		xdim = daxdim;
		ydim = daydim;

//		j = ydim * 4; //Leave room for horizlookup&horizlookup2
//		lookups = new int[2 * j];

		//Force drawrooms to call dosetaspect & recalculate stuff
//		oxyaspect = oxdimen = oviewingrange = -1;

		setview(0, 0, xdim - 1, ydim - 1);
		clearview(0);
		setbrightness(curbrightness, palette, 0);
		
		Console.ResizeDisplay(daxdim, daydim);

		render.uninit();
		render.init();
		
		if(Gdx.app.getType() == ApplicationType.Android) {
			daxdim = Gdx.graphics.getWidth();
			daydim = Gdx.graphics.getHeight();
			Gdx.graphics.setWindowedMode(daxdim, daydim);
			return true;
		}
		
		if(davidoption == 1)
		{
			DisplayMode m = null;
			for(DisplayMode mode: Gdx.graphics.getDisplayModes()) {
				if(Gdx.graphics.getDisplayMode().refreshRate == mode.refreshRate)
				{
					if(mode.width == daxdim && mode.height == daydim)
						m = mode;
				}
			}
			
			if(m == null) {
				Console.Println("Warning: " + daxdim + "x" + daydim + " fullscreen not support", OSDTEXT_YELLOW);
				Gdx.graphics.setWindowedMode(daxdim, daydim);
				return false;
			} else Gdx.graphics.setFullscreenMode(m);
		} else Gdx.graphics.setWindowedMode(daxdim, daydim);

		return true;
	}

	public void inittimer(int tickspersecond) {
		if (timerfreq != 0)
			return; // already installed

		timerfreq = 1000;
		timerticspersec = tickspersecond;
		timerlastsample = getticks() * timerticspersec / timerfreq;
	}

	public void sampletimer() {
		if (timerfreq == 0)
			return;

		int n = (int) ((getticks() * timerticspersec / timerfreq) - timerlastsample);  

		if (n > 0) {
			totalclock += n;
			timerlastsample += n;
		}
	}

	public long getticks() {
		return System.currentTimeMillis();
	}
	
	HashMap<String, FadeEffect> fades;
	public void registerFade(String fadename, FadeEffect effect) {
		if(fades == null) fades = new HashMap<String, FadeEffect>();
		fades.put(fadename, effect);
	}
	
	public void updateFade(String fadename, int intensive)
	{
		FadeEffect effect = fades.get(fadename);
		if(effect != null)
			effect.update(intensive);
	}

	public void showfade() {
		render.palfade(fades);
	}
	
	public void loadpic(String filename)
	{
		int fil = -1; byte[] buf = new byte[4];
		
		if ((fil = kOpen(filename, 0)) != -1) {
			kRead(fil, buf, 4);

			artversion = LittleEndian.getInt(buf);
			if (artversion != 1)
				return;
			kRead(fil, buf, 4);
			numtiles = LittleEndian.getInt(buf);
			kRead(fil, buf, 4);
			int localtilestart = LittleEndian.getInt(buf);
			kRead(fil, buf, 4);
			int localtileend = LittleEndian.getInt(buf);
			int k = localtilestart / (localtileend - localtilestart);

			for (int i = localtilestart; i <= localtileend; i++) {
				kRead(fil, buf, 2);
				tilesizx[i] = LittleEndian.getShort(buf);
			}
			for (int i = localtilestart; i <= localtileend; i++) {
				kRead(fil, buf, 2);
				tilesizy[i] = LittleEndian.getShort(buf);
			}
			for (int i = localtilestart; i <= localtileend; i++) {
				kRead(fil, buf, 4);
				picanm[i] = LittleEndian.getInt(buf);
			}
			int offscount = 4 + 4 + 4 + 4 + ((localtileend - localtilestart + 1) << 3);
			for (int i = localtilestart; i <= localtileend; i++) {
				tilefilenum[i] = k;
				tilefileoffs[i] = offscount;
				int dasiz = tilesizx[i] * tilesizy[i];
				offscount += dasiz;
				waloff[i] = null;
			}
			
			klseek(fil, tilefileoffs[localtilestart], SEEK_SET);
			for (int i = localtilestart; i <= localtileend; i++) {
				int dasiz = tilesizx[i] * tilesizy[i];
				waloff[i] = new byte[dasiz];
				kRead(fil, waloff[i], dasiz);
				setpicsiz(i);
			}
			kClose(fil);
		}
	}
	
	public void setpicsiz(int tilenum)
	{
		int j = 15;
		while ((j > 1) && (pow2long[j] > tilesizx[tilenum]))
			j--;
		picsiz[tilenum] = j;
		j = 15;
		while ((j > 1) && (pow2long[j] > tilesizy[tilenum]))
			j--;
		picsiz[tilenum] += (j << 4);
	}

	public int loadpics(String filename) {
		int offscount, localtilestart, localtileend, dasiz;
		int fil, i, k;

		filename.getChars(0, filename.length(), artfilename, 0);

		numtilefiles = 0;
		byte[] buf = new byte[4];
		do {
			k = numtilefiles;

			artfilename[7] = (char) ((k % 10) + 48);
			artfilename[6] = (char) (((k / 10) % 10) + 48);
			artfilename[5] = (char) (((k / 100) % 10) + 48);
			String name = String.copyValueOf(artfilename);

			if ((fil = kOpen(name, 0)) != -1) {
				if (render == null) //first load
					Console.Println("Loading " + name + "...");
				kRead(fil, buf, 4);
				artversion = LittleEndian.getInt(buf);
				if (artversion != 1)
					return (-1);
				kRead(fil, buf, 4);
				numtiles = LittleEndian.getInt(buf);
				kRead(fil, buf, 4);
				localtilestart = LittleEndian.getInt(buf);
				kRead(fil, buf, 4);
				localtileend = LittleEndian.getInt(buf);

				for (i = localtilestart; i <= localtileend; i++) {
					kRead(fil, buf, 2);
					tilesizx[i] = LittleEndian.getShort(buf);
				}
				for (i = localtilestart; i <= localtileend; i++) {
					kRead(fil, buf, 2);
					tilesizy[i] = LittleEndian.getShort(buf);
				}
				for (i = localtilestart; i <= localtileend; i++) {
					kRead(fil, buf, 4);
					picanm[i] = LittleEndian.getInt(buf);
				}
				offscount = 4 + 4 + 4 + 4 + ((localtileend - localtilestart + 1) << 3);
				for (i = localtilestart; i <= localtileend; i++) {
					tilefilenum[i] = k;
					tilefileoffs[i] = offscount;
					dasiz = tilesizx[i] * tilesizy[i];
					offscount += dasiz;
				}
				kClose(fil);

				numtilefiles++;
			}
		} while (k != numtilefiles);

		for (i = 0; i < MAXTILES; i++)
			setpicsiz(i);

		if (artfil != -1)
			kClose(artfil);
		artfil = -1;
		artfilnum = -1;
		artfilplc = 0;
		
		return (numtilefiles);
	}

	public byte[] loadtile(int tilenume) {
		int i, dasiz;

		
		if (tilenume >= MAXTILES)
			return null;
		dasiz = tilesizx[tilenume] * tilesizy[tilenume];
		
		if (dasiz <= 0)
			return null;

		i = tilefilenum[tilenume];

		if (i != artfilnum) {
			if (artfil != -1)
				kClose(artfil);
			artfilnum = (int) i;
			artfilplc = 0;

			artfilename[7] = (char) ((i % 10) + 48);
			artfilename[6] = (char) (((i / 10) % 10) + 48);
			artfilename[5] = (char) (((i / 100) % 10) + 48);

			artfil = kOpen(new String(artfilename), 0);

			faketimerhandler();
		}
		
		if(artfil == -1)
			return null;

		if (waloff[tilenume] == null) 
			waloff[tilenume] = new byte[dasiz];

		if (artfilplc != tilefileoffs[tilenume]) {
			klseek(artfil, tilefileoffs[tilenume] - artfilplc, SEEK_CUR);
			faketimerhandler();
		}

		if(kRead(artfil, waloff[tilenume], dasiz) == -1)
			return null;
		
		faketimerhandler();
		artfilplc = (int) (tilefileoffs[tilenume] + dasiz);

		return waloff[tilenume];
	}

	public byte[] allocatepermanenttile(int tilenume, int xsiz, int ysiz) {
		if ((xsiz <= 0) || (ysiz <= 0) || (tilenume >= MAXTILES))
			return null;

		int dasiz = xsiz * ysiz;

		waloff[tilenume] = new byte[dasiz];

		tilesizx[tilenume] = (short) xsiz;
		tilesizy[tilenume] = (short) ysiz;
		picanm[tilenume] = 0;

		setpicsiz(tilenume);

		return (waloff[tilenume]);
	}

	public int clipinsidebox(int x, int y, short wallnum, int walldist) {
		WALL wal;
		int x1, y1, x2, y2, r;

		r = (walldist << 1);
		wal = wall[wallnum];
		if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) return 0;
		x1 = wal.x + walldist - x;
		y1 = wal.y + walldist - y;
		wal = wall[wal.point2];
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

	public int inside(int x, int y, short sectnum) {
		WALL wal;
		int i, x1, y1, x2, y2;
		int cnt;
		int wallid;
		if ((sectnum < 0) || (sectnum >= numsectors))
			return (-1);

		cnt = 0;
		wallid = sector[sectnum].wallptr;
		i = sector[sectnum].wallnum;

		if(wallid < 0) return -1;
		do {
			wal = wall[wallid];
			if (wal == null || wal.point2 < 0 || wall[wal.point2] == null)
				return -1;
			y1 = wal.y - y;
			y2 = wall[wal.point2].y - y;

			if ((y1 ^ y2) < 0) {
				x1 = wal.x - x;
				x2 = wall[wal.point2].x - x;
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

	public short getangle(int xvect, int yvect) {
		if (releasedEngine) {
			if ((xvect | yvect) == 0)
				return (0);
			if (xvect == 0)
				return (short) (512 + ((yvect < 0 ? 1 : 0) << 10));
			if (yvect == 0)
				return (short) ((xvect < 0 ? 1 : 0) << 10);
			if (xvect == yvect)
				return (short) (256 + ((xvect < 0 ? 1 : 0) << 10));
			if (xvect == -yvect)
				return (short) (768 + ((xvect > 0 ? 1 : 0) << 10));

			if (klabs(xvect) > klabs(yvect)) {
				return (short) (((radarang[640 + scale(160, yvect, xvect)] >> 6) + ((xvect < 0 ? 1 : 0) << 10)) & 2047);
			}
			return (short) (((radarang[640 - scale(160, xvect, yvect)] >> 6) + 512 + ((yvect < 0 ? 1 : 0) << 10)) & 2047);
		} else {
			if ((xvect | yvect) == 0)
				return (0);
			if (xvect == 0)
				return (short) (512 + ((yvect < 0 ? 1 : 0) << 10));
			if (yvect == 0)
				return (short) ((xvect < 0 ? 1 : 0) << 10);
			if (xvect == yvect)
				return (short) (256 + ((xvect < 0 ? 1 : 0) << 10));
			if (xvect == -yvect)
				return (short) (768 + ((xvect > 0 ? 1 : 0) << 10));

			if (klabs(xvect) > klabs(yvect)) {
				return (short) (((radarang[160 + scale(160, yvect, xvect)] >> 6) + ((xvect < 0 ? 1 : 0) << 10)) & 2047);
			}
			return (short) (((radarang[160 - scale(160, xvect, yvect)] >> 6) + 512 + ((yvect < 0 ? 1 : 0) << 10)) & 2047);
		}
	}

	public int ksqrt(int a) {
		if(compatibleMode) {
			long out = a & 0xFFFFFFFFL;
			int value;
			if ( (out & 0xFF000000) != 0 )
				value = shlookup[(int) ((out >> 24) + 4096)] & 0xFFFF;
			else
				value = shlookup[(int) (out >> 12)] & 0xFFFF;
			
			out >>= value & 0xff;				
			out = (out & 0xffff0000) | (sqrtable[(int) out] & 0xFFFF);	
			out >>= ((value & 0xff00) >> 8);		
								
			return (int) out;
		} else return (int) sqrt(a & 0xFFFFFFFFL);
	}

	private static int SETSPRITEZ = 0;
	public short setsprite(int spritenum, int newx, int newy, int newz)
	{
		short tempsectnum;

		sprite[spritenum].x = newx;
		sprite[spritenum].y = newy;
		sprite[spritenum].z = newz;

		tempsectnum = sprite[spritenum].sectnum;
		if(SETSPRITEZ == 1)
			tempsectnum = updatesectorz(newx,newy,newz,tempsectnum);
		else
			tempsectnum = updatesector(newx,newy,tempsectnum);
		if (tempsectnum < 0)
			return(-1);
		if (tempsectnum != sprite[spritenum].sectnum)
			changespritesect(spritenum,tempsectnum);

		return(0);
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

		int wallid = sector[sectnum].wallptr;
		i = sector[sectnum].wallnum;
		do {
			wal = wall[wallid];
			if (wal.nextsector >= 0) {
				if (topbottom == 1) {
					testz = sector[wal.nextsector].floorz;
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
					testz = sector[wal.nextsector].ceilingz;
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

	public int cansee(int x1, int y1, int z1, short sect1, int x2, int y2, int z2, short sect2) {
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
			sec = sector[dasectnum];

			if(sec == null) continue;
			int startwall = sec.wallptr;
			int endwall = startwall + sec.wallnum - 1;
			if(startwall < 0 || endwall < 0) continue;
			for (int w = startwall; w <= endwall; w++) {
				wal = wall[w];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = wall[wal.point2];
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

				getzsofslope((short) dasectnum, x, y);
				if ((z <= ceilzsofslope) || (z >= floorzsofslope))
					return (0);
				getzsofslope((short) nexts, x, y);
				if ((z <= ceilzsofslope) || (z >= floorzsofslope))
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

	public int hitscan(int xs, int ys, int zs, short sectnum, int vx, int vy, int vz,
			Hitscan hit, int cliptype) {
		SECTOR sec;
		WALL wal, wal2;
		SPRITE spr;
		int z, zz, x1, y1 = 0, z1 = 0, x2, y2, x3, y3, x4, y4;
		int intx, inty, intz;

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
			sec = sector[dasector];
			if(sec == null) break;
			x1 = 0x7fffffff;
			if ((sec.ceilingstat & 2) != 0) {
				wal = wall[sec.wallptr];
				wal2 = wall[wal.point2];
				dax = wal2.x - wal.x;
				day = wal2.y - wal.y;
				i = (int) ksqrt(dax * dax + day * day);
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
				wal = wall[sec.wallptr];
				wal2 = wall[wal.point2];
				dax = wal2.x - wal.x;
				day = wal2.y - wal.y;
				i = (int) ksqrt(dax * dax + day * day);
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
			int out;
			for (z = startwall; z < endwall; z++) {
				wal = wall[z];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = wall[wal.point2];
				if(wal2 == null) continue;
				x1 = wal.x;
				y1 = wal.y;
				x2 = wal2.x;
				y2 = wal2.y;

				if ((x1 - xs) * (y2 - ys) < (x2 - xs) * (y1 - ys))
					continue;
				out = rintersect(xs, ys, zs, vx, vy, vz, x1, y1, x2, y2);
				intx = Engine.intx;
				inty = Engine.inty;
				intz = Engine.intz;

				if (out == 0)
					continue;

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
				getzsofslope(nextsector, intx, inty);
				if ((intz <= ceilzsofslope) || (intz >= floorzsofslope)) {
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

			for (z = headspritesect[dasector]; z >= 0; z = nextspritesect[z]) {
				spr = sprite[z];
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

					out = rintersect(xs, ys, zs, vx, vy, vz, x1, y1, x2, y2);
					intx = Engine.intx;
					inty = Engine.inty;
					intz = Engine.intz;
					if (out == 0)
						continue;

					if (klabs(intx - xs) + klabs(inty - ys) > klabs((hit.hitx) - xs) + klabs((hit.hity) - ys))
						continue;

					k = ((tilesizy[spr.picnum] * spr.yrepeat) << 2);
					if ((cstat & 128) != 0)
						ceilzsofslope = spr.z + (k >> 1);
					else
						ceilzsofslope = spr.z;
					if ((picanm[spr.picnum] & 0x00ff0000) != 0)
						ceilzsofslope -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
					if ((intz < ceilzsofslope) && (intz > ceilzsofslope - k)) {
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

	public void nextpage() {
		Console.draw();
		render.nextpage();
		audio.update();
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

		do {
			dasector = clipsectorlist[tempshortcnt];

			startwall = sector[dasector].wallptr;
			endwall = (short) (startwall + sector[dasector].wallnum - 1);
			for (z = startwall; z <= endwall; z++) {
				wal = wall[z];
				wal2 = wall[wal.point2];
				x1 = wal.x;
				y1 = wal.y;
				x2 = wal2.x;
				y2 = wal2.y;

				nextsector = wal.nextsector;

				good = 0;
				if (nextsector >= 0) {
					if (((tagsearch & 1) != 0) && sector[nextsector].lotag != 0)
						good |= 1;
					if (((tagsearch & 2) != 0) && sector[nextsector].hitag != 0)
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

				if (lintersect(xs, ys, zs, xe, ye, ze, x1, y1, x2, y2)) {
					if (good != 0) {
						if ((good & 1) != 0)
							near.tagsector = nextsector;
						if ((good & 2) != 0)
							near.tagwall = z;
						near.taghitdist = dmulscale(intx - xs, sintable[(ange + 2560) & 2047], inty - ys, sintable[(ange + 2048) & 2047], 14);
						xe = intx;
						ye = inty;
						ze = intz;
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

			for (z = headspritesect[dasector]; z >= 0; z = nextspritesect[z]) {
				spr = sprite[z];

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
							intz = zs + scale(vz, topt, bot);
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
									intx = xs + scale(vx, topt, bot);
									inty = ys + scale(vy, topt, bot);
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

	public long qdist(long dx, long dy) {
		dx = abs(dx);
		dy = abs(dy);

		if (dx > dy)
			dy = (3 * dy) >> 3;
		else
			dx = (3 * dx) >> 3;

		return dx + dy;
	}

	public void dragpoint(int pointhighlight, int dax, int day) {
		short cnt, tempshort;

		wall[pointhighlight].x = dax;
		wall[pointhighlight].y = day;

		cnt = (short) MAXWALLS;
		tempshort = (short) pointhighlight; //search points CCW
		do {
			if (wall[tempshort].nextwall >= 0) {
				tempshort = wall[wall[tempshort].nextwall].point2;
				wall[tempshort].x = dax;
				wall[tempshort].y = day;
			} else {
				tempshort = (short) pointhighlight; //search points CW if not searched all the way around
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

	public int lastwall(int point) {
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

	public void addclipline(int dax1, int day1, int dax2, int day2, int daoval) {
		if (clipnum < MAXCLIPNUM) {
			if (clipit[clipnum] == null)
				clipit[clipnum] = new linetype();
			clipit[clipnum].x1 = dax1;
			clipit[clipnum].y1 = day1;
			clipit[clipnum].x2 = dax2;
			clipit[clipnum].y2 = day2;
			clipobjectval[clipnum] = daoval;
			clipnum++;
		}
	}

	public static int clipmove_x, clipmove_y, clipmove_z, clipmove_sectnum;

	public int clipmove(int x, int y, int z, int sectnum,
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
		rad = (int) (ksqrt(gx * gx + gy * gy) + MAXCLIPDIST + walldist + 8);
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
			sec = sector[dasect];
			startwall = sec.wallptr;
			endwall = startwall + sec.wallnum;
			if(startwall < 0 || endwall < 0) { clipsectcnt++; continue; }
			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				wal2 = wall[wal.point2];
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
					int out = rintersect(clipmove_x, clipmove_y, 0, gx, gy, 0, x1, y1, x2, y2);
					dax = Engine.intx;
					day = Engine.inty;
					daz = Engine.intz;

					if (out == 0) {
						dax = clipmove_x;
						day = clipmove_y;
					}
					daz = (int) getflorzofslope((short) dasect, dax, day);
					daz2 = (int) getflorzofslope(wal.nextsector, dax, day);

					sec2 = sector[wal.nextsector];
					if(sec2 == null) continue;
					if (daz2 < daz - (1 << 8))
						if ((sec2.floorstat & 1) == 0)
							if ((clipmove_z) >= daz2 - (flordist - 1))
								clipyou = 1;
					if (clipyou == 0) {
						daz = (int) getceilzofslope((short) dasect, dax, day);
						daz2 = (int) getceilzofslope(wal.nextsector, dax, day);
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

			for (j = headspritesect[dasect]; j >= 0; j = nextspritesect[j]) {
				spr = sprite[j];

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
			hitwall = raytrace(clipmove_x, clipmove_y, goalx, goaly);
			intx = rayx;
			inty = rayy;
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

				keepaway(goalx, goaly, hitwall);
				goalx = keepaway_x;
				goaly = keepaway_y;
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
		for (j = numsectors - 1; j >= 0; j--)
			if (inside(clipmove_x, clipmove_y, (short) j) == 1) {
				if ((sector[j].ceilingstat & 2) != 0)
					templong2 = (int) (getceilzofslope((short) j, clipmove_x, clipmove_y) - (clipmove_z));
				else
					templong2 = (sector[j].ceilingz - (clipmove_z));

				if (templong2 > 0) {
					if (templong2 < templong1) {
						clipmove_sectnum = j;
						templong1 = templong2;
					}
				} else {
					if ((sector[j].floorstat & 2) != 0)
						templong2 = (int) ((clipmove_z) - getflorzofslope((short) j, clipmove_x, clipmove_y));
					else
						templong2 = ((clipmove_z) - sector[j].floorz);

					if (templong2 <= 0) {
						clipmove_sectnum = j;
						return (retval);
					}
					if (templong2 < templong1) {
						clipmove_sectnum = j;
						templong1 = templong2;
					}
				}
			}

		return (retval);
	}

	public static int pushmove_x, pushmove_y, pushmove_z, pushmove_sectnum;

	public int pushmove(int x, int y, int z, int sectnum,
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
		//dasprclipmask = (cliptype>>16);

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

				sec = sector[clipsectorlist[clipsectcnt]];
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
					wal = wall[i];
					if (clipinsidebox(pushmove_x, pushmove_y, (short) i, walldist - 4) == 1) {
						j = 0;
						if (wal.nextsector < 0)
							j = 1;
						if ((wal.cstat & dawalclipmask) != 0)
							j = 1;
						if (j == 0) {
							sec2 = sector[wal.nextsector];

							//Find closest point on wall (dax, day) to (*x, *y)
							dax = wall[wal.point2].x - wal.x;
							day = wall[wal.point2].y - wal.y;
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

							daz = (int) getflorzofslope(clipsectorlist[clipsectcnt], dax, day);
							daz2 = (int) getflorzofslope(wal.nextsector, dax, day);
							if(sec2 == null) continue;
							if ((daz2 < daz - (1 << 8)) && ((sec2.floorstat & 1) == 0))
								if (pushmove_z >= daz2 - (flordist - 1))
									j = 1;

							daz = (int) getceilzofslope(clipsectorlist[clipsectcnt], dax, day);
							daz2 = (int) getceilzofslope(wal.nextsector, dax, day);
							if ((daz2 > daz + (1 << 8)) && ((sec2.ceilingstat & 1) == 0))
								if (pushmove_z <= daz2 + (ceildist - 1))
									j = 1;
						}
						if (j != 0) {
							j = getangle(wall[wal.point2].x - wal.x, wall[wal.point2].y - wal.y);
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

	public short updatesector(int x, int y, int sectnum) {
		WALL wal;
		int i, j, wallid;

		if (inside(x, y, (short) sectnum) == 1)
			return (short) sectnum;

		if ((sectnum >= 0) && (sectnum < numsectors)) {
			wallid = sector[sectnum].wallptr;
			j = sector[sectnum].wallnum;
			if(wallid < 0) return -1;
			do {
				if(wallid >= MAXWALLS) break;
				wal = wall[wallid];
				if(wal == null) { wallid++; j--; continue; }
				i = wal.nextsector;
				if (i >= 0)
					if (inside(x, y, (short) i) == 1) {
						return (short) i;
					}
				wallid++;
				j--;
			} while (j != 0);
		}

		for (i = numsectors - 1; i >= 0; i--)
			if (inside(x, y, (short) i) == 1) {
				return (short) i;
			}

		return -1;
	}

	public short updatesectorz(int x, int y, int z, short sectnum) {
		WALL wal;
		long i, j;
		int wallid;

		getzsofslope(sectnum, x, y);
		if ((z >= ceilzsofslope) && (z <= floorzsofslope))
			if (inside(x, y, sectnum) != 0)
				return sectnum;

		if ((sectnum >= 0) && (sectnum < numsectors)) {
			if(sector[sectnum] == null) return -1;
			wallid = sector[sectnum].wallptr;
			j = sector[sectnum].wallnum;
			do {
				if(wallid >= MAXWALLS) break;
				wal = wall[wallid];
				if(wal == null) { wallid++; j--; continue; }
				i = wal.nextsector;
				if (i >= 0) {
					getzsofslope((short) i, x, y);
					if ((z >= ceilzsofslope) && (z <= floorzsofslope))
						if (inside(x, y, (short) i) == 1) {
							return (short) i;
						}
				}
				wallid++;
				j--;
			} while (j != 0);
		}

		for (i = numsectors - 1; i >= 0; i--) {
			getzsofslope((short) i, x, y);
			if ((z >= ceilzsofslope) && (z <= floorzsofslope))
				if (inside(x, y, (short) i) == 1) {
					return (short) i;
				}
		}

		return -1;
	}

	public static Vector2 rotatepoint = new Vector2();

	public void rotatepoint(int xpivot, int ypivot, int x, int y, short daang) {
		int dacos, dasin;

		dacos = sintable[(daang + 2560) & 2047];
		dasin = sintable[(daang + 2048) & 2047];
		x -= xpivot;
		y -= ypivot;
		rotatepoint.x = dmulscale(x, dacos, -y, dasin, 14) + xpivot;
		rotatepoint.y = dmulscale(y, dacos, x, dasin, 14) + ypivot;
	}

	//	public boolean buttonPressed;
	public void initmouse() {
		Gdx.input.setCursorCatched(true);
	}
	
	public void srand(int seed)
	{
		randomseed = seed;
	}
	
	public int getrand()
	{
		return randomseed;
	}

	public int krand() {
		randomseed = (randomseed * 27584621) + 1;
		return (int) ((randomseed&0xFFFFFFFFL) >> 16);
	}
	
	public int rand()
	{
		return (int) (Math.random() * 32767);
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

		getzsofslope(sectnum, x, y);
		zr_ceilz = ceilzsofslope;
		zr_florz = floorzsofslope;

		zr_ceilhit = sectnum + 16384;
		zr_florhit = sectnum + 16384;

		dawalclipmask = (cliptype & 65535);
		dasprclipmask = (cliptype >> 16);

		clipsectorlist[0] = sectnum;
		clipsectcnt = 0;
		clipsectnum = 1;

		do //Collect sectors inside your square first
		{
			sec = sector[clipsectorlist[clipsectcnt]];
			startwall = sec.wallptr;
			endwall = startwall + sec.wallnum;
			if(startwall < 0 || endwall < 0) { clipsectcnt++; continue; }
			for (j = startwall; j < endwall; j++) {
				wal = wall[j];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				k = wal.nextsector;
				if (k >= 0) {
					wal2 = wall[wal.point2];
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
					sec = sector[k];
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
					getzsofslope((short) k, x, y);

					if (ceilzsofslope > zr_ceilz) {
						zr_ceilz = ceilzsofslope;
						zr_ceilhit = k + 16384;
					}
					if (floorzsofslope < zr_florz) {
						zr_florz = floorzsofslope;
						zr_florhit = k + 16384;
					}
				}
			}
			clipsectcnt++;
		} while (clipsectcnt < clipsectnum);

		for (i = 0; i < clipsectnum; i++) {
			for (j = headspritesect[clipsectorlist[i]]; j >= 0; j = nextspritesect[j]) {
				spr = sprite[j];
				cstat = spr.cstat;
				if ((cstat & dasprclipmask) != 0) {
					x1 = spr.x;
					y1 = spr.y;

					clipyou = 0;
					switch (cstat & 48) {
					case 0:
						k = walldist + (spr.clipdist << 2) + 1;
						if ((klabs(x1 - x) <= k) && (klabs(y1 - y) <= k)) {
							ceilzsofslope = spr.z;
							k = ((tilesizy[spr.picnum] * spr.yrepeat) << 1);
							if ((cstat & 128) != 0)
								ceilzsofslope += k;
							if ((picanm[spr.picnum] & 0x00ff0000) != 0)
								ceilzsofslope -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
							floorzsofslope = ceilzsofslope - (k << 1);
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
							ceilzsofslope = spr.z;
							k = ((tilesizy[spr.picnum] * spr.yrepeat) << 1);
							if ((cstat & 128) != 0)
								ceilzsofslope += k;
							if ((picanm[spr.picnum] & 0x00ff0000) != 0)
								ceilzsofslope -= ((int) ((byte) ((picanm[spr.picnum] >> 16) & 255)) * spr.yrepeat << 2);
							floorzsofslope = ceilzsofslope - (k << 1);
							clipyou = 1;
						}
						break;
					case 32:
						ceilzsofslope = spr.z;
						floorzsofslope = ceilzsofslope;

						if ((cstat & 64) != 0)
							if ((z > ceilzsofslope) == ((cstat & 8) == 0))
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
						if ((z > ceilzsofslope) && (ceilzsofslope > zr_ceilz)) {
							zr_ceilz = ceilzsofslope;
							zr_ceilhit = j + 49152;
						}
						if ((z < floorzsofslope) && (floorzsofslope < zr_florz)) {
							zr_florz = floorzsofslope;
							zr_florhit = j + 49152;
						}
					}
				}
			}
		}
	}

	public void setaspect_new() {
		if (r_usenewaspect != 0 && newaspect_enable != 0) {
			// the correction factor 100/107 has been found
			// out experimentally. squares ftw!
			int vr, yx = (65536 * 4 * 100) / (3 * 107);
			int y, x;

			int xd = setaspect_new_use_dimen != 0 ? xdimen : xdim;
			int yd = setaspect_new_use_dimen != 0 ? ydimen : ydim;

			x = xd;
			y = yd;

			vr = (int) divscale(x * 3, y * 4, 16);

			setaspect(vr, yx);
		} else
			setaspect(65536, (int) divscale(ydim * 320, xdim * 200, 16));
	}

	public void setview(int x1, int y1, int x2, int y2) {
		windowx1 = x1;
		wx1 = (x1 << 12);
		windowy1 = y1;
		wy1 = (y1 << 12);
		windowx2 = x2;
		wx2 = ((x2 + 1) << 12);
		windowy2 = y2;
		wy2 = ((y2 + 1) << 12);

		xdimen = (x2 - x1) + 1;
		halfxdimen = (xdimen >> 1);
		ydimen = (y2 - y1) + 1;

		setaspect_new();
	}

	public void setaspect(int daxrange, int daaspect) {
		viewingrange = daxrange;
		viewingrangerecip = (int) divscale(1, daxrange, 32);

		yxaspect = daaspect;
		xyaspect = (int) divscale(1, yxaspect, 32);
		xdimenscale = scale(xdimen, yxaspect, 320);
		xdimscale = scale(320, xyaspect, xdimen);
	}

	//dastat&1    :translucence
	//dastat&2    :auto-scale mode (use 320*200 coordinates)
	//dastat&4    :y-flip
	//dastat&8    :don't clip to startumost/startdmost
	//dastat&16   :force point passed to be top-left corner, 0:Editart center
	//dastat&32   :reverse translucence
	//dastat&64   :non-masked, 0:masked
	//dastat&128  :draw all pages (permanent)
	//dastat&256  :align to the left (widescreen support)
	//dastat&512  :align to the right (widescreen support)
	//dastat&1024 :stretch to screen resolution (distorts aspect ration)

	public void rotatesprite(int sx, int sy, int z, int a, int picnum,
			int dashade, int dapalnum, int dastat,
			int cx1, int cy1, int cx2, int cy2) {
		render.rotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2);
	}

	public void makepalookup(int palnum, byte[] remapbuf, int r, int g, int b, int dastat)
	{
		int i, j, palscale;

		if (paletteloaded == 0) return;

		//Allocate palookup buffer
		if (palookup[palnum] == null)
			palookup[palnum] = new byte[numshades<<8];
		
		if (dastat == 0) return;
		if ((r|g|b|63) != 63) return;

		if ((r|g|b) == 0)
		{
			for(i=0;i<256;i++)
			{
				for (j=0; j<numshades; j++) {
					palookup[palnum][i + j * 256] = palookup[0][remapbuf[i]&0xFF + j * 256];
				}
			}
			palookupfog[palnum][0] = 0;
			palookupfog[palnum][1] = 0;
			palookupfog[palnum][2] = 0;
		}
		else
		{
			for (i=0; i<numshades; i++)
	        {
	            palscale = (int) divscale(i,numshades, 16);
	            for (j=0; j<256; j++)
	            {
	                palookup[palnum][i] = (byte) getclosestcol((int)palette[remapbuf[j]&0xFF*3]+mulscale(r-palette[remapbuf[j]&0xFF*3],palscale, 16),
	                                        (int)palette[remapbuf[j]&0xFF*3+1]+mulscale(g-palette[remapbuf[j]&0xFF*3+1],palscale, 16),
	                                        (int)palette[remapbuf[j]&0xFF*3+2]+mulscale(b-palette[remapbuf[j]&0xFF*3+2],palscale, 16));
	            }
	        }
			palookupfog[palnum][0] = (byte) r;
			palookupfog[palnum][1] = (byte) g;
			palookupfog[palnum][2] = (byte) b;
		}
	}

	// flags:
	//  1: don't setpalette(),  DON'T USE THIS FLAG!
	//  2: don't gltexinvalidateall()
	//  4: don't calc curbrightness from dabrightness,  DON'T USE THIS FLAG!
	//  8: don't gltexinvalidate8()
	// 16: don't reset palfade*

	public void setbrightness(int dabrightness, byte[] dapal, int flags) {
		
		if ((flags&4) == 0)
			curbrightness = min(max(dabrightness,0),15);

		for (int i = 0; i < 256; i++) {
			if (curpalette[i] == null)
				curpalette[i] = new Palette();

			// save palette without any brightness adjustment
			curpalette[i].r = (dapal[i * 3 + 0] & 0xFF) << 2;
			curpalette[i].g = (dapal[i * 3 + 1] & 0xFF) << 2;
			curpalette[i].b = (dapal[i * 3 + 2] & 0xFF) << 2;
			curpalette[i].f = 0;
		}

//		copybufbyte(curpalette, curpalettefaded, curpalette.length);
//		if ((flags&1) == 0)
//			setpalette(0,256,(char*)tempbuf);

		if ((flags & 2) != 0) 
			render.gltexinvalidateall(0);

		palfadergb.r = palfadergb.g = palfadergb.b = 0;
		palfadergb.a = 0;
	}

	public Palette getpal(int col) {
		if (curpalette[col] == null)
			return null;
		if (gammabrightness != 0)
			return curpalette[col];
		else {
			returnpal.b = britable[curbrightness][curpalette[col].b];
			returnpal.g = britable[curbrightness][curpalette[col].g];
			returnpal.r = britable[curbrightness][curpalette[col].r];
			returnpal.f = 0;

			return returnpal;
		}
	}

	public void setpalettefade(int r, int g, int b, int offset) {
		palfadergb.r = min(63, r) << 2;
		palfadergb.g = min(63, g) << 2;
		palfadergb.b = min(63, b) << 2;
		palfadergb.a = (min(63, offset) << 2);
		
		//setpalettefade_calc(offset);
	}

	public void clearview(int dacol) {
		render.clearview(dacol);
	}
	
	public void setviewtotile(int tilenume, int xsiz, int ysiz)
	{
	    //DRAWROOMS TO TILE BACKUP&SET CODE
	    tilesizx[tilenume] = (short)xsiz; tilesizy[tilenume] = (short)ysiz;
	    bakwindowx1[setviewcnt] = windowx1; bakwindowy1[setviewcnt] = windowy1;
	    bakwindowx2[setviewcnt] = windowx2; bakwindowy2[setviewcnt] = windowy2;
	
	    if (setviewcnt == 0)
	        baktile = tilenume;
	   
	    offscreenrendering = true;
	    
	    setviewcnt++;
	    setview(0,0,ysiz-1,xsiz-1);
	    setaspect(65536,65536);
	}
	
	public void setviewback()
	{
	    if (setviewcnt <= 0) return;
	    setviewcnt--;

	    offscreenrendering = (setviewcnt>0);
	    
	    if (setviewcnt == 0) {
	    	waloff[baktile] = getframe(tilesizx[baktile], tilesizy[baktile]);
	        invalidatetile(baktile,-1,-1);
	    }
	    setviewcnt = 0;
	    setview(bakwindowx1[setviewcnt],bakwindowy1[setviewcnt],
	            bakwindowx2[setviewcnt],bakwindowy2[setviewcnt]); 
	}

	public void preparemirror(int dax, int day, int daz, float daang, float dahoriz, int dawall, int dasector) {
		int i, j, x, y, dx, dy;

		x = wall[dawall].x;
		dx = wall[wall[dawall].point2].x - x;
		y = wall[dawall].y;
		dy = wall[wall[dawall].point2].y - y;
		j = dx * dx + dy * dy;
		if (j == 0)
			return;
		i = (((dax - x) * dx + (day - y) * dy) << 1);
		mirrorx = (x << 1) + scale(dx, i, j) - dax;
		mirrory = (y << 1) + scale(dy, i, j) - day;
		mirrorang = BClampAngle((getangle(dx, dy) << 1) - daang);

		inpreparemirror = true;
	}

	public void completemirror() {
		//Software render
	}

	public int sectorofwall(short theline) {
		int i, gap;

		if ((theline < 0) || (theline >= numwalls))
			return (-1);
		i = wall[theline].nextwall;
		if (i >= 0)
			return (wall[i].nextsector);

		gap = (numsectors >> 1);
		i = gap;
		while (gap > 1) {
			gap >>= 1;
			if (sector[i].wallptr < theline)
				i += gap;
			else
				i -= gap;
		}
		while (sector[i].wallptr > theline)
			i--;
		while (sector[i].wallptr + sector[i].wallnum <= theline)
			i++;
		return (i);
	}

	public int getceilzofslope(short sectnum, int dax, int day) {
		if(sectnum == -1 || sector[sectnum] == null) return 0;
		if ((sector[sectnum].ceilingstat & 2) == 0)
			return (sector[sectnum].ceilingz);

		int dx, dy, i, j;
		WALL wal;

		wal = wall[sector[sectnum].wallptr];
		dx = wall[wal.point2].x - wal.x;
		dy = wall[wal.point2].y - wal.y;
		i = (int) (ksqrt(dx * dx + dy * dy) << 5);
		if (i == 0)
			return (sector[sectnum].ceilingz);
		j = (int) dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);
		if(compatibleMode)
			return sector[sectnum].ceilingz + (scale(sector[sectnum].ceilingheinum, j, i));
		return (sector[sectnum].ceilingz + (scale(sector[sectnum].ceilingheinum, j >> 1, i) << 1));
	}

	public int getflorzofslope(short sectnum, int dax, int day) {
		if(sectnum == -1 || sector[sectnum] == null) return 0;
		if ((sector[sectnum].floorstat & 2) == 0)
			return (sector[sectnum].floorz);

		int dx, dy, i, j;
		WALL wal;

		wal = wall[sector[sectnum].wallptr];
		dx = wall[wal.point2].x - wal.x;
		dy = wall[wal.point2].y - wal.y;
		i = (int) (ksqrt(dx * dx + dy * dy) << 5);
		if (i == 0)
			return (sector[sectnum].floorz);
		j = dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);
		if(compatibleMode)
			return sector[sectnum].floorz + (scale(sector[sectnum].floorheinum, j, i));
		return (sector[sectnum].floorz + (scale(sector[sectnum].floorheinum, j >> 1, i) << 1));
	}

	public void getzsofslope(short sectnum, int dax, int day) {
		if(sectnum == -1 || sector[sectnum] == null) return;
		
		int dx, dy, i, j;
		WALL wal, wal2;
		SECTOR sec;

		sec = sector[sectnum];
		if(sec == null) return;
		ceilzsofslope = sec.ceilingz;
		floorzsofslope = sec.floorz;
		if (((sec.ceilingstat | sec.floorstat) & 2) != 0) {
			wal = wall[sec.wallptr];
			wal2 = wall[wal.point2];
			dx = wal2.x - wal.x;
			dy = wal2.y - wal.y;
			i = (int) (ksqrt(dx * dx + dy * dy) << 5);
			if (i == 0)
				return;
			j = (int) dmulscale(dx, day - wal.y, -dy, dax - wal.x, 3);

			if(compatibleMode) {
				if ((sec.ceilingstat & 2) != 0)
					ceilzsofslope += scale(sec.ceilingheinum, j, i);
				if ((sec.floorstat & 2) != 0)
					floorzsofslope += scale(sec.floorheinum, j, i);
			} else {
				if ((sec.ceilingstat & 2) != 0)
					ceilzsofslope += scale(sec.ceilingheinum, j >> 1, i) << 1;
				if ((sec.floorstat & 2) != 0)
					floorzsofslope += scale(sec.floorheinum, j >> 1, i) << 1;
			}
		}
	}

	public void alignceilslope(short dasect, int x, int y, int z) {
		int i, dax, day;
		WALL wal;

		wal = wall[sector[dasect].wallptr];
		dax = wall[wal.point2].x - wal.x;
		day = wall[wal.point2].y - wal.y;

		i = (y - wal.y) * dax - (x - wal.x) * day;
		if (i == 0)
			return;
		sector[dasect].ceilingheinum = (short) scale((z - sector[dasect].ceilingz) << 8, (int) ksqrt(dax * dax + day * day), i);

		if (sector[dasect].ceilingheinum == 0)
			sector[dasect].ceilingstat &= ~2;
		else
			sector[dasect].ceilingstat |= 2;
	}

	public void alignflorslope(short dasect, int x, int y, int z) {
		int i, dax, day;
		WALL wal;

		wal = wall[sector[dasect].wallptr];
		dax = wall[wal.point2].x - wal.x;
		day = wall[wal.point2].y - wal.y;

		i = (y - wal.y) * dax - (x - wal.x) * day;
		if (i == 0)
			return;
		sector[dasect].floorheinum = (short) scale((z - sector[dasect].floorz) << 8,
				(int) ksqrt(dax * dax + day * day), i);

		if (sector[dasect].floorheinum == 0)
			sector[dasect].floorstat &= ~2;
		else
			sector[dasect].floorstat |= 2;
	}

	public int loopnumofsector(short sectnum, short wallnum) {
		int i, numloops, startwall, endwall;

		numloops = 0;
		startwall = sector[sectnum].wallptr;
		endwall = startwall + sector[sectnum].wallnum;
		for (i = startwall; i < endwall; i++) {
			if (i == wallnum)
				return (numloops);
			if (wall[i].point2 < i)
				numloops++;
		}
		return (-1);
	}

	public void setfirstwall(short sectnum, short newfirstwall) {
		int i, j, k, numwallsofloop;
		int startwall, endwall, danumwalls, dagoalloop;

		startwall = sector[sectnum].wallptr;
		danumwalls = sector[sectnum].wallnum;
		endwall = startwall + danumwalls;
		if ((newfirstwall < startwall) || (newfirstwall >= startwall + danumwalls))
			return;
		for (i = 0; i < danumwalls; i++) {
			if (wall[i + numwalls] == null)
				wall[i + numwalls] = new WALL();
			wall[i + numwalls].set(wall[i + startwall]);
		}

		numwallsofloop = 0;
		i = newfirstwall;
		do {
			numwallsofloop++;
			i = wall[i].point2;
		} while (i != newfirstwall);

		//Put correct loop at beginning
		dagoalloop = loopnumofsector(sectnum, newfirstwall);
		if (dagoalloop > 0) {
			j = 0;
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
				wall[wall[i].nextwall].nextwall = (short) i;
	}

	public void printext256(int xpos, int ypos, int col, int backcol, char[] name, int fontsize) {
		render.printext(xpos, ypos, col, backcol, name, fontsize);
	}
	
	public void printchar256(int xpos, int ypos, int col, int backcol, char ch, int fontsize) {
		render.printchar(xpos, ypos, col, backcol, ch, fontsize);
	}

	public String screencapture(String fn) {
		int a, b, c, d;

		fn = fn.substring(0, Bstrrchr(fn, '.') - 4);
		
		DirectoryEntry userdir = cache.checkDirectory("<userdir>");

		int capturecount = 0;
		do { // JBF 2004022: So we don't overwrite existing screenshots
			if (capturecount > 9999)
				return null;

			a = ((capturecount / 1000) % 10);
			b = ((capturecount / 100) % 10);
			c = ((capturecount / 10) % 10);
			d = (capturecount % 10);

			if(userdir.checkFile(fn + a + b + c + d + ".png") == null)
				break;
			capturecount++;
		} while (true);
		
		int w = xdim, h = ydim;
		ByteBuffer frame = render.getframebuffer(0, 0, w, h, GL10.GL_RGB);
		Pixmap capture = new Pixmap(w, h, Format.RGB888);
		ByteBuffer pixels = capture.getPixels();
		
		final int numBytes = w * h * 3;
		byte[] lines = new byte[numBytes];
		final int numBytesPerLine = w * 3;
		for (int i = 0; i < h; i++) {
			frame.position((h - i - 1) * numBytesPerLine);
			frame.get(lines, i * numBytesPerLine, numBytesPerLine);
		}
		pixels.put(lines);
		
		File pci = new File(userdir.getAbsolutePath() + fn + a + b + c + d + ".png");
		try {
			PixmapIO.writePNG(new FileHandle(pci), capture);
			userdir.addFile(pci);
			capture.dispose();
			return fn + a + b + c + d + ".png";
		} 
		catch(Exception e) {
			return null;
		}
	}
	
	private byte[] capture;
	public byte[] screencapture(int width, int heigth) {
		if (capture == null || capture.length < width * heigth ) 
			capture = new byte[width * heigth];
		ByteBuffer frame = render.getframebuffer(0, 0, xdim, ydim, GL10.GL_RGB);

		long xf = divscale(xdim, width, 16);
		long yf = divscale(ydim, heigth, 16);

		int base, r, g, b;
		for (int x, y = 0; y < heigth; y++) {
			base = mulscale(heigth - y - 1, yf, 16) * xdim;
			for (x = 0; x < width; x++) {
				frame.position(3 * (base + mulscale(x, xf, 16)));
				r = frame.get() & 0xFF;
				g = frame.get() & 0xFF;
				b = frame.get() & 0xFF;
				capture[heigth * x + y] = getclosestcol(r, g, b);
			}
		}
		return capture;
	}
	
	public byte[] getframe(int width, int heigth) {
		if (capture == null || capture.length < width * heigth ) 
			capture = new byte[width * heigth];
		ByteBuffer frame = render.getframebuffer(0, ydim - heigth, width, heigth, GL10.GL_RGB);
		int r, g, b;
		for (int x, y = heigth - 1; y >= 0; y--) {
			for (x = 0; x < width; x++) {
				r = frame.get() & 0xFF;
				g = frame.get() & 0xFF;
				b = frame.get() & 0xFF;
				capture[heigth * x + y] = getclosestcol(r, g, b);
			}
		}
		
		return capture;
	}

	public void savetexture(byte[] pixels, int tw, int th, int w, int h, int num) {
		Pixmap pixmap = new Pixmap(w, h, Format.RGB888);

		for (int i = 0; i < (tw * th); i++) {
			int row = (int) Math.floor(i / tw);
			int col = i % tw;
			if (col < w && row < h) {
				pixmap.setColor((pixels[4 * i + 0] & 0xFF) / 255.f, (pixels[4 * i + 1] & 0xFF) / 255.f, (pixels[4 * i + 2] & 0xFF) / 255.f, 1);
				pixmap.drawPixel(col, row);
			}
		}

		PixmapIO.writePNG(new FileHandle("texture" + num + ".png"), pixmap);

		System.out.println("texture" + num + ".png saved!");
		pixmap.dispose();
	}

	public int setrendermode(Renderer render) {
		this.render = render;

		return 0;
	}
	
	public Renderer getrender()
	{
		return render;
	}

	public void invalidatetile(int tilenume, int pal, int how) {

		if(render == null) //not initialized...
			return;
		
		int numpal, firstpal, np;
		int hp;

		if (pal < 0) {
			numpal = MAXPALOOKUPS;
			firstpal = 0;
		} else {
			numpal = 1;
			firstpal = pal % MAXPALOOKUPS;
		}

		for (hp = 0; hp < 8; hp += 4) {
			if ((how & pow2long[hp]) == 0)
				continue;

			for (np = firstpal; np < firstpal + numpal; np++) {
				render.gltexinvalidate(tilenume, np, hp);
			}
		}
	}

	public void deletetile(int tilenume) {

	}

	public void copytilepiece(int tilenume1, int sx1, int sy1, int xsiz, int ysiz,
			int tilenume2, int sx2, int sy2) {
		byte ptr1;
		long xsiz1, ysiz1, xsiz2, ysiz2, i, j, x1, y1, x2, y2;

		xsiz1 = tilesizx[tilenume1];
		ysiz1 = tilesizy[tilenume1];
		xsiz2 = tilesizx[tilenume2];
		ysiz2 = tilesizy[tilenume2];
		if ((xsiz1 > 0) && (ysiz1 > 0) && (xsiz2 > 0) && (ysiz2 > 0)) {
			if (waloff[tilenume1] == null)
				loadtile(tilenume1);
			if (waloff[tilenume2] == null)
				loadtile(tilenume2);

			x1 = sx1;
			for (i = 0; i < xsiz; i++) {
				y1 = sy1;
				for (j = 0; j < ysiz; j++) {
					x2 = sx2 + i;
					y2 = sy2 + j;
					if ((x2 >= 0) && (y2 >= 0) && (x2 < xsiz2) && (y2 < ysiz2)) {
						ptr1 = waloff[tilenume1][(int) (x1 * ysiz1 + y1)];
						if (ptr1 != 255)
							waloff[tilenume2][(int) (x2 * ysiz2 + y2)] = ptr1;
					}

					y1++;
					if (y1 >= ysiz1)
						y1 = 0;
				}
				x1++;
				if (x1 >= xsiz1)
					x1 = 0;
			}
		}
	}

	public abstract void faketimerhandler();

	public void setgotpic(int tilenume) {
		gotpic[tilenume >> 3] |= pow2char[tilenume & 7];
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
			if (wall[wall[i].point2].x < minx) {
				minx = wall[wall[i].point2].x;
				themin = i;
			}
		} while ((wall[i].point2 != wallstart) && (i < MAXWALLS));

		x0 = wall[themin].x;
		y0 = wall[themin].y;
		x1 = wall[wall[themin].point2].x;
		y1 = wall[wall[themin].point2].y;
		x2 = wall[wall[wall[themin].point2].point2].x;
		y2 = wall[wall[wall[themin].point2].point2].y;

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
			x1 = wall[i].x;
			x2 = wall[wall[i].point2].x;
			if ((x1 >= x) || (x2 >= x)) {
				y1 = wall[i].y;
				y2 = wall[wall[i].point2].y;
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
			i = wall[i].point2;
		} while (i != startwall);
		return (cnt);
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
	
	public boolean showMessage(String header, String text, boolean send)
	{
		if(message == null) return false;
		if(Gdx.graphics != null)
			Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		return message.show(header, text, send);
	}
	
	public static KeyInput getInput()
	{
		return input;
	}

	public void handleevents() { 
		if(Gdx.input == null) //not initialized
			return;

		input.handleevents();
		Console.HandleScanCode();
	}

	public void initkeys() {
		input = new KeyInput();
	}
	
	private int FrameCount = 0;
	private int LastCount = 0;
	private int LastSec = 0;
	private long LastMS = 0; //getticks();
    
    private final char[] fpsbuffer = new char[15];

    public void printfps() {
		// adapted from ZDoom because I like it better than what we had
		// applicable ZDoom code available under GPL from csDoom

    	long ms = getticks();
    	long howlong = ms - LastMS;
    	
    	if(howlong > 9999) howlong = 9999;
		if (howlong >= 0 && howlong <= 9999) {
			int thisSec = (int)(ms / 1000);
			
			int chars = Bitoa((int)howlong, fpsbuffer);
			chars = buildString(fpsbuffer, chars, "ms ", LastCount);
			chars = buildString(fpsbuffer, chars, "fps");

			printext256(windowx2 - (chars << (3)), windowy1 + 1, 31, -1, fpsbuffer, 0);

			if ((thisSec - LastSec) != 0) {
				LastCount = FrameCount / (thisSec - LastSec);
				LastSec = thisSec;
				FrameCount = 0;
			}
			FrameCount++;
		}
		LastMS = ms;
    }

    public BAudio getAudio()
    {
    	return audio;
    }
}
