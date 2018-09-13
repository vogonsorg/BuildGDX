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
import ru.m210projects.Build.Types.SPRITE;
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
	
	protected class Line {
		public int x1, y1, x2, y2;
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
	
	private boolean releasedEngine;
	public boolean compatibleMode;
	public static boolean UseBloodPal = false;
	
	public Renderer render;
	private Message message;
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
//	public static byte[] show2dsector;
//	public static byte[] show2dwall;
//	public static byte[] show2dsprite;

//	public static SECTOR[] sector;
//	public static WALL[] wall;
//	public static SPRITE[] sprite;
	public static SPRITE[] tsprite;

//	public static short[] headspritesect, headspritestat;
//	public static short[] prevspritesect, prevspritestat;
//	public static short[] nextspritesect, nextspritestat;
	
	public static byte[] gotpic;
	public static byte[] gotsector;
	public static int spritesortcnt;
	public static int windowx1, windowy1, windowx2, windowy2;
	public static int xdim, ydim;
	public static int yxaspect, viewingrange;
	
	//OUTPUT VALUES
//	public static int floorzsofslope, ceilzsofslope;
//	public static int mirrorx, mirrory;
//	public static float mirrorang;
	
//	public static Point intersect;
//	public static int rayx = 0;
//	public static int rayy = 0;
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

	private final char[] fpsbuffer = new char[15];
	public static byte[] curpalette;
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
	public static int globalvisibility;
	public static int globalshade, globalpal, cosglobalang, singlobalang;
	public static int cosviewingrangeglobalang, sinviewingrangeglobalang;
	public static int beforedrawrooms = 1;
	public static int xyaspect, viewingrangerecip;
	public static boolean inpreparemirror = false;
	public static char[] textfont;
	public static char[] smalltextfont;
	
	//high resources
	public static boolean usehightile = true;
	public static boolean usevoxels = true;
	public static boolean usemodels = true;
	
//	private byte[] sectbitmap;
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
//	private int hitallsprites = 0;
//	private final int MAXCLIPNUM = 1024;
//	protected final int MAXCLIPDIST = 1024;
//
//	protected short clipnum;

//	protected int[] rxi;
//	protected int[] ryi;
//	protected int[] hitwalls;
//
//	protected Line[] clipit;
//	protected short[] clipsectorlist;
//	protected short clipsectnum;
//	protected int[] clipobjectval;

	private int[] rdist, gdist, bdist;
	private final int FASTPALGRIDSIZ = 8;
	
	private byte[] colhere;
	private byte[] colhead;
	private byte[] colnext;
	private final byte[] coldist = { 0, 1, 2, 3, 4, 3, 2, 1 };
	private int[] colscan;
	private int randomseed = 1;

	public static short[] radarang;
	public static byte[] transluc;

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
		
	protected final World world;
		
	//Engine.c
	
	public World getWorld()
	{
		return world;
	}
	
	public abstract World InitWorld();

	public int getpalookup(int davis, int dashade) //jfBuild
	{
	    return(min(max(dashade+(davis>>8),0),numshades-1));
	}

	public int animateoffs(int tilenum, int nInfo) { //jfBuild + gdxBuild
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

	public void initksqrt() { //jfBuild
		int i, j = 1, k = 0;
		for (i = 0; i < 4096; i++) {
			if (i >= j) { j <<= 2; k++; }

			sqrtable[i] = (short)((int)sqrt(((i << 18) + 131072)) << 1);
			shlookup[i] = (short) ((k << 1) + ((10 - k) << 8));
			if (i < 256) shlookup[i + 4096] = (short) (((k + 6) << 1) + ((10 - (k + 6)) << 8));
		}
	}

	public void calcbritable() { //jfBuild
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

	public void loadtables() throws Exception { //jfBuild + gdxBuild
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

	public void initfastcolorlookup(int rscale, int gscale, int bscale) { //jfBuild
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

	public void loadpalette() throws Exception //jfBuild + gdxBuild
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

	public byte getclosestcol(int r, int g, int b) { //jfBuild
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

	//
	// Exported Engine Functions
	//
	
	public void InitArrays() //gdxBuild
	{
		palookupfog = new byte[MAXPALOOKUPS][3];
		pskyoff = new short[MAXPSKYTILES];
		zeropskyoff = new short[MAXPSKYTILES];
		spriteext = new Spriteext[MAXSPRITES + MAXUNIQHUDID];
		spritesmooth = new Spritesmooth[MAXSPRITES+MAXUNIQHUDID];
		tilesizx = new short[MAXTILES]; 
		tilesizy = new short[MAXTILES];
		picanm = new int[MAXTILES];
		tsprite = new SPRITE[MAXSPRITESONSCREEN + 1];

		gotpic = new byte[(MAXTILES + 7) >> 3];
		gotsector = new byte[(MAXSECTORS + 7) >> 3];

		pHitInfo = new Hitscan();
		neartag = new Neartag();
		britable = new byte[16][256];
		picsiz = new int[MAXTILES];
		tilefilenum = new int[MAXTILES];
		tilefileoffs = new int[MAXTILES];
		sqrtable = new short[4096];
		shlookup = new short[4096 + 256];
		curpalette = new byte[768];
		palfadergb = new FadeEffect(GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) {
			@Override
			public void update(int intensive) {}
		};
		tile2model = new Tile2model[MAXTILES + EXTRATILES];
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
		
		Arrays.fill(tiletovox, -1);
//		Arrays.fill(voxscale, 65536);
		
		bakwindowx1 = new int[4]; 
		bakwindowy1 = new int[4];
		bakwindowx2 = new int[4]; 
		bakwindowy2 = new int[4];
	}

	public Engine(Message message, BAudio audio, boolean releasedEngine) throws Exception { //gdxBuild
		this.world = InitWorld();
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

	public void uninit() //gdxBuild
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

	public int drawrooms(float daposx, float daposy, float daposz,
			float daang, float dahoriz, int dacursectnum) { //eDuke32 visibility set

		beforedrawrooms = 0;

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

	public void drawmasks() { //gdxBuild
		render.drawmasks();
	}

	public void drawmapview(int dax, int day, int zoome, int ang) { //gdxBuild
		render.drawmapview(dax, day, zoome, ang);
	}

	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) { //gdxBuild
		render.drawoverheadmap(cposx, cposy, czoom, cang);
	}

	// JBF: davidoption now functions as a windowed-mode flag (0 == windowed, 1 == fullscreen)
	public boolean setgamemode(int davidoption, int daxdim, int daydim) { //jfBuild + gdxBuild
		daxdim = max(320, daxdim);
		daydim = max(200, daydim);

		if ((davidoption == fullscreen) && (xdim == daxdim) && (ydim == daydim))
			return true;

		xdim = daxdim;
		ydim = daydim;

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
				if(mode.width == daxdim && mode.height == daydim)
					if(m == null || m.refreshRate < Gdx.graphics.getDisplayMode().refreshRate) {
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

	public void inittimer(int tickspersecond) { //jfBuild
		if (timerfreq != 0)
			return; // already installed

		timerfreq = 1000;
		timerticspersec = tickspersecond;
		timerlastsample = getticks() * timerticspersec / timerfreq;
	}

	public void sampletimer() { //jfBuild
		if (timerfreq == 0)
			return;

		long n = (getticks() * timerticspersec / timerfreq) - timerlastsample;  
		if (n > 0) {
			totalclock += n;
			timerlastsample += n;
		}
	}

	public long getticks() { //gdxBuild
		return System.currentTimeMillis();
	}
	
	HashMap<String, FadeEffect> fades;
	public void registerFade(String fadename, FadeEffect effect) { //gdxBuild
		if(fades == null) fades = new HashMap<String, FadeEffect>();
		fades.put(fadename, effect);
	}
	
	public void updateFade(String fadename, int intensive) //gdxBuild
	{
		FadeEffect effect = fades.get(fadename);
		if(effect != null)
			effect.update(intensive);
	}

	public void showfade() { //gdxBuild
		render.palfade(fades);
	}
	
	public void loadpic(String filename) //gdxBuild
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
	
	public void setpicsiz(int tilenum) //jfBuild
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

	public int loadpics(String filename) { //jfBuild
		int offscount, localtilestart, localtileend, dasiz;
		int fil, i, k;

		buildString(artfilename, 0, filename);

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

	public byte[] loadtile(int tilenume) { //jfBuild
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

	public byte[] allocatepermanenttile(int tilenume, int xsiz, int ysiz) { //jfBuild
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

	public short getangle(int xvect, int yvect) { //jfBuild + gdxBuild
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

	public int ksqrt(int a) { //jfBuild + gdxBuild
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

	public void nextpage() { //gdxBuild
		Console.draw();
		render.nextpage();
		audio.update();
	}

	public long qdist(long dx, long dy) { //gdxBuild
		dx = abs(dx);
		dy = abs(dy);

		if (dx > dy)
			dy = (3 * dy) >> 3;
		else
			dx = (3 * dx) >> 3;

		return dx + dy;
	}

	public static Vector2 rotatepoint = new Vector2();
	public void rotatepoint(int xpivot, int ypivot, int x, int y, short daang) { //jfBuild
		int dacos, dasin;

		dacos = sintable[(daang + 2560) & 2047];
		dasin = sintable[(daang + 2048) & 2047];
		x -= xpivot;
		y -= ypivot;
		rotatepoint.x = dmulscale(x, dacos, -y, dasin, 14) + xpivot;
		rotatepoint.y = dmulscale(y, dacos, x, dasin, 14) + ypivot;
	}

	public void srand(int seed) //gdxBuild
	{
		randomseed = seed;
	}
	
	public int getrand() //gdxBuild
	{
		return randomseed;
	}

	public int krand() { //jfBuild
		randomseed = (randomseed * 27584621) + 1;
		return (int) ((randomseed&0xFFFFFFFFL) >> 16);
	}
	
	public int rand() //gdxBuild
	{
		return (int) (Math.random() * 32767);
	}

	public void setaspect_new() { //eduke32 aspect
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

	public void setview(int x1, int y1, int x2, int y2) { //jfBuild
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

	public void setaspect(int daxrange, int daaspect) { //jfBuild
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

	public void rotatesprite(int sx, int sy, int z, int a, int picnum, //gdxBuild
			int dashade, int dapalnum, int dastat,
			int cx1, int cy1, int cx2, int cy2) {
		render.rotatesprite(sx, sy, z, a, picnum, dashade, dapalnum, dastat, cx1, cy1, cx2, cy2);
	}

	public void makepalookup(int palnum, byte[] remapbuf, int r, int g, int b, int dastat)  //jfBuild
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
	                palookup[palnum][i] = (byte) getclosestcol((palette[remapbuf[j]&0xFF*3]&0xFF)+mulscale(r-palette[remapbuf[j]&0xFF*3]&0xFF,palscale, 16),
	                                        (palette[remapbuf[j]&0xFF*3+1]&0xFF)+mulscale(g-palette[remapbuf[j]&0xFF*3+1]&0xFF,palscale, 16),
	                                        (palette[remapbuf[j]&0xFF*3+2]&0xFF)+mulscale(b-palette[remapbuf[j]&0xFF*3+2]&0xFF,palscale, 16));
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

	public void setbrightness(int dabrightness, byte[] dapal, int flags) { //jfBuild
		
		if ((flags&4) == 0)
			curbrightness = min(max(dabrightness,0),15);

		for (int i = 0; i < 768; i++) 
			curpalette[i] = (byte) ((dapal[i]& 0xFF) << 2);
		
//		copybufbyte(curpalette, curpalettefaded, curpalette.length);
//		if ((flags&1) == 0)
//			setpalette(0,256,(char*)tempbuf);

		if ((flags & 2) != 0) 
			render.gltexinvalidateall(0);

		palfadergb.r = palfadergb.g = palfadergb.b = 0;
		palfadergb.a = 0;
	}

	public void setpalettefade(int r, int g, int b, int offset) { //jfBuild
		palfadergb.r = min(63, r) << 2;
		palfadergb.g = min(63, g) << 2;
		palfadergb.b = min(63, b) << 2;
		palfadergb.a = (min(63, offset) << 2);
	}

	public void clearview(int dacol) { //gdxBuild
		render.clearview(dacol);
	}
	
	public void setviewtotile(int tilenume, int xsiz, int ysiz) //jfBuild
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
	
	public void setviewback() //jfBuild
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

	public void printext256(int xpos, int ypos, int col, int backcol, char[] name, int fontsize) { //gdxBuild
		render.printext(xpos, ypos, col, backcol, name, fontsize);
	}
	
	public void printchar256(int xpos, int ypos, int col, int backcol, char ch, int fontsize) { //gdxBuild
		render.printchar(xpos, ypos, col, backcol, ch, fontsize);
	}

	public String screencapture(String fn) { //jfBuild + gdxBuild
		int a, b, c, d;
		
		if(render.getname().equals("Classic"))
			return null;

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
	public byte[] screencapture(int width, int heigth) { //gdxBuild
		if (capture == null || capture.length < width * heigth ) 
			capture = new byte[width * heigth];

		if(render.getname().equals("Classic"))
			return null;
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
	
	public byte[] getframe(int width, int heigth) { //gdxBuild
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

	public void savetexture(byte[] pixels, int tw, int th, int w, int h, int num) { //gdxBuild
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

	public int setrendermode(Renderer render) { //gdxBuild
		this.render = render;

		return 0;
	}
	
	public Renderer getrender() //gdxBuild
	{
		return render;
	}

	//
	// invalidatetile
	//  pal: pass -1 to invalidate all palettes for the tile, or >=0 for a particular palette
	//  how: pass -1 to invalidate all instances of the tile in texture memory, or a bitfield
	//	         bit 0: opaque or masked (non-translucent) texture, using repeating
	//	         bit 1: ignored
	//	         bit 2: ignored (33% translucence, using repeating)
	//	         bit 3: ignored (67% translucence, using repeating)
	//	         bit 4: opaque or masked (non-translucent) texture, using clamping
	//	         bit 5: ignored
	//	         bit 6: ignored (33% translucence, using clamping)
	//	         bit 7: ignored (67% translucence, using clamping)
	//	       clamping is for sprites, repeating is for walls
	//
	
	public void invalidatetile(int tilenume, int pal, int how) { //jfBuild

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

	public void copytilepiece(int tilenume1, int sx1, int sy1, int xsiz, int ysiz, //jfBuild
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

	public void setgotpic(int tilenume) { //jfBuild
		gotpic[tilenume >> 3] |= pow2char[tilenume & 7];
	}

	public boolean showMessage(String header, String text, boolean send) //gdxBuild
	{
		if(message == null) return false;
		if(Gdx.graphics != null)
			Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		return message.show(header, text, send);
	}
	 
	public static KeyInput getInput() //gdxBuild
	{
		return input;
	}

	public void handleevents() { //gdxBuild
		if(Gdx.input == null) //not initialized
			return;

		input.handleevents();
		Console.HandleScanCode();
		
		sampletimer();
	}

	public void initkeys() { //gdxBuild
		input = new KeyInput();
	}
	
    public void printfps() { 
    	int fps = Gdx.graphics.getFramesPerSecond();
    	int rate = (int)(Gdx.graphics.getDeltaTime() * 1000);
    	if(fps <= 9999 && rate <= 9999) {
	    	int chars = Bitoa(rate, fpsbuffer);
			chars = buildString(fpsbuffer, chars, "ms ", fps);
			chars = buildString(fpsbuffer, chars, "fps");
			
			printext256(windowx2 - (chars << (3)), windowy1 + 1, 31, -1, fpsbuffer, 0);
    	}
    }

    public BAudio getAudio() //gdxBuild
    {
    	return audio;
    }
}
