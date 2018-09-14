/*
 * Software renderer code originally written by Ken Silverman
 * Ken Silverman's official web site: "http://www.advsys.net/ken"
 * See the included license file "BUILDLIC.TXT" for license info.
 *
 * This file has been modified from Ken Silverman's original release
 * by Jonathon Fowler (jf@jonof.id.au)
 * by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Render.Software;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Loader.Model;
import ru.m210projects.Build.Render.Renderer;
import ru.m210projects.Build.Render.Types.FadeEffect;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;

public class Software implements Renderer {

	public final int BITSOFPRECISION = 3;
	
	private Ac a;
	protected Engine engine;
	private JDisplay display;
	
	private int xres, yres;

	private int numpages; //XXX
	public int bytesperline, frameoffset;
	
	private int guniqhudid;
	
	private final int MAXPERMS = 512;
	private PermFifo permfifo[] = new PermFifo[MAXPERMS];
	private int permhead = 0, permtail = 0;
	
	public int[] nrx1 = new int[8], nry1 = new int[8], 
			nrx2 = new int[8], nry2 = new int[8];	// JBF 20031206: Thanks Ken
	
	public short[] umost  = new short[MAXXDIM], dmost = new short[MAXXDIM];
	public short[] uplc  = new short[MAXXDIM], dplc = new short[MAXXDIM];
	
	public short[] startumost = new short[MAXXDIM], startdmost = new short[MAXXDIM];
	public int[] ylookup = new int[MAXYDIM+1];
	
	public int[] lookups;

	public byte[] frameplace;
	
	public int globaluclip, globaldclip;
	public int globalpisibility, globalhisibility, globalcisibility;
	public int globparaceilclip, globparaflorclip;
	
	protected short globalpicnum, globalshiftval;
	public int globalorientation, globvis, globalyscale;
	public int globalxpanning, globalypanning;
	public int globalx1, globaly1, globalx2, globaly2;
	public int globalx;
	public int globaly;
	public int globalx3, globaly3;
	public int globalzd, globalzx, globalz;
	protected byte globalxshift;
	protected byte globalyshift;
	
	public byte[] globalbufplc;
	public int globalpalwritten;

	public int numscans, numhits, numbunches;
	
	private final int MAXWALLSB = ((MAXWALLS >> 2) + (MAXWALLS >> 3));
	private final int MAXYSAVES = ((MAXXDIM*MAXSPRITES)>>7);
	public int[] xb1 = new int[MAXWALLSB], yb1 = new int[MAXWALLSB], xb2 = new int[MAXWALLSB], yb2 = new int[MAXWALLSB];
	public int[] rx1 = new int[MAXWALLSB], ry1 = new int[MAXWALLSB], rx2 = new int[MAXWALLSB], ry2 = new int[MAXWALLSB];
	
	public int[] rxi = new int[8], ryi = new int[8], rzi = new int[8], rxi2 = new int[8], ryi2 = new int[8], rzi2 = new int[8];
	public int[] xsi = new int[8], ysi = new int[8];
	public int horizlookup, horizlookup2;
	public int horizycent;

	
	
	public short[] p2 = new short[MAXWALLSB], thesector = new short[MAXWALLSB], thewall = new short[MAXWALLSB];
	public short[] bunchfirst = new short[MAXWALLSB], bunchlast = new short[MAXWALLSB];
	public short[] radarang2= new short[MAXXDIM];
	
	public short[] uwall = new short[MAXXDIM], dwall = new short[MAXXDIM];
	public int[] swall = new int[MAXXDIM], lwall = new int[MAXXDIM+4];
	public int[] swplc = new int[MAXXDIM], lplc = new int[MAXXDIM];
	public int[] lastx = new int[MAXYDIM];

	public int[] slopalookup = new int[16384];	// was 2048
	
	public int[] reciptable = new int[2048];
	public int xdimenrecip;
	
	public int smostwall[] = new int[MAXWALLSB], smostwallcnt = -1;
	public short smost[] = new short[MAXYSAVES], smostcnt;
	public int smoststart[] = new int[MAXWALLSB];
	public byte[] smostwalltype = new byte[MAXWALLSB];
	
	public int maskwall[] = new int[MAXWALLSB], maskwallcnt;
	private int spritesx[] = new int[MAXSPRITESONSCREEN];
	private int spritesy[] = new int[MAXSPRITESONSCREEN+1];
	private int spritesz[] = new int[MAXSPRITESONSCREEN];
	private SPRITE[] tspriteptr = new SPRITE[MAXSPRITESONSCREEN + 1];
	
	public short[] sectorborder = new short[256];
	public short sectorbordercnt;
	
	public int mirrorsx1, mirrorsy1, mirrorsx2, mirrorsy2;
	public byte[] tempbuf = new byte[MAXWALLS];
	
	private int oxyaspect, oxdimen, oviewingrange;

	private final int MAXVOXMIPS = 5;
	Model[][] voxoff = new Model[MAXVOXELS][MAXVOXMIPS];
	private int[] zofslope = new int[2];
	
//	private final int MAXXSIZ = 256;	
//	private int[] ggxinc = new int[MAXXSIZ+1], ggyinc = new int[MAXXSIZ+1];
//	private int lowrecip[] = new int[1024], nytooclose, nytoofar;
	private int[] distrecip = new int[65536];

	public Software(Engine engine)
	{
		this.engine = engine;
		a = new Ac(this);
	}
	
	@Override
	public void init() {
		this.xres = xdim;
		this.yres = ydim;
		display = new JDisplay(xres, yres, "Software");
		frameplace = new byte[xres * yres];
		bytesperline = xres;
		
		int j = ydim*4*4;
		
		lookups = new int[j<<1];
		
		horizlookup = 0;
		horizlookup2 = j;
		horizycent = ((ydim*4)>>1);
		
		//Force drawrooms to call dosetaspect & recalculate stuff
		oxyaspect = oxdimen = oviewingrange = -1;
		xdimenrecip = (int) divscale(1,xdimen,32);

		a.setvlinebpl(bytesperline);
		
		a.fixtransluscence(transluc);
		globalpalwritten = 0;
		a.setpalookupaddress(globalpalwritten);
		
		j = 0;
		for(int i=0;i<=ydim;i++) { ylookup[i] = j; j += bytesperline; }
		
		for(int i=0;i<2048;i++) reciptable[i] = (int) divscale(2048,i+2048, 30);
		
		for(int i=0;i<windowx1;i++) { startumost[i] = 1; startdmost[i] = 0; }
		for(int i=windowx1;i<=windowx2;i++)
			{ startumost[i] = (short) windowy1; startdmost[i] = (short) (windowy2+1); }
		for(int i=windowx2+1;i<xdim;i++) { startumost[i] = 1; startdmost[i] = 0; }
	}

	@Override
	public void uninit() {
		
	}

	@Override
	public void drawmasks() {
		int i, j, k, l, gap, xs, ys, xp, yp, yoff, yspan;

		for(i=spritesortcnt-1;i>=0;i--) tspriteptr[i] = tsprite[i];
		for(i=spritesortcnt-1;i>=0;i--)
		{
			xs = tspriteptr[i].x-globalposx; ys = tspriteptr[i].y-globalposy;
			yp = dmulscale(xs,cosviewingrangeglobalang,ys,sinviewingrangeglobalang,6);
			if (yp > (4<<8))
			{
				xp = dmulscale(ys,cosglobalang,-xs,singlobalang,6);
				spritesx[i] = scale(xp+yp,xdimen<<7,yp);
			}
			else if ((tspriteptr[i].cstat&48) == 0)
			{
				spritesortcnt--;  //Delete face sprite if on wrong side!
				if (i != spritesortcnt)
				{
					tspriteptr[i] = tspriteptr[spritesortcnt];
					spritesx[i] = spritesx[spritesortcnt];
					spritesy[i] = spritesy[spritesortcnt];
				}
				continue;
			}
			spritesy[i] = yp;
		}

		gap = 1; while (gap < spritesortcnt) gap = (gap<<1)+1;
		for(gap>>=1;gap>0;gap>>=1)      //Sort sprite list
			for(i=0;i<spritesortcnt-gap;i++)
				for(l=i;l>=0;l-=gap)
				{
					if (spritesy[l] <= spritesy[l+gap]) break;
					SPRITE stmp = tspriteptr[l];
					tspriteptr[l] = tspriteptr[l + gap];
					tspriteptr[l + gap] = stmp;

					int tmp = spritesx[l];
					spritesx[l] = spritesx[l + gap];
					spritesx[l + gap] = tmp;

					tmp = spritesy[l];
					spritesy[l] = spritesy[l + gap];
					spritesy[l + gap] = tmp;
				}

		if (spritesortcnt > 0)
			spritesy[spritesortcnt] = (spritesy[spritesortcnt-1]^1);

		ys = spritesy[0]; i = 0;
		for(j=1;j<=spritesortcnt;j++)
		{
			if (spritesy[j] == ys) continue;
			ys = spritesy[j];
			if (j > i+1)
			{
				for(k=i;k<j;k++)
				{
					spritesz[k] = tspriteptr[k].z;
					if ((tspriteptr[k].cstat&48) != 32)
					{
						yoff = (int)((byte)((picanm[tspriteptr[k].picnum]>>16)&255))+(tspriteptr[k].yoffset);
						spritesz[k] -= ((yoff*tspriteptr[k].yrepeat)<<2);
						yspan = (tilesizy[tspriteptr[k].picnum]*tspriteptr[k].yrepeat<<2);
						if ((tspriteptr[k].cstat&128) == 0) spritesz[k] -= (yspan>>1);
						if (klabs(spritesz[k]-globalposz) < (yspan>>1)) spritesz[k] = globalposz;
					}
				}
				for(k=i+1;k<j;k++)
					for(l=i;l<k;l++)
						if (klabs(spritesz[k]-globalposz) < klabs(spritesz[l]-globalposz))
						{
							SPRITE stmp = tspriteptr[k];
							tspriteptr[k] = tspriteptr[l];
							tspriteptr[l] = stmp;

							int tmp = spritesx[k];
							spritesx[k] = spritesx[l];
							spritesx[l] = tmp;

							tmp = spritesy[k];
							spritesy[k] = spritesy[l];
							spritesy[l] = tmp;

							tmp = spritesz[k];
							spritesz[k] = spritesz[l];
							spritesz[l] = tmp;
						}
				for(k=i+1;k<j;k++)
					for(l=i;l<k;l++)
						if (tspriteptr[k].statnum < tspriteptr[l].statnum)
						{
							SPRITE stmp = tspriteptr[k];
							tspriteptr[k] = tspriteptr[l];
							tspriteptr[l] = stmp;
							int tmp = spritesx[k];
							spritesx[k] = spritesx[l];
							spritesx[l] = tmp;

							tmp = spritesy[k];
							spritesy[k] = spritesy[l];
							spritesy[l] = tmp;
						}
			}
			i = j;
		}

		while ((spritesortcnt > 0) && (maskwallcnt > 0))  //While BOTH > 0
		{
			j = maskwall[maskwallcnt-1];
			if (!spritewallfront(tspriteptr[spritesortcnt-1],thewall[j]))
				drawsprite(--spritesortcnt);
			else
			{
					//Check to see if any sprites behind the masked wall...
				k = -1;
				gap = 0;
				for(i=spritesortcnt-2;i>=0;i--)
					if ((xb1[j] <= (spritesx[i]>>8)) && ((spritesx[i]>>8) <= xb2[j]))
						if (!spritewallfront(tspriteptr[i],thewall[j]))
						{
							drawsprite(i);
							tspriteptr[i].owner = -1;
							k = i;
							gap++;
						}
				if (k >= 0)       //remove holes in sprite list
				{
					for(i=k;i<spritesortcnt;i++)
						if (tspriteptr[i].owner >= 0)
						{
							if (i > k)
							{
								tspriteptr[k] = tspriteptr[i];
								spritesx[k] = spritesx[i];
								spritesy[k] = spritesy[i];
							}
							k++;
						}
					spritesortcnt -= gap;
				}

				//finally safe to draw the masked wall
				drawmaskwall(--maskwallcnt);
			}
		}
		while (spritesortcnt > 0) drawsprite(--spritesortcnt);
		while (maskwallcnt > 0) drawmaskwall(--maskwallcnt);
	}

	@Override
	public void drawrooms() {
		globaluclip = (0-(int)globalhoriz)*xdimscale;
		globaldclip = (ydimen-(int)globalhoriz)*xdimscale;

		int i = mulscale(xdimenscale,viewingrangerecip, 16);
		globalpisibility = mulscale(parallaxvisibility,i,16);
		
		globalhisibility = mulscale(globalvisibility,xyaspect,16);
		globalcisibility = mulscale(globalhisibility,320,8);

		if ((xyaspect != oxyaspect) || (xdimen != oxdimen) || (viewingrange != oviewingrange))
			dosetaspect();

		i = xdimen-1;
		do
		{
			umost[i] = (short) (startumost[windowx1 + i]-windowy1);
			dmost[i] = (short) (startdmost[windowx1 + i]-windowy1);
			i--;
		} while (i != 0);
		umost[0] = (short) (startumost[windowx1]-windowy1);
		dmost[0] = (short) (startdmost[windowx1]-windowy1);
		
		frameoffset = windowy1*bytesperline + windowx1;
		
		numhits = xdimen; numscans = 0; numbunches = 0;
		maskwallcnt = 0; smostwallcnt = 0; smostcnt = 0; spritesortcnt = 0;

		if (globalcursectnum >= MAXSECTORS)
			globalcursectnum -= MAXSECTORS;
		else
		{
			i = globalcursectnum;
			globalcursectnum = engine.updatesector(globalposx,globalposy,globalcursectnum);
			if (globalcursectnum < 0) globalcursectnum = (short) i;
		}
		
		globparaceilclip = 1;
		globparaflorclip = 1;
		engine.getzsofslope(globalcursectnum,globalposx,globalposy, zofslope);
		int cz = zofslope[CEIL];
		int fz = zofslope[FLOOR];
		if (globalposz < cz) globparaceilclip = 0;
		if (globalposz > fz) globparaflorclip = 0;

		scansector(globalcursectnum);

		if (inpreparemirror)
		{
			inpreparemirror = false;
			mirrorsx1 = xdimen-1; mirrorsx2 = 0;
			for(i=numscans-1;i>=0;i--)
			{
				if (wall[thewall[i]].nextsector < 0) continue;
				if (xb1[i] < mirrorsx1) mirrorsx1 = xb1[i];
				if (xb2[i] > mirrorsx2) mirrorsx2 = xb2[i];
			}

			for(i=0;i<mirrorsx1;i++)
				if (umost[i] <= dmost[i])
					{ umost[i] = 1; dmost[i] = 0; numhits--; }
			for(i=mirrorsx2+1;i<xdimen;i++)
				if (umost[i] <= dmost[i])
					{ umost[i] = 1; dmost[i] = 0; numhits--; }

			drawalls(0);
			numbunches--;
			bunchfirst[0] = bunchfirst[numbunches];
			bunchlast[0] = bunchlast[numbunches];

			mirrorsy1 = Math.min(umost[mirrorsx1],umost[mirrorsx2]);
			mirrorsy2 = Math.max(dmost[mirrorsx1],dmost[mirrorsx2]);
		}
		
		while ((numbunches > 0) && (numhits > 0))
		{
			Arrays.fill(tempbuf, 0, (numbunches+3)>>2, (byte)0);
			tempbuf[0] = 1;

			int closest = 0, j;              //Almost works, but not quite :(
			for(i=1;i<numbunches;i++)
			{
				if ((j = bunchfront(i,closest)) < 0) continue;
				tempbuf[i] = 1;
				if (j == 0) { tempbuf[closest] = 1; closest = i; }
			}
			for(i=0;i<numbunches;i++) //Double-check
			{
				if (tempbuf[i] != 0) continue;
				if ((j = bunchfront(i,closest)) < 0) continue;
				tempbuf[i] = 1;
				if (j == 0) { tempbuf[closest] = 1; closest = i; i = 0; }
			}

			drawalls(closest);

			if (automapping != 0)
			{
				for(int z=bunchfirst[closest];z>=0;z=p2[z])
					show2dwall[thewall[z]>>3] |= pow2char[thewall[z]&7];
			}

			numbunches--;
			bunchfirst[closest] = bunchfirst[numbunches];
			bunchlast[closest] = bunchlast[numbunches];
		}
	}
	
	private int[] cz = new int[5];
	private int[] fz = new int[5];
	private void drawalls(int bunch)
	{
		int z = bunchfirst[bunch];
		short sectnum = thesector[z]; 
		SECTOR sec = sector[sectnum];
		
		int andwstat1 = 0xff; int andwstat2 = 0xff;
		for(;z>=0;z=p2[z])  //uplc/dplc calculation
		{
			andwstat1 &= wallmost(uplc,z,sectnum,0);
			andwstat2 &= wallmost(dplc,z,sectnum,1);
		}
		
		if ((andwstat1&3) != 3)     //draw ceilings
		{
			if ((sec.ceilingstat&3) == 2)
				grouscan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum,0);
			else if ((sec.ceilingstat&1) == 0)
				ceilscan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum);
			else
				parascan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum,0,bunch);
		}
		if ((andwstat2&12) != 12)   //draw floors
		{
			if ((sec.floorstat&3) == 2) //slopes
				grouscan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum,1);
			else if ((sec.floorstat&1) == 0) //solid 
				florscan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum);
			else //background
				parascan(xb1[bunchfirst[bunch]],xb2[bunchlast[bunch]],sectnum,1,bunch);
		}
	
		//DRAW WALLS SECTION!
		for(z=bunchfirst[bunch];z>=0;z=p2[z])
		{
			int x1 = xb1[z]; 
			int x2 = xb2[z], x;
			if (umost[x2] >= dmost[x2])
			{
				for(x=x1;x<x2;x++)
					if (umost[x] < dmost[x]) break;
				if (x >= x2)
				{
					smostwall[smostwallcnt] = z;
					smostwalltype[smostwallcnt] = 0;
					smostwallcnt++;
					continue;
				}
			}

			int wallnum = thewall[z]; WALL wal = wall[wallnum];
			short nextsectnum = wal.nextsector; 
			SECTOR nextsec = null;

			int gotswall = 0;

			int startsmostwallcnt = smostwallcnt;
			short startsmostcnt = smostcnt;

			if (nextsectnum >= 0)
			{
				nextsec = sector[nextsectnum];
				engine.getzsofslope(sectnum,wal.x,wal.y, zofslope);
				cz[0] = zofslope[CEIL]; fz[0] = zofslope[FLOOR];
				engine.getzsofslope(sectnum,wall[wal.point2].x,wall[wal.point2].y, zofslope);
				cz[1] = zofslope[CEIL]; fz[1] = zofslope[FLOOR];
				engine.getzsofslope(nextsectnum,wal.x,wal.y, zofslope);
				cz[2] = zofslope[CEIL]; fz[2] = zofslope[FLOOR];
				engine.getzsofslope(nextsectnum,wall[wal.point2].x,wall[wal.point2].y, zofslope);
				cz[3] = zofslope[CEIL]; fz[3] = zofslope[FLOOR];
				engine.getzsofslope(nextsectnum,globalposx,globalposy, zofslope);
				cz[4] = zofslope[CEIL]; fz[4] = zofslope[FLOOR];

				if ((wal.cstat&48) == 16) maskwall[maskwallcnt++] = z;

				if (((sec.ceilingstat&1) == 0) || ((nextsec.ceilingstat&1) == 0))
				{
					if ((cz[2] <= cz[0]) && (cz[3] <= cz[1]))
					{
						if (globparaceilclip != 0)
							for(x=x1;x<=x2;x++)
								if (uplc[x] > umost[x])
									if (umost[x] <= dmost[x])
									{
										umost[x] = uplc[x];
										if (umost[x] > dmost[x]) numhits--;
									}
					}
					else
					{
						wallmost(dwall,z,nextsectnum,(char)0);
						if ((cz[2] > fz[0]) || (cz[3] > fz[1]))
							for(int i=x1;i<=x2;i++) if (dwall[i] > dplc[i]) dwall[i] = dplc[i];

						globalorientation = wal.cstat;
						globalpicnum = wal.picnum;
						if (globalpicnum >= MAXTILES) globalpicnum = 0;
						globalxpanning = wal.xpanning;
						globalypanning = wal.ypanning;
						globalshiftval = (short) (picsiz[globalpicnum]>>4);
						if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
						globalshiftval = (short) (32-globalshiftval);
						if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum,(short)wallnum+16384);
						globalshade = wal.shade;
						globvis = globalvisibility;
						if (sec.visibility != 0) globvis = mulscale(globvis,(int)((byte)(sec.visibility+16)),4);
						globalpal = wal.pal;
						if (palookup[globalpal] == null) globalpal = 0;	// JBF: fixes crash
						globalyscale = (wal.yrepeat<<(globalshiftval-19));
						if ((globalorientation&4) == 0)
							globalzd = (((globalposz-nextsec.ceilingz)*globalyscale)<<8);
						else
							globalzd = (((globalposz-sec.ceilingz)*globalyscale)<<8);
						globalzd += (globalypanning<<24);
						if ((globalorientation&256) != 0) { globalyscale = -globalyscale; globalzd = -globalzd; }

						if (gotswall == 0) { gotswall = 1; prepwall(z,wal); }
						wallscan(x1,x2,uplc,dwall,swall,lwall);

						if ((cz[2] >= cz[0]) && (cz[3] >= cz[1]))
						{
							for(x=x1;x<=x2;x++)
								if (dwall[x] > umost[x])
									if (umost[x] <= dmost[x])
									{
										umost[x] = dwall[x];
										if (umost[x] > dmost[x]) numhits--;
									}
						}
						else
						{
							for(x=x1;x<=x2;x++)
								if (umost[x] <= dmost[x])
								{
									int i = Math.max(uplc[x],dwall[x]);
									if (i > umost[x])
									{
										umost[x] = (short) i;
										if (umost[x] > dmost[x]) numhits--;
									}
								}
						}
					}
					if ((cz[2] < cz[0]) || (cz[3] < cz[1]) || (globalposz < cz[4]))
					{
						int i = x2-x1+1;
						if (smostcnt+i < MAXYSAVES)
						{
							smoststart[smostwallcnt] = smostcnt;
							smostwall[smostwallcnt] = z;
							smostwalltype[smostwallcnt] = 1;   //1 for umost
							smostwallcnt++;
							System.arraycopy(umost, x1, smost, smostcnt, i);
							smostcnt += i;
						}
					}
				}
				if (((sec.floorstat&1) == 0) || ((nextsec.floorstat&1) == 0))
				{
					if ((fz[2] >= fz[0]) && (fz[3] >= fz[1]))
					{
						if (globparaflorclip != 0)
							for(x=x1;x<=x2;x++)
								if (dplc[x] < dmost[x])
									if (umost[x] <= dmost[x])
									{
										dmost[x] = dplc[x];
										if (umost[x] > dmost[x]) numhits--;
									}
					}
					else
					{
						wallmost(uwall,z,nextsectnum,(char)1);
						if ((fz[2] < cz[0]) || (fz[3] < cz[1]))
							for(int i=x1;i<=x2;i++) if (uwall[i] < uplc[i]) uwall[i] = uplc[i];

						if ((wal.cstat&2) > 0)
						{
							wallnum = wal.nextwall; wal = wall[wallnum];
							globalorientation = wal.cstat;
							globalpicnum = wal.picnum;
							if (globalpicnum >= MAXTILES) globalpicnum = 0;
							globalxpanning = wal.xpanning;
							globalypanning = wal.ypanning;
							if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum,wallnum+16384);
							globalshade = wal.shade;
							globalpal = wal.pal;
							wallnum = thewall[z]; wal = wall[wallnum];
						}
						else
						{
							globalorientation = wal.cstat;
							globalpicnum = wal.picnum;
							if (globalpicnum >= MAXTILES) globalpicnum = 0;
							globalxpanning = wal.xpanning;
							globalypanning = wal.ypanning;
							if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum,wallnum+16384);
							globalshade = wal.shade;
							globalpal = wal.pal;
						}
						if (palookup[globalpal] == null) globalpal = 0;	// JBF: fixes crash
						globvis = globalvisibility;
						if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
						globalshiftval = (short) (picsiz[globalpicnum]>>4);
						if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
						globalshiftval = (short) (32-globalshiftval);
						globalyscale = (wal.yrepeat<<(globalshiftval-19));
						if ((globalorientation&4) == 0)
							globalzd = (((globalposz-nextsec.floorz)*globalyscale)<<8);
						else
							globalzd = (((globalposz-sec.ceilingz)*globalyscale)<<8);
						globalzd += (globalypanning<<24);
						if ((globalorientation&256) != 0) { globalyscale = -globalyscale; globalzd = -globalzd; }

						if (gotswall == 0) { gotswall = 1; prepwall(z,wal); }
						wallscan(x1,x2,uwall,dplc,swall,lwall);

						if ((fz[2] <= fz[0]) && (fz[3] <= fz[1]))
						{
							for(x=x1;x<=x2;x++)
								if (uwall[x] < dmost[x])
									if (umost[x] <= dmost[x])
									{
										dmost[x] = uwall[x];
										if (umost[x] > dmost[x]) numhits--;
									}
						}
						else
						{
							for(x=x1;x<=x2;x++)
								if (umost[x] <= dmost[x])
								{
									int i = Math.min(dplc[x],uwall[x]);
									if (i < dmost[x])
									{
										dmost[x] = (short) i;
										if (umost[x] > dmost[x]) numhits--;
									}
								}
						}
					}
					if ((fz[2] > fz[0]) || (fz[3] > fz[1]) || (globalposz > fz[4]))
					{
						int i = x2-x1+1;
						if (smostcnt+i < MAXYSAVES)
						{
							smoststart[smostwallcnt] = smostcnt;
							smostwall[smostwallcnt] = z;
							smostwalltype[smostwallcnt] = 2;   //2 for dmost
							smostwallcnt++;
							System.arraycopy(dmost, x1, smost, smostcnt, i);
							smostcnt += i;
						}
					}
				}
				if (numhits < 0) return;
				if (((wal.cstat&32) == 0) && ((gotsector[nextsectnum>>3]&pow2char[nextsectnum&7]) == 0))
				{
					if (umost[x2] < dmost[x2])
						scansector(nextsectnum);
					else
					{
						for(x=x1;x<x2;x++)
							if (umost[x] < dmost[x])
								{ scansector(nextsectnum); break; }

						//If can't see sector beyond, then cancel smost array and just
						//store wall!
						if (x == x2)
						{
							smostwallcnt = startsmostwallcnt;
							smostcnt = startsmostcnt;
							smostwall[smostwallcnt] = z;
							smostwalltype[smostwallcnt] = 0;
							smostwallcnt++;
						}
					}
				}
			}
			if ((nextsectnum < 0) || (wal.cstat&32) != 0)   //White/1-way wall
			{
				globalorientation = wal.cstat;
				if (nextsectnum < 0) globalpicnum = wal.picnum;
					else globalpicnum = wal.overpicnum;
				if (globalpicnum >= MAXTILES) globalpicnum = 0;
				globalxpanning = wal.xpanning;
				globalypanning = wal.ypanning;
				if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum,wallnum+16384);
				globalshade = wal.shade;
				globvis = globalvisibility;
				if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
				globalpal = wal.pal;
				if (palookup[globalpal] == null) globalpal = 0;	// JBF: fixes crash
				globalshiftval = (short) (picsiz[globalpicnum]>>4);
				if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
				globalshiftval = (short) (32-globalshiftval);
				globalyscale = (wal.yrepeat<<(globalshiftval-19));
				if (nextsectnum >= 0)
				{
					if ((globalorientation&4) == 0) globalzd = globalposz-nextsec.ceilingz;
					else globalzd = globalposz-sec.ceilingz;
				}
				else
				{
					if ((globalorientation&4) == 0) globalzd = globalposz-sec.ceilingz;
					else globalzd = globalposz-sec.floorz;
				}
				globalzd = ((globalzd*globalyscale)<<8) + (globalypanning<<24);
				if ((globalorientation&256) != 0) { globalyscale = -globalyscale; globalzd = -globalzd; }

				if (gotswall == 0) { gotswall = 1; prepwall(z,wal); }

				wallscan(x1,x2,uplc,dplc,swall,lwall);

				for(x=x1;x<=x2;x++)
					if (umost[x] <= dmost[x])
						{ umost[x] = 1; dmost[x] = 0; numhits--; }
				smostwall[smostwallcnt] = z;
				smostwalltype[smostwallcnt] = 0;
				smostwallcnt++;
			}
		}
	}
	
	private void drawsprite(int snum)
	{
		SPRITE tspr = tspriteptr[snum];

		int xb = spritesx[snum];
		int yp = spritesy[snum];
		int tilenum = tspr.picnum, vtilenum = 0;
		short spritenum = tspr.owner;
		short cstat = tspr.cstat;
		
		if ((cstat&48)==48) vtilenum = tilenum;	// if the game wants voxels, it gets voxels
		else if ((cstat&48)!=48 && (usevoxels) && (tiletovox[tilenum] != -1)) {
			vtilenum = tiletovox[tilenum];
			cstat |= 48;
		}
		
		if ((cstat&48) != 48)
		{
			if ((picanm[tilenum]&192) != 0) tilenum += engine.animateoffs(tilenum,spritenum+32768);
			if ((tilesizx[tilenum] <= 0) || (tilesizy[tilenum] <= 0) || (spritenum < 0))
				return;
		}
		if ((tspr.xrepeat <= 0) || (tspr.yrepeat <= 0)) return;
		
		short sectnum = tspr.sectnum; 
		SECTOR sec = sector[sectnum];
		globalpal = tspr.pal;
		if (palookup[globalpal] == null) globalpal = 0;	// JBF: fixes null-pointer crash
		globalshade = tspr.shade;
		if ((cstat&2) != 0)
		{
			if ((cstat&512) != 0) a.settransreverse(); else a.settransnormal();
		}

		int xoff = ((byte)((picanm[tilenum]>>8)&255))+(tspr.xoffset);
		int yoff = ((byte)((picanm[tilenum]>>16)&255))+(tspr.yoffset);
		
		int xv, yv, x1, y1, x2, y2, dax, day, dax1, dax2, dalx2, darx2;
		int i, j, k, x, y, z, zz, z1, z2, xp1, yp1,xp2, yp2;
		switch ((cstat >> 4) & 3) {
			case 0: // Face sprite
				if (yp <= (4<<8)) return;

				long siz = divscale(xdimenscale,yp,19);

				xv = mulscale((tspr.xrepeat)<<16,xyaspect,16);

				short xspan = tilesizx[tilenum];
				short yspan = tilesizy[tilenum];
				int xsiz = mulscale(siz,xv*xspan,30);
				int ysiz = mulscale(siz,tspr.yrepeat*yspan,14);

				if (((tilesizx[tilenum]>>11) >= xsiz) || (yspan >= (ysiz>>1)))
					return;  //Watch out for divscale overflow

				x1 = xb-(xsiz>>1);
				if ((xspan&1) != 0) x1 += mulscale(siz,xv,31);  //Odd xspans
				i = mulscale(siz,xv*xoff,30);
				if ((cstat&4) == 0) x1 -= i; else x1 += i;

				 y1 = mulscale(tspr.z-globalposz,siz,16);
				y1 -= mulscale(siz,tspr.yrepeat*yoff,14);
				y1 += ((int)globalhoriz<<8)-ysiz;
				if ((cstat&128) != 0)
				{
					y1 += (ysiz>>1);
					if ((yspan&1) != 0) y1 += mulscale(siz,tspr.yrepeat,15);  //Odd yspans
				}

				x2 = x1+xsiz-1;
				y2 = y1+ysiz-1;
				if ((y1|255) >= (y2|255)) return;

				int lx = (x1>>8)+1; if (lx < 0) lx = 0;
				int rx = (x2>>8); if (rx >= xdimen) rx = xdimen-1;
				if (lx > rx) return;

				long yinc = divscale(yspan,ysiz,32);

				long startum = 0;
				if ((sec.ceilingstat&3) == 0)
					startum = (long)globalhoriz+mulscale(siz,sec.ceilingz-globalposz,24)-1;

				long startdm = 0x7fffffff;
				if ((sec.floorstat&3) == 0)
					startdm = (long)globalhoriz+mulscale(siz,sec.floorz-globalposz,24)+1;

				if ((y1>>8) > startum) startum = (y1>>8);
				if ((y2>>8) < startdm) startdm = (y2>>8);

				if (startum < -32768) startum = -32768;
				if (startdm > 32767) startdm = 32767;
				if (startum >= startdm) return;

				int linum;
				long linuminc;
				if ((cstat&4) == 0)
				{
					linuminc = divscale(xspan,xsiz,24);
					linum = mulscale((lx<<8)-x1,linuminc,8);
				}
				else
				{
					linuminc = -divscale(xspan,xsiz,24);
					linum = mulscale((lx<<8)-x2,linuminc,8);
				}
				
				if ((cstat&8) > 0)
				{
					yinc = -yinc;
					i = y1; y1 = y2; y2 = i;
				}

				for(x=lx;x<=rx;x++)
				{
					uwall[x] = (short) Math.max(startumost[x+windowx1]-windowy1,(short)startum);
					dwall[x] = (short) Math.min(startdmost[x+windowx1]-windowy1,(short)startdm);
				}

				int daclip = 0;
				for(i=smostwallcnt-1;i>=0;i--)
				{
					if ((smostwalltype[i]&daclip) != 0) continue;
					j = smostwall[i];
					if ((xb1[j] > rx) || (xb2[j] < lx)) continue;
					if ((yp <= yb1[j]) && (yp <= yb2[j])) continue;
					if (spritewallfront(tspr,thewall[j]) && ((yp <= yb1[j]) || (yp <= yb2[j]))) continue;

					dalx2 = Math.max(xb1[j],lx); 
					darx2 = Math.min(xb2[j],rx);
					
					switch(smostwalltype[i])
					{
						case 0:
							if (dalx2 <= darx2)
							{
								if ((dalx2 == lx) && (darx2 == rx)) return;
								for (k=dalx2; k<=darx2; k++) dwall[k] = 0;
							}
							break;
						case 1:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] > uwall[x]) uwall[x] = smost[k+x];
							if ((dalx2 == lx) && (darx2 == rx)) daclip |= 1;
							break;
						case 2:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] < dwall[x]) dwall[x] = smost[k+x];
							if ((dalx2 == lx) && (darx2 == rx)) daclip |= 2;
							break;
					}
				}
					
				if (uwall[rx] >= dwall[rx])
				{
					for(x=lx;x<rx;x++)
						if (uwall[x] < dwall[x]) break;
					if (x == rx) return;
				}

				z2 = tspr.z - ((yoff*tspr.yrepeat)<<2);
				if ((cstat&128) != 0)
				{
					z2 += ((yspan*tspr.yrepeat)<<1);
					if ((yspan&1) != 0) z2 += (tspr.yrepeat<<1);        //Odd yspans
				}
				z1 = z2 - ((yspan*tspr.yrepeat)<<2);

				globalorientation = 0;
				globalpicnum = (short) tilenum;
				if (globalpicnum >= MAXTILES) globalpicnum = 0;
				globalxpanning = 0;
				globalypanning = 0;
				globvis = globalvisibility;
				if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
				globalshiftval = (short) (picsiz[globalpicnum]>>4);
				if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
				globalshiftval = (short) (32-globalshiftval);
				globalyscale = (int) divscale(512,tspr.yrepeat,globalshiftval-19);
				globalzd = (((globalposz-z1)*globalyscale)<<8);
				if ((cstat&8) > 0)
				{
					globalyscale = -globalyscale;
					globalzd = (((globalposz-z2)*globalyscale)<<8);
				}

				qinterpolatedown16(lwall,lx,rx-lx+1,linum,linuminc);
				
				Arrays.fill(swall, lx, rx+1, mulscale(yp,xdimscale,19));
				if ((cstat&2) == 0)
					maskwallscan(lx,rx,uwall,dwall,swall,lwall);
				else
					transmaskwallscan(lx,rx);

				break;
			case 1: // Wall sprite
				if ((cstat&4) > 0) xoff = -xoff;
				if ((cstat&8) > 0) yoff = -yoff;

				xspan = tilesizx[tilenum]; yspan = tilesizy[tilenum];
				xv = tspr.xrepeat*sintable[(tspr.ang+2560+1536)&2047];
				yv = tspr.xrepeat*sintable[(tspr.ang+2048+1536)&2047];
				i = (xspan>>1)+xoff;
				x1 = tspr.x-globalposx-mulscale(xv,i,16); x2 = x1+mulscale(xv,xspan,16);
				y1 = tspr.y-globalposy-mulscale(yv,i,16); y2 = y1+mulscale(yv,xspan,16);

				yp1 = dmulscale(x1,cosviewingrangeglobalang,y1,sinviewingrangeglobalang,6);
				yp2 = dmulscale(x2,cosviewingrangeglobalang,y2,sinviewingrangeglobalang,6);
				if ((yp1 <= 0) && (yp2 <= 0)) return;
				xp1 = dmulscale(y1,cosglobalang,-x1,singlobalang,6);
				xp2 = dmulscale(y2,cosglobalang,-x2,singlobalang,6);

				x1 += globalposx; y1 += globalposy;
				x2 += globalposx; y2 += globalposy;

				int swapped = 0;
				if (dmulscale(xp1,yp2,-xp2,yp1,32) >= 0)  //If wall's NOT facing you
				{
					if ((cstat&64) != 0) return;
					i = xp1; xp1 = xp2; xp2 = i;
					i = yp1; yp1 = yp2; yp2 = i;
					i = x1; x1 = x2; x2 = i;
					i = y1; y1 = y2; y2 = i;
					swapped = 1;
				}

				if (xp1 >= -yp1)
				{
					if (xp1 > yp1) return;

					if (yp1 == 0) return;
					xb1[MAXWALLSB-1] = halfxdimen + scale(xp1,halfxdimen,yp1);
					if (xp1 >= 0) xb1[MAXWALLSB-1]++;   //Fix for SIGNED divide
					if (xb1[MAXWALLSB-1] >= xdimen) xb1[MAXWALLSB-1] = xdimen-1;
					yb1[MAXWALLSB-1] = yp1;
				}
				else
				{
					if (xp2 < -yp2) return;
					xb1[MAXWALLSB-1] = 0;
					i = yp1-yp2+xp1-xp2;
					if (i == 0) return;
					yb1[MAXWALLSB-1] = yp1 + scale(yp2-yp1,xp1+yp1,i);
				}
				if (xp2 <= yp2)
				{
					if (xp2 < -yp2) return;

					if (yp2 == 0) return;
					xb2[MAXWALLSB-1] = halfxdimen + scale(xp2,halfxdimen,yp2) - 1;
					if (xp2 >= 0) xb2[MAXWALLSB-1]++;   //Fix for SIGNED divide
					if (xb2[MAXWALLSB-1] >= xdimen) xb2[MAXWALLSB-1] = xdimen-1;
					yb2[MAXWALLSB-1] = yp2;
				}
				else
				{
					if (xp1 > yp1) return;

					xb2[MAXWALLSB-1] = xdimen-1;
					i = xp2-xp1+yp1-yp2;
					if (i == 0) return;
					yb2[MAXWALLSB-1] = yp1 + scale(yp2-yp1,yp1-xp1,i);
				}

				if ((yb1[MAXWALLSB-1] < 256) || (yb2[MAXWALLSB-1] < 256) || (xb1[MAXWALLSB-1] > xb2[MAXWALLSB-1]))
					return;

				int topinc = -mulscale(yp1,xspan,10);
				int top = (((mulscale(xp1,xdimen,10) - mulscale(xb1[MAXWALLSB-1]-halfxdimen,yp1,9))*xspan)>>3);
				int botinc = ((yp2-yp1)>>8);
				int bot = mulscale(xp1-xp2,xdimen,11) + mulscale(xb1[MAXWALLSB-1]-halfxdimen,botinc,2);

				j = xb2[MAXWALLSB-1]+3;
				z = mulscale(top,krecipasm(bot), 20);
				lwall[xb1[MAXWALLSB-1]] = (z>>8);
				for(x=xb1[MAXWALLSB-1]+4;x<=j;x+=4)
				{
					top += topinc; bot += botinc;
					zz = z; z = mulscale(top,krecipasm(bot),20);
					lwall[x] = (z>>8);
					i = ((z+zz)>>1);
					lwall[x-2] = (i>>8);
					lwall[x-3] = ((i+zz)>>9);
					lwall[x-1] = ((i+z)>>9);
				}

				if (lwall[xb1[MAXWALLSB-1]] < 0) lwall[xb1[MAXWALLSB-1]] = 0;
				if (lwall[xb2[MAXWALLSB-1]] >= xspan) lwall[xb2[MAXWALLSB-1]] = xspan-1;

				if ((swapped^(((cstat&4)>0)?1:0)) > 0)
				{
					j = xspan-1;
					for(x=xb1[MAXWALLSB-1];x<=xb2[MAXWALLSB-1];x++)
						lwall[x] = j-lwall[x];
				}

				rx1[MAXWALLSB-1] = xp1; ry1[MAXWALLSB-1] = yp1;
				rx2[MAXWALLSB-1] = xp2; ry2[MAXWALLSB-1] = yp2;

				int hplc = (int) divscale(xdimenscale,yb1[MAXWALLSB-1],19);
				long hinc = divscale(xdimenscale,yb2[MAXWALLSB-1],19);
				hinc = (hinc-hplc)/(xb2[MAXWALLSB-1]-xb1[MAXWALLSB-1]+1);

				z2 = tspr.z - ((yoff*tspr.yrepeat)<<2);
				if ((cstat&128) != 0)
				{
					z2 += ((yspan*tspr.yrepeat)<<1);
					if ((yspan&1) != 0) z2 += (tspr.yrepeat<<1);        //Odd yspans
				}
				z1 = z2 - ((yspan*tspr.yrepeat)<<2);

				globalorientation = 0;
				globalpicnum = (short) tilenum;
				if (globalpicnum >= MAXTILES) globalpicnum = 0;
				globalxpanning = 0;
				globalypanning = 0;
				globvis = globalvisibility;
				
				if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
				globalshiftval = (short) (picsiz[globalpicnum]>>4);
				if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
				globalshiftval = (short) (32-globalshiftval);
				globalyscale = (int) divscale(512,tspr.yrepeat,globalshiftval-19);
				globalzd = (((globalposz-z1)*globalyscale)<<8);
				if ((cstat&8) > 0)
				{
					globalyscale = -globalyscale;
					globalzd = (((globalposz-z2)*globalyscale)<<8);
				}

				if (((sec.ceilingstat&1) == 0) && (z1 < sec.ceilingz))
					z1 = sec.ceilingz;
				if (((sec.floorstat&1) == 0) && (z2 > sec.floorz))
					z2 = sec.floorz;

				owallmost(uwall,(MAXWALLSB-1),z1-globalposz);
				owallmost(dwall,(MAXWALLSB-1),z2-globalposz);
				for(i=xb1[MAXWALLSB-1];i<=xb2[MAXWALLSB-1];i++)
					{ swall[i] = (krecipasm(hplc)<<2); hplc += hinc; }

				for(i=smostwallcnt-1;i>=0;i--)
				{
					j = smostwall[i];

					if ((xb1[j] > xb2[MAXWALLSB-1]) || (xb2[j] < xb1[MAXWALLSB-1])) continue;

					dalx2 = xb1[j]; darx2 = xb2[j];
					if (Math.max(yb1[MAXWALLSB-1],yb2[MAXWALLSB-1]) > Math.min(yb1[j],yb2[j]))
					{
						if (Math.min(yb1[MAXWALLSB-1],yb2[MAXWALLSB-1]) > Math.max(yb1[j],yb2[j]))
						{
							x = 0x80000000;
						}
						else
						{
							x = thewall[j]; xp1 = wall[x].x; yp1 = wall[x].y;
							x = wall[x].point2; xp2 = wall[x].x; yp2 = wall[x].y;

							z1 = (xp2-xp1)*(y1-yp1) - (yp2-yp1)*(x1-xp1);
							z2 = (xp2-xp1)*(y2-yp1) - (yp2-yp1)*(x2-xp1);
							if ((z1^z2) >= 0)
								x = (z1+z2);
							else
							{
								z1 = (x2-x1)*(yp1-y1) - (y2-y1)*(xp1-x1);
								z2 = (x2-x1)*(yp2-y1) - (y2-y1)*(xp2-x1);

								if ((z1^z2) >= 0)
									x = -(z1+z2);
								else
								{
									if ((xp2-xp1)*(tspr.y-yp1) == (tspr.x-xp1)*(yp2-yp1))
									{
										if (wall[thewall[j]].nextsector == tspr.sectnum)
											x = 0x80000000;
										else
											x = 0x7fffffff;
									}
									else
									{     //INTERSECTION!
										x = (xp1-globalposx) + scale(xp2-xp1,z1,z1-z2);
										y = (yp1-globalposy) + scale(yp2-yp1,z1,z1-z2);

										yp1 = dmulscale(x,cosglobalang,y,singlobalang,14);
										if (yp1 > 0)
										{
											xp1 = dmulscale(y,cosglobalang,-x,singlobalang,14);

											x = halfxdimen + scale(xp1,halfxdimen,yp1);
											if (xp1 >= 0) x++;   //Fix for SIGNED divide

											if (z1 < 0)
												{ if (dalx2 < x) dalx2 = x; }
											else
												{ if (darx2 > x) darx2 = x; }
											x = 0x80000001;
										}
										else
											x = 0x7fffffff;
									}
								}
							}
						}
						if (x < 0)
						{
							if (dalx2 < xb1[MAXWALLSB-1]) dalx2 = xb1[MAXWALLSB-1];
							if (darx2 > xb2[MAXWALLSB-1]) darx2 = xb2[MAXWALLSB-1];
							switch(smostwalltype[i])
							{
								case 0:
									if (dalx2 <= darx2)
									{
										if ((dalx2 == xb1[MAXWALLSB-1]) && (darx2 == xb2[MAXWALLSB-1])) return;
										for (k=dalx2; k<=darx2; k++) dwall[k] = 0;
									}
									break;
								case 1:
									k = smoststart[i] - xb1[j];
									for(x=dalx2;x<=darx2;x++)
										if (smost[k+x] > uwall[x]) uwall[x] = smost[k+x];
									break;
								case 2:
									k = smoststart[i] - xb1[j];
									for(x=dalx2;x<=darx2;x++)
										if (smost[k+x] < dwall[x]) dwall[x] = smost[k+x];
									break;
							}
						}
					}
				}
				
				if ((cstat&2) == 0) {
					maskwallscan(xb1[MAXWALLSB-1],xb2[MAXWALLSB-1],uwall,dwall,swall,lwall);
				} else {
					transmaskwallscan(xb1[MAXWALLSB-1],xb2[MAXWALLSB-1]);
				}
				break;
			case 2: // Floor sprite
				if ((cstat&64) != 0)
					if ((globalposz > tspr.z) == ((cstat&8)==0))
						return;

				if ((cstat&4) > 0) xoff = -xoff;
				if ((cstat&8) > 0) yoff = -yoff;
				xspan = tilesizx[tilenum];
				yspan = tilesizy[tilenum];

					//Rotate center point
				dax = tspr.x-globalposx;
				day = tspr.y-globalposy;
				rzi[0] = dmulscale(cosglobalang,dax,singlobalang,day,10);
				rxi[0] = dmulscale(cosglobalang,day,-singlobalang,dax,10);

					//Get top-left corner
				i = ((tspr.ang+2048-(int)globalang)&2047);
				int cosang = sintable[(i+512)&2047]; 
				int sinang = sintable[i];
				dax = ((xspan>>1)+xoff)*tspr.xrepeat;
				day = ((yspan>>1)+yoff)*tspr.yrepeat;
				rzi[0] += dmulscale(sinang,dax,cosang,day,12);
				rxi[0] += dmulscale(sinang,day,-cosang,dax,12);

					//Get other 3 corners
				dax = xspan*tspr.xrepeat;
				day = yspan*tspr.yrepeat;
				rzi[1] = rzi[0]-mulscale(sinang,dax,12);
				rxi[1] = rxi[0]+mulscale(cosang,dax,12);
				dax = -mulscale(cosang,day,12);
				day = -mulscale(sinang,day,12);
				rzi[2] = rzi[1]+dax; rxi[2] = rxi[1]+day;
				rzi[3] = rzi[0]+dax; rxi[3] = rxi[0]+day;

					//Put all points on same z
				ryi[0] = scale((tspr.z-globalposz),yxaspect,320<<8);
				if (ryi[0] == 0) return;
				ryi[1] = ryi[2] = ryi[3] = ryi[0];

				if ((cstat&4) == 0)
					{ z = 0; z1 = 1; z2 = 3; }
				else
					{ z = 1; z1 = 0; z2 = 2; }

				dax = rzi[z1]-rzi[z]; day = rxi[z1]-rxi[z];
				bot = dmulscale(dax,dax,day,day,8);
				if (((klabs(dax)>>13) >= bot) || ((klabs(day)>>13) >= bot)) return;
				globalx1 = (int)divscale(dax,bot,18);
				globalx2 = (int)divscale(day,bot,18);

				dax = rzi[z2]-rzi[z]; day = rxi[z2]-rxi[z];
				bot = dmulscale(dax,dax,day,day,8);
				if (((klabs(dax)>>13) >= bot) || ((klabs(day)>>13) >= bot)) return;
				globaly1 = (int)divscale(dax,bot,18);
				globaly2 = (int)divscale(day,bot,18);

					//Calculate globals for hline texture mapping function
				globalxpanning = (rxi[z]<<12);
				globalypanning = (rzi[z]<<12);
				globalzd = (ryi[z]<<12);

				rzi[0] = mulscale(rzi[0],viewingrange,16);
				rzi[1] = mulscale(rzi[1],viewingrange,16);
				rzi[2] = mulscale(rzi[2],viewingrange,16);
				rzi[3] = mulscale(rzi[3],viewingrange,16);

				if (ryi[0] < 0)   //If ceilsprite is above you, reverse order of points
				{
					i = rxi[1]; rxi[1] = rxi[3]; rxi[3] = i;
					i = rzi[1]; rzi[1] = rzi[3]; rzi[3] = i;
				}


					//Clip polygon in 3-space
				int npoints = 4;

					//Clip edge 1
				int npoints2 = 0;
				int zzsgn = rxi[0]+rzi[0];
				for(z=0;z<npoints;z++)
				{
					zz = z+1; if (zz == npoints) zz = 0;
					int zsgn = zzsgn; zzsgn = rxi[zz]+rzi[zz];
					if (zsgn >= 0)
					{
						rxi2[npoints2] = rxi[z]; ryi2[npoints2] = ryi[z]; rzi2[npoints2] = rzi[z];
						npoints2++;
					}
					if ((zsgn^zzsgn) < 0)
					{
						long t = divscale(zsgn,zsgn-zzsgn,30);
						rxi2[npoints2] = rxi[z] + mulscale(t,rxi[zz]-rxi[z],30);
						ryi2[npoints2] = ryi[z] + mulscale(t,ryi[zz]-ryi[z],30);
						rzi2[npoints2] = rzi[z] + mulscale(t,rzi[zz]-rzi[z],30);
						npoints2++;
					}
				}
				if (npoints2 <= 2) return;

					//Clip edge 2
				npoints = 0;
				zzsgn = rxi2[0]-rzi2[0];
				for(z=0;z<npoints2;z++)
				{
					zz = z+1; if (zz == npoints2) zz = 0;
					int zsgn = zzsgn; zzsgn = rxi2[zz]-rzi2[zz];
					if (zsgn <= 0)
					{
						rxi[npoints] = rxi2[z]; ryi[npoints] = ryi2[z]; rzi[npoints] = rzi2[z];
						npoints++;
					}
					if ((zsgn^zzsgn) < 0)
					{
						long t = divscale(zsgn,zsgn-zzsgn,30);
						rxi[npoints] = rxi2[z] + mulscale(t,rxi2[zz]-rxi2[z],30);
						ryi[npoints] = ryi2[z] + mulscale(t,ryi2[zz]-ryi2[z],30);
						rzi[npoints] = rzi2[z] + mulscale(t,rzi2[zz]-rzi2[z],30);
						npoints++;
					}
				}
				if (npoints <= 2) return;

					//Clip edge 3
				npoints2 = 0;
				zzsgn = (ryi[0]*halfxdimen + (rzi[0]*((int)globalhoriz-0)));
				for(z=0;z<npoints;z++)
				{
					zz = z+1; if (zz == npoints) zz = 0;
					int zsgn = zzsgn; zzsgn = ryi[zz]*halfxdimen + (rzi[zz]*((int)globalhoriz));
					if (zsgn >= 0)
					{
						rxi2[npoints2] = rxi[z];
						ryi2[npoints2] = ryi[z];
						rzi2[npoints2] = rzi[z];
						npoints2++;
					}
					if ((zsgn^zzsgn) < 0)
					{
						long t = divscale(zsgn,zsgn-zzsgn,30);
						rxi2[npoints2] = rxi[z] + mulscale(t,rxi[zz]-rxi[z],30);
						ryi2[npoints2] = ryi[z] + mulscale(t,ryi[zz]-ryi[z],30);
						rzi2[npoints2] = rzi[z] + mulscale(t,rzi[zz]-rzi[z],30);
						npoints2++;
					}
				}
				if (npoints2 <= 2) return;

					//Clip edge 4
				npoints = 0;
				zzsgn = ryi2[0]*halfxdimen + (rzi2[0]*((int)globalhoriz-ydimen));
				for(z=0;z<npoints2;z++)
				{
					zz = z+1; if (zz == npoints2) zz = 0;
					int zsgn = zzsgn; zzsgn = ryi2[zz]*halfxdimen + (rzi2[zz]*((int)globalhoriz-ydimen));
					if (zsgn <= 0)
					{
						rxi[npoints] = rxi2[z];
						ryi[npoints] = ryi2[z];
						rzi[npoints] = rzi2[z];
						npoints++;
					}
					if ((zsgn^zzsgn) < 0)
					{
						long t = divscale(zsgn,zsgn-zzsgn,30);
						rxi[npoints] = rxi2[z] + mulscale(t,rxi2[zz]-rxi2[z],30);
						ryi[npoints] = ryi2[z] + mulscale(t,ryi2[zz]-ryi2[z],30);
						rzi[npoints] = rzi2[z] + mulscale(t,rzi2[zz]-rzi2[z],30);
						npoints++;
					}
				}
				if (npoints <= 2) return;

					//Project onto screen
				int lpoint = -1; 
				long lmax = 0x7fffffff;
				int rpoint = -1; 
				long rmax = 0x80000000;
				for(z=0;z<npoints;z++)
				{
					xsi[z] = scale(rxi[z],xdimen<<15,rzi[z]) + (xdimen<<15);
					ysi[z] = scale(ryi[z],xdimen<<15,rzi[z]) + ((int)globalhoriz<<16);
					if (xsi[z] < 0) xsi[z] = 0;
					if (xsi[z] > (xdimen<<16)) xsi[z] = (xdimen<<16);
					if (ysi[z] < (0<<16)) ysi[z] = (0<<16);
					if (ysi[z] > (ydimen<<16)) ysi[z] = (ydimen<<16);
					if (xsi[z] < lmax) { lmax = xsi[z]; lpoint = z; }
					if (xsi[z] > rmax) { rmax = xsi[z]; rpoint = z; }
				}

					//Get uwall arrays
				for(z=lpoint;z!=rpoint;z=zz)
				{
					zz = z+1; if (zz == npoints) zz = 0;

					dax1 = ((xsi[z]+65535)>>16);
					dax2 = ((xsi[zz]+65535)>>16);
					if (dax2 > dax1)
					{
						yinc = divscale(ysi[zz]-ysi[z],xsi[zz]-xsi[z],16);
						y = ysi[z] + mulscale((dax1<<16)-xsi[z],yinc,16);
						qinterpolatedown16short(uwall,dax1,dax2-dax1,y,yinc);
					}
				}

					//Get dwall arrays
				for(;z!=lpoint;z=zz)
				{
					zz = z+1; if (zz == npoints) zz = 0;

					dax1 = ((xsi[zz]+65535)>>16);
					dax2 = ((xsi[z]+65535)>>16);
					if (dax2 > dax1)
					{
						yinc = divscale(ysi[zz]-ysi[z],xsi[zz]-xsi[z],16);
						y = ysi[zz] + mulscale((dax1<<16)-xsi[zz],yinc,16);
						qinterpolatedown16short(dwall,dax1,dax2-dax1,y,yinc);
					}
				}

				lx = (int) ((lmax+65535)>>16);
				rx = (int) ((rmax+65535)>>16);
				for(x=lx;x<=rx;x++)
				{
					uwall[x] = (short) Math.max(uwall[x],startumost[x+windowx1]-windowy1);
					dwall[x] = (short) Math.min(dwall[x],startdmost[x+windowx1]-windowy1);
				}

					//Additional uwall/dwall clipping goes here
				for(i=smostwallcnt-1;i>=0;i--)
				{
					j = smostwall[i];
					if ((xb1[j] > rx) || (xb2[j] < lx)) continue;
					if ((yp <= yb1[j]) && (yp <= yb2[j])) continue;

						//if (spritewallfront(tspr,thewall[j]) == 0)
					x = thewall[j]; xp1 = wall[x].x; yp1 = wall[x].y;
					x = wall[x].point2; xp2 = wall[x].x; yp2 = wall[x].y;
					x = (xp2-xp1)*(tspr.y-yp1)-(tspr.x-xp1)*(yp2-yp1);
					if ((yp > yb1[j]) && (yp > yb2[j])) x = -1;
					if ((x >= 0) && ((x != 0) || (wall[thewall[j]].nextsector != tspr.sectnum))) continue;

					dalx2 = Math.max(xb1[j],lx); darx2 = Math.min(xb2[j],rx);

					switch(smostwalltype[i])
					{
						case 0:
							if (dalx2 <= darx2)
							{
								if ((dalx2 == lx) && (darx2 == rx)) return;
								for (x=dalx2; x<=darx2; x++) dwall[x] = 0;
							}
							break;
						case 1:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] > uwall[x]) uwall[x] = smost[k+x];
							break;
						case 2:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] < dwall[x]) dwall[x] = smost[k+x];
							break;
					}
				}

				globalorientation = cstat;
				globalpicnum = (short) tilenum;
				if (globalpicnum >= MAXTILES) globalpicnum = 0;
	
				if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);
				engine.setgotpic(globalpicnum);
				globalbufplc = waloff[globalpicnum];

				globvis = mulscale(globalhisibility,viewingrange,16);
				if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
			
				x = picsiz[globalpicnum]; y = ((x>>4)&15); x &= 15;
				if (pow2long[x] != xspan)
				{
					x++;
					globalx1 = mulscale(globalx1,xspan,x);
					globalx2 = mulscale(globalx2,xspan,x);
				}

				dax = globalxpanning; day = globalypanning;
				globalxpanning = -dmulscale(globalx1,day,globalx2,dax,6);
				globalypanning = -dmulscale(globaly1,day,globaly2,dax,6);

				globalx2 = mulscale(globalx2,viewingrange,16);
				globaly2 = mulscale(globaly2,viewingrange,16);
				globalzd = mulscale(globalzd,viewingrangerecip,16);

				globalx1 = (globalx1-globalx2)*halfxdimen;
				globaly1 = (globaly1-globaly2)*halfxdimen;

				if ((cstat&2) == 0)
					a.msethlineshift(x,y);
				else
					a.tsethlineshift(x,y);
				
				//Draw it!
				ceilspritescan(lx,rx-1);
				break;
			case 3: // Voxel sprite
				long nyrepeat;

				lx = 0; rx = xdim-1;
				for(x=lx;x<=rx;x++)
				{
					lwall[x] = startumost[x+windowx1]-windowy1;
					swall[x] = startdmost[x+windowx1]-windowy1;
				}
				for(i=smostwallcnt-1;i>=0;i--)
				{
					j = smostwall[i];
					if ((xb1[j] > rx) || (xb2[j] < lx)) continue;
					if ((yp <= yb1[j]) && (yp <= yb2[j])) continue;
					if (spritewallfront(tspr,thewall[j]) && ((yp <= yb1[j]) || (yp <= yb2[j]))) continue;

					dalx2 = Math.max(xb1[j],lx); darx2 = Math.min(xb2[j],rx);

					switch(smostwalltype[i])
					{
						case 0:
							if (dalx2 <= darx2)
							{
								if ((dalx2 == lx) && (darx2 == rx)) return;
								for (x=dalx2; x<=darx2; x++) swall[x] = 0;
							}
							break;
						case 1:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] > lwall[x]) lwall[x] = smost[k+x];
							break;
						case 2:
							k = smoststart[i] - xb1[j];
							for(x=dalx2;x<=darx2;x++)
								if (smost[k+x] < swall[x]) swall[x] = smost[k+x];
							break;
					}
				}

				if (lwall[rx] >= swall[rx])
				{
					for(x=lx;x<rx;x++)
						if (lwall[x] < swall[x]) break;
					if (x == rx) return;
				}

				for(i=0;i<MAXVOXMIPS;i++)
					if (voxoff[vtilenum][i] == null)
					{
						kloadvoxel(vtilenum);
						break;
					}
				
				Model m = voxoff[vtilenum][0];
				if(m == null) break;
				if (m.scale == 65536)
				{
					nyrepeat = ((tspr.yrepeat)<<16);
				}
				else
				{
					nyrepeat = (tspr.yrepeat)*(int)m.scale;
				}

				if ((cstat&128) == 0) tspr.z -= mulscale((int)m.zadd,nyrepeat,22);
				yoff = (int)((byte)((picanm[sprite[tspr.owner].picnum]>>16)&255))+(tspr.yoffset);
				tspr.z -= mulscale(yoff,nyrepeat,14);

				globvis = globalvisibility;
				if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
				
				i = tspr.ang+1536;
				drawvox(tspr.x,tspr.y,tspr.z,i,tspr.xrepeat,tspr.yrepeat,vtilenum,tspr.shade,tspr.pal,lwall,swall);
				break;
		}
		if (automapping == 1) show2dsprite[spritenum>>3] |= pow2char[spritenum&7];
	}
	
	private void kloadvoxel(int voxindex)
	{
		
	}
	
	private void drawvox(int dasprx, int daspry, int dasprz, int dasprang,
			  int daxscale, int dayscale, int daindex,
			  int dashade, int dapal, int[] daumost, int[] dadmost)
	{
		//XXX
	}
	
	private void drawmaskwall(int damaskwallcnt)
	{
		int i, j, k, x, z, z1, z2, lx, rx;
		short sectnum;
		SECTOR sec, nsec;
		WALL wal;

		z = maskwall[damaskwallcnt];
		wal = wall[thewall[z]];
		sectnum = thesector[z]; sec = sector[sectnum];
		nsec = sector[wal.nextsector];
		z1 = Math.max(nsec.ceilingz,sec.ceilingz);
		z2 = Math.min(nsec.floorz,sec.floorz);

		wallmost(uwall,z,sectnum,0);
		wallmost(uplc,z,wal.nextsector,0);
		for(x=xb1[z];x<=xb2[z];x++) if (uplc[x] > uwall[x]) uwall[x] = uplc[x];
		wallmost(dwall,z,sectnum,1);
		wallmost(dplc,z,wal.nextsector,1);
		for(x=xb1[z];x<=xb2[z];x++) if (dplc[x] < dwall[x]) dwall[x] = dplc[x];
		prepwall(z,wal);

		globalorientation = wal.cstat;
		globalpicnum = wal.overpicnum;
		if (globalpicnum >= MAXTILES) globalpicnum = 0;
		globalxpanning = wal.xpanning;
		globalypanning = wal.ypanning;
		if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum,(short)thewall[z]+16384);
		globalshade =wal.shade;
		globvis = globalvisibility;
		if (sec.visibility != 0) globvis = mulscale(globvis,sec.visibility+16,4);
		globalpal = wal.pal;
		globalshiftval = (short) (picsiz[globalpicnum]>>4);
		if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
		globalshiftval = (short) (32-globalshiftval);
		globalyscale = (wal.yrepeat<<(globalshiftval-19));
		if ((globalorientation&4) == 0)
			globalzd = (((globalposz-z1)*globalyscale)<<8);
		else
			globalzd = (((globalposz-z2)*globalyscale)<<8);
		globalzd += (globalypanning<<24);
		if ((globalorientation&256) != 0) { globalyscale = -globalyscale; globalzd = -globalzd; }

		for(i=smostwallcnt-1;i>=0;i--)
		{
			j = smostwall[i];
			if ((xb1[j] > xb2[z]) || (xb2[j] < xb1[z])) continue;
			if (wallfront(j,z) != 0) continue;

			lx = Math.max(xb1[j],xb1[z]); rx = Math.min(xb2[j],xb2[z]);

			switch(smostwalltype[i])
			{
				case 0:
					if (lx <= rx)
					{
						if ((lx == xb1[z]) && (rx == xb2[z])) return;
						//clearbufbyte(&dwall[lx],(rx-lx+1)*sizeof(dwall[0]),0L);
						for (x=lx; x<=rx; x++) dwall[x] = 0;
					}
					break;
				case 1:
					k = smoststart[i] - xb1[j];
					for(x=lx;x<=rx;x++)
						if (smost[k+x] > uwall[x]) uwall[x] = smost[k+x];
					break;
				case 2:
					k = smoststart[i] - xb1[j];
					for(x=lx;x<=rx;x++)
						if (smost[k+x] < dwall[x]) dwall[x] = smost[k+x];
					break;
			}
		}

		//maskwall
		if ((globalorientation&128) == 0)
			maskwallscan(xb1[z],xb2[z],uwall,dwall,swall,lwall);
		else
		{
			if ((globalorientation&128) != 0)
			{
				if ((globalorientation&512) != 0) a.settransreverse(); else a.settransnormal();
			}
			transmaskwallscan(xb1[z],xb2[z]);
		}
	}
	
	
	private void transmaskwallscan(int x1, int x2)
	{
		engine.setgotpic(globalpicnum);
		if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0)) return;

		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);

		a.setuptvlineasm(globalshiftval);

		int x = x1;
		while ((startumost[x+windowx1] > startdmost[x+windowx1]) && (x <= x2)) x++;

		while (x <= x2) { transmaskvline(x); x++; }
		engine.faketimerhandler();
	}
	
	private void transmaskvline(int x)
	{
		if ((x < 0) || (x >= xdimen)) return;

		int y1v = Math.max(uwall[x],startumost[x+windowx1]-windowy1);
		int y2v = Math.min(dwall[x],startdmost[x+windowx1]-windowy1);
		y2v--;
		if (y2v < y1v) return;

		int vinc = swall[x]*globalyscale;
		int vplc = globalzd + vinc*(y1v-(int)globalhoriz+1);

		int i = lwall[x]+globalxpanning;
		if (i >= tilesizx[globalpicnum]) i %= tilesizx[globalpicnum];
		byte[] bufplc = waloff[globalpicnum];
		int bufoffs = i*tilesizy[globalpicnum];

		int p = ylookup[y1v]+x+frameoffset;

		a.tvlineasm1(vinc,globalpal, (engine.getpalookup(mulscale(swall[x],globvis,16),globalshade)<<8),y2v-y1v,vplc,bufplc,bufoffs,p);
	}
	
	private void prepwall(int z, WALL wal)
	{
		int l=0, ol=0;

		int walxrepeat = (wal.xrepeat<<3);

		//lwall calculation
		int i = (xb1[z]-halfxdimen);
		int topinc = -(ry1[z]>>2);
		int botinc = ((ry2[z]-ry1[z])>>8);
		int top = (mulscale(rx1[z],xdimen,5)+mulscale(topinc,i,2));
		int bot = (mulscale(rx1[z]-rx2[z],xdimen,11)+mulscale(botinc,i,2));

		int splc = mulscale(ry1[z],xdimscale,19);
		int sinc = mulscale(ry2[z]-ry1[z],xdimscale,16);
		
		int x = xb1[z];
		if (bot != 0)
		{
			l = (int) divscale(top,bot, 12);
			swall[x] = mulscale(l,sinc,21)+splc;
			l *= walxrepeat;
			lwall[x] = (l>>18);
		}
		while (x+4 <= xb2[z])
		{
			top += topinc; bot += botinc;
			if (bot != 0)
			{
				ol = l; l = (int) divscale(top,bot, 12);
				swall[x+4] = mulscale(l,sinc,21)+splc;
				l *= walxrepeat;
				lwall[x+4] = (l>>18);
			}
			i = ((ol+l)>>1);
			lwall[x+2] = (i>>18);
			lwall[x+1] = ((ol+i)>>19);
			lwall[x+3] = ((l+i)>>19);
			swall[x+2] = ((swall[x]+swall[x+4])>>1);
			swall[x+1] = ((swall[x]+swall[x+2])>>1);
			swall[x+3] = ((swall[x+4]+swall[x+2])>>1);
			x += 4;
		}
		if (x+2 <= xb2[z])
		{
			top += (topinc>>1); bot += (botinc>>1);
			if (bot != 0)
			{
				ol = l; l = (int) divscale(top,bot, 12);
				swall[x+2] = mulscale(l,sinc,21)+splc;
				l *= walxrepeat;
				lwall[x+2] = (l>>18);
			}
			lwall[x+1] = ((l+ol)>>19);
			swall[x+1] = ((swall[x]+swall[x+2])>>1);
			x += 2;
		}
		if (x+1 <= xb2[z])
		{
			bot += (botinc>>2);
			if (bot != 0)
			{
				l = (int) divscale(top+(topinc>>2),bot, 12);
				swall[x+1] = mulscale(l,sinc,21)+splc;
				lwall[x+1] = mulscale(l,walxrepeat,18);
			}
		}

		
		if (lwall[xb1[z]] < 0) lwall[xb1[z]] = 0;
		if ((lwall[xb2[z]] >= walxrepeat) && (walxrepeat != 0)) lwall[xb2[z]] = walxrepeat-1;
		if ((wal.cstat&8) != 0)
		{
			walxrepeat--;
			for(x=xb1[z];x<=xb2[z];x++) 
				lwall[x] = walxrepeat-lwall[x];
		}
	}
	
	private void maskwallscan(int x1, int x2, short[] uwal, short[] dwal, int[] swal, int[] lwal)
	{
		int x, startx;
		int y1ve, y2ve, tsizx, tsizy;

		tsizx = tilesizx[globalpicnum];
		tsizy = tilesizy[globalpicnum];
		engine.setgotpic(globalpicnum);
		if ((tsizx <= 0) || (tsizy <= 0)) return;
		if ((uwal[x1] > ydimen) && (uwal[x2] > ydimen)) return;
		if ((dwal[x1] < 0) && (dwal[x2] < 0)) return;

		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);

		startx = x1;

		boolean xnice = (pow2long[picsiz[globalpicnum]&15] == tsizx);
		if (xnice) tsizx = (tsizx-1);
		boolean ynice = (pow2long[picsiz[globalpicnum]>>4] == tsizy);
		if (ynice) tsizy = (picsiz[globalpicnum]>>4);

		a.setupmvlineasm(globalshiftval);

		for(x=startx;x<=x2;x++)
		{
			y1ve = Math.max(uwal[x],startumost[x+windowx1]-windowy1);
			y2ve = Math.min(dwal[x],startdmost[x+windowx1]-windowy1);
			if (y2ve <= y1ve) continue;

			int shade = (engine.getpalookup(mulscale(swal[x],globvis,16),globalshade)<<8);
			int bufplce = lwal[x] + globalxpanning;
			
			if (bufplce >= tsizx) { if (!xnice) bufplce %= tsizx; else bufplce &= tsizx; }
			if (!ynice) bufplce *= tsizy; else bufplce <<= tsizy;

			int vince = swal[x]*globalyscale;
			int vplce = globalzd + vince*(y1ve-(int)globalhoriz+1);

			a.mvlineasm1(vince,globalpal,shade,y2ve-y1ve-1,vplce,waloff[globalpicnum],bufplce,x+frameoffset+ylookup[y1ve]);
		}

		engine.faketimerhandler();
	}
	
	private void wallscan(int x1, int x2, short[] uwal, short[] dwal, int[] swal, int[] lwal)
	{
		int x, fpalookup = 0;
		boolean ynice;
		boolean xnice;
		int y1ve, y2ve;
		int tsizx, tsizy;

		tsizx = tilesizx[globalpicnum];
		tsizy = tilesizy[globalpicnum];
		engine.setgotpic(globalpicnum);
		if ((tsizx <= 0) || (tsizy <= 0)) return;
		if ((uwal[x1] > ydimen) && (uwal[x2] > ydimen)) return;
		if ((dwal[x1] < 0) && (dwal[x2] < 0)) return;

		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);

		xnice = (pow2long[picsiz[globalpicnum]&15] == tsizx);
		if (xnice) tsizx--;
		ynice = (pow2long[picsiz[globalpicnum]>>4] == tsizy);
		if (ynice) tsizy = (picsiz[globalpicnum]>>4);

		fpalookup = globalpal;

		a.setupvlineasm(globalshiftval);

		for(x=x1;x<=x2;x++)
		{
			y1ve = Math.max(uwal[x],umost[x]);
			y2ve = Math.min(dwal[x],dmost[x]);
			if (y2ve <= y1ve) continue;

			int shade = (engine.getpalookup(mulscale(swal[x],globvis,16),globalshade)<<8);

			int bufplce = lwal[x] + globalxpanning;
			if (bufplce >= tsizx) { if (!xnice) bufplce %= tsizx; else bufplce &= tsizx; }
			if (!ynice) bufplce *= tsizy; else bufplce <<= tsizy;

			int vince = swal[x]*globalyscale;
			int vplce = globalzd + vince*(y1ve-(int)globalhoriz+1);

			a.vlineasm1(vince,fpalookup,shade,y2ve-y1ve-1,vplce,waloff[globalpicnum],bufplce,x+frameoffset+ylookup[y1ve]);
		}
		
		engine.faketimerhandler();
	}
	
	private void florscan(int x1, int x2, int sectnum)
	{
		int i, j, ox, oy, x, y1, y2, twall, bwall;
		SECTOR sec;

		sec = sector[sectnum];
		if (sec.floorpal != globalpalwritten)
		{
			globalpalwritten = sec.floorpal;
			a.setpalookupaddress(globalpalwritten);
		}

		globalzd = globalposz-sec.floorz;
		if (globalzd > 0) return;
		globalpicnum = sec.floorpicnum;
		if (globalpicnum >= MAXTILES) globalpicnum = 0;
		engine.setgotpic(globalpicnum);
		if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0)) return;
		if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum, sectnum);

		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);
		globalbufplc = waloff[globalpicnum];

		globalshade = sec.floorshade;
		globvis = globalcisibility;
		if (sec.visibility != 0) globvis = mulscale(globvis,(sec.visibility+16),4);
		globalorientation = sec.floorstat;

		if ((globalorientation&64) == 0)
		{
			globalx1 = singlobalang; globalx2 = singlobalang;
			globaly1 = cosglobalang; globaly2 = cosglobalang;
			globalxpanning = (globalposx<<20);
			globalypanning = -(globalposy<<20);
		}
		else
		{
			j = sec.wallptr;
			ox = wall[wall[j].point2].x - wall[j].x;
			oy = wall[wall[j].point2].y - wall[j].y;
			i = engine.ksqrt(ox*ox+oy*oy); if (i == 0) i = 1024; else i = 1048576/i;
			globalx1 = mulscale(dmulscale(ox,singlobalang,-oy,cosglobalang,10),i,10);
			globaly1 = mulscale(dmulscale(ox,cosglobalang,oy,singlobalang,10),i,10);
			globalx2 = -globalx1;
			globaly2 = -globaly1;

			ox = ((wall[j].x-globalposx)<<6); oy = ((wall[j].y-globalposy)<<6);
			i = dmulscale(oy,cosglobalang,-ox,singlobalang,14);
			j = dmulscale(ox,cosglobalang,oy,singlobalang,14);
			ox = i; oy = j;
			globalxpanning = (globalx1*ox - globaly1*oy);
			globalypanning = (globaly2*ox + globalx2*oy);
		}
		globalx2 = mulscale(globalx2,viewingrangerecip,16);
		globaly1 = mulscale(globaly1,viewingrangerecip,16);
		globalxshift = (byte) (8-(picsiz[globalpicnum]&15));
		globalyshift = (byte) (8-(picsiz[globalpicnum]>>4));
		if ((globalorientation&8) != 0) { globalxshift++; globalyshift++; }

		if ((globalorientation&0x4) > 0)
		{
			i = globalxpanning; globalxpanning = globalypanning; globalypanning = i;
			i = globalx2; globalx2 = -globaly1; globaly1 = -i;
			i = globalx1; globalx1 = globaly2; globaly2 = i;
		}
		if ((globalorientation&0x10) > 0) { globalx1 = -globalx1; globaly1 = -globaly1; globalxpanning = -globalxpanning; }
		if ((globalorientation&0x20) > 0) { globalx2 = -globalx2; globaly2 = -globaly2; globalypanning = -globalypanning; }
		globalx1 <<= globalxshift; globaly1 <<= globalxshift;
		globalx2 <<= globalyshift;  globaly2 <<= globalyshift;
		globalxpanning <<= globalxshift; globalypanning <<= globalyshift;
		globalxpanning += ((sec.floorxpanning)<<24);
		globalypanning += ((sec.floorypanning)<<24);
		globaly1 = (-globalx1-globaly1)*halfxdimen;
		globalx2 = (globalx2-globaly2)*halfxdimen;

		a.sethlinesizes(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4,globalbufplc);

		globalx2 += globaly2*(x1-1);
		globaly1 += globalx1*(x1-1);
		globalx1 = mulscale(globalx1,globalzd,16);
		globalx2 = mulscale(globalx2,globalzd,16);
		globaly1 = mulscale(globaly1,globalzd,16);
		globaly2 = mulscale(globaly2,globalzd,16);
		globvis = (int) klabs(mulscale(globvis,globalzd,10));

		if ((globalorientation&0x180) == 0)
		{
			y1 = Math.max(dplc[x1],umost[x1]); y2 = y1;
			for(x=x1;x<=x2;x++)
			{
				twall = Math.max(dplc[x],umost[x])-1; bwall = dmost[x];
				if (twall < bwall-1)
				{
					if (twall >= y2)
					{
						while (y1 < y2-1) a.hline(x-1,++y1);
						y1 = twall;
					}
					else
					{
						while (y1 < twall) a.hline(x-1,++y1);
						while (y1 > twall) lastx[y1--] = x;
					}
					while (y2 > bwall) a.hline(x-1,--y2);
					while (y2 < bwall) lastx[y2++] = x;
				}
				else
				{
					while (y1 < y2-1) a.hline(x-1,++y1);
					if (x == x2) { globalx2 += globaly2; globaly1 += globalx1; break; }
					y1 = Math.max(dplc[x+1],umost[x+1]); y2 = y1;
				}
				globalx2 += globaly2; globaly1 += globalx1;
			}
			while (y1 < y2-1) a.hline(x2,++y1);
			engine.faketimerhandler();
			return;
		}

		switch(globalorientation&0x180)
		{
			case 128:
				a.msethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
			case 256:
				a.settransnormal();
				a.tsethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
			case 384:
				a.settransreverse();
				a.tsethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
		}

		y1 = Math.max(dplc[x1],umost[x1]); y2 = y1;
		for(x=x1;x<=x2;x++)
		{
			twall = Math.max(dplc[x],umost[x])-1; bwall = dmost[x];
			if (twall < bwall-1)
			{
				if (twall >= y2)
				{
					while (y1 < y2-1) slowhline(x-1,++y1);
					y1 = twall;
				}
				else
				{
					while (y1 < twall) slowhline(x-1,++y1);
					while (y1 > twall) lastx[y1--] = x;
				}
				while (y2 > bwall) slowhline(x-1,--y2);
				while (y2 < bwall) lastx[y2++] = x;
			}
			else
			{
				while (y1 < y2-1) slowhline(x-1,++y1);
				if (x == x2) { globalx2 += globaly2; globaly1 += globalx1; break; }
				y1 = Math.max(dplc[x+1],umost[x+1]); y2 = y1;
			}
			globalx2 += globaly2; globaly1 += globalx1;
		}
		while (y1 < y2-1) slowhline(x2,++y1);
		engine.faketimerhandler();
	}
	
	private void parascan(int dax1, int dax2, short sectnum, int dastat, int bunch)
	{
		SECTOR sec;
		int j, k, l, m, n, x, z, wallnum, nextsectnum, globalhorizbak;
		short[] topptr, botptr;

		sectnum = thesector[bunchfirst[bunch]]; sec = sector[sectnum];
		globalhorizbak = (int) globalhoriz;
		if (parallaxyscale != 65536)
			globalhoriz = mulscale((int)globalhoriz-(ydimen>>1),parallaxyscale,16) + (ydimen>>1);
		globvis = globalpisibility;

		if (sec.visibility != 0) globvis = mulscale(globvis,((sec.visibility+16)),4);

		if (dastat == 0)
		{
			globalpal = sec.ceilingpal;
			globalpicnum = sec.ceilingpicnum;
			globalshade = sec.ceilingshade;
			globalxpanning = sec.ceilingxpanning;
			globalypanning = sec.ceilingypanning;
			topptr = umost;
			botptr = uplc;
		}
		else
		{
			globalpal = sec.floorpal;
			globalpicnum = sec.floorpicnum;
			globalshade = sec.floorshade;
			globalxpanning = sec.floorxpanning;
			globalypanning = sec.floorypanning;
			topptr = dplc;
			botptr = dmost;
		}
		
		if (globalpicnum >= MAXTILES) globalpicnum = 0;
		if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum, sectnum);
		globalshiftval = (short) (picsiz[globalpicnum]>>4);
		if (pow2long[globalshiftval] != tilesizy[globalpicnum]) globalshiftval++;
		globalshiftval = (short) (32-globalshiftval);
		globalzd = (((tilesizy[globalpicnum]>>1)+parallaxyoffs)<<globalshiftval)+(globalypanning<<24);
		globalyscale = (8<<(globalshiftval-19));

		k = 11 - (picsiz[globalpicnum]&15) - pskybits;
		x = -1;

		for(z=bunchfirst[bunch];z>=0;z=p2[z])
		{
			wallnum = thewall[z]; nextsectnum = wall[wallnum].nextsector;

			j = 0;
			if(nextsectnum != -1) {
				if (dastat == 0) j = sector[nextsectnum].ceilingstat;
					else j = sector[nextsectnum].floorstat;
			}

			if ((nextsectnum < 0) || ((wall[wallnum].cstat&32) != 0) || ((j&1) == 0))
			{
				if (x == -1) x = xb1[z];
				if (parallaxtype == 0)
				{
					n = mulscale(xdimenrecip,viewingrange,16);
					for(j=xb1[z];j<=xb2[z];j++)
						lplc[j] = (((mulscale(j-halfxdimen,n, 23)+(int)globalang)&2047)>>k);
				}
				else
				{
					for(j=xb1[z];j<=xb2[z];j++)
						lplc[j] = (((radarang2[j]+(int)globalang)&2047)>>k);
				}
				
				if (parallaxtype == 2)
				{
					n = mulscale(xdimscale,viewingrange,16);
					for(j=xb1[z];j<=xb2[z];j++)
						swplc[j] = mulscale(sintable[(radarang2[j]+512)&2047],n,14);
				}
				else {
					Arrays.fill(swplc, xb1[z], xb2[z]+1, mulscale(xdimscale,viewingrange, 16));
				}
			}
			else if (x >= 0)
			{
				l = globalpicnum; m = (picsiz[globalpicnum]&15);
				globalpicnum = (short) (l+pskyoff[(lplc[x]>>m)]);

				if (((lplc[x]^lplc[(xb1[z]-1)])>>m) == 0)
					wallscan(x, xb1[z]-1,topptr,botptr,swplc,lplc);
				else
				{
					j = x;
					while (x < xb1[z])
					{
						n = l+pskyoff[(lplc[x]>>m)];
						if (n != globalpicnum)
						{
							wallscan(j,x-1,topptr,botptr,swplc,lplc);
							j = x;
							globalpicnum = (short) n;
						}
						x++;
					}
					if (j < x)
						wallscan(j,x-1,topptr,botptr,swplc,lplc);
				}

				globalpicnum = (short) l;
				x = -1;
			}
		}

		if (x >= 0)
		{
			l = globalpicnum; m = (picsiz[globalpicnum]&15);
			globalpicnum = (short) (l+pskyoff[(lplc[x]>>m)]);
			if (((lplc[x]^lplc[xb2[bunchlast[bunch]]])>>m) == 0) 
				wallscan(x,xb2[bunchlast[bunch]],topptr,botptr,swplc,lplc);
			else
			{
				j = x;
				while (x <= xb2[bunchlast[bunch]])
				{
					n = l+pskyoff[(lplc[x]>>m)];
					if (n != globalpicnum)
					{
						wallscan(j,x-1,topptr,botptr,swplc,lplc);
						j = x;
						globalpicnum = (short) n;
					}
					x++;
				}
				if (j <= x)
					wallscan(j,x,topptr,botptr,swplc,lplc);
			}
			globalpicnum = (short) l;
		}
		globalhoriz = globalhorizbak;
	}
	
	private void ceilspritescan(int x1, int x2)
	{
		int x, y1, y2, twall, bwall;

		y1 = uwall[x1]; y2 = y1;
		for(x=x1;x<=x2;x++)
		{
			twall = uwall[x]-1; bwall = dwall[x];
			if (twall < bwall-1)
			{
				if (twall >= y2)
				{
					while (y1 < y2-1) ceilspritehline(x-1,++y1);
					y1 = twall;
				}
				else
				{
					while (y1 < twall) ceilspritehline(x-1,++y1);
					while (y1 > twall) lastx[y1--] = x;
				}
				while (y2 > bwall) ceilspritehline(x-1,--y2);
				while (y2 < bwall) lastx[y2++] = x;
			}
			else
			{
				while (y1 < y2-1) ceilspritehline(x-1,++y1);
				if (x == x2) break;
				y1 = uwall[x+1]; y2 = y1;
			}
		}
		while (y1 < y2-1) ceilspritehline(x2,++y1);
		engine.faketimerhandler();
	}
	
	private void ceilspritehline(int x2, int y)
	{
		int x1, v, bx, by;

		//x = x1 + (x2-x1)t + (y1-y2)u  ³  x = 160v
		//y = y1 + (y2-y1)t + (x2-x1)u  ³  y = (scrx-160)v
		//z = z1 = z2                   ³  z = posz + (scry-horiz)v

		x1 = lastx[y]; if (x2 < x1) return;

		v = mulscale(globalzd,lookups[horizlookup+y-(int)globalhoriz+horizycent],20);
		bx = mulscale(globalx2*x1+globalx1,v,14) + globalxpanning;
		by = mulscale(globaly2*x1+globaly1,v,14) + globalypanning;
		a.asm1 = mulscale(globalx2,v,14);
		a.asm2 = mulscale(globaly2,v,14);

		a.hlinepal = globalpal;
		a.hlineshade = engine.getpalookup(mulscale(klabs(v),globvis,28),globalshade)<<8;

		if ((globalorientation&2) == 0)
			a.mhline(globalbufplc,bx,(x2-x1)<<16,0,by,ylookup[y]+x1+frameoffset);
		else
		{
			a.thline(globalbufplc,bx,(x2-x1)<<16,0,by,ylookup[y]+x1+frameoffset);
		}
	}
	
	private void grouscan(int dax1, int dax2, short sectnum, int dastat)
	{
		int i, j, l, x, y, dx, dy, wx, wy, y1, y2, daz;
		int daslope, dasqr;
		int shoffs, shinc, m1, m2;
		int mptr1, mptr2, nptr1, nptr2;
		WALL wal;
		SECTOR sec = sector[sectnum];

		if (dastat == 0)
		{
			if (globalposz <= engine.getceilzofslope(sectnum,globalposx,globalposy))
				return;  //Back-face culling
			globalorientation = sec.ceilingstat;
			globalpicnum = sec.ceilingpicnum;
			globalshade = sec.ceilingshade;
			globalpal = sec.ceilingpal;
			daslope = sec.ceilingheinum;
			daz = sec.ceilingz;
		}
		else
		{
			if (globalposz >= engine.getflorzofslope(sectnum,globalposx,globalposy))
				return;  //Back-face culling
			globalorientation = sec.floorstat;
			globalpicnum = sec.floorpicnum;
			globalshade = sec.floorshade;
			globalpal = sec.floorpal;
			daslope = sec.floorheinum;
			daz = sec.floorz;
		}

		if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum, sectnum);
		engine.setgotpic(globalpicnum);
		if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0)) return;
		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);

		wal = wall[sec.wallptr];
		wx = wall[wal.point2].x - wal.x;
		wy = wall[wal.point2].y - wal.y;
		dasqr = krecipasm(engine.ksqrt(wx*wx+wy*wy));
		i = mulscale(daslope,dasqr,21);
		wx *= i; wy *= i;

		globalx = -mulscale(singlobalang,xdimenrecip,19);
		globaly = mulscale(cosglobalang,xdimenrecip,19);
		globalx1 = (globalposx<<8);
		globaly1 = -(globalposy<<8);
		i = (dax1-halfxdimen)*xdimenrecip;
		globalx2 = mulscale(cosglobalang<<4,viewingrangerecip,16) - mulscale(singlobalang,i,27);
		globaly2 = mulscale(singlobalang<<4,viewingrangerecip,16) + mulscale(cosglobalang,i,27);
		globalzd = (xdimscale<<9);
		globalzx = -dmulscale(wx,globaly2,-wy,globalx2,17) + mulscale(1-(int)globalhoriz,globalzd,10);
		globalz = -dmulscale(wx,globaly,-wy,globalx,25);

		if ((globalorientation&64) != 0)  //Relative alignment
		{
			dx = mulscale(wall[wal.point2].x-wal.x,dasqr,14);
			dy = mulscale(wall[wal.point2].y-wal.y,dasqr,14);

			i = engine.ksqrt(daslope*daslope+16777216);

			x = globalx; y = globaly;
			globalx = dmulscale(x,dx,y,dy,16);
			globaly = mulscale(dmulscale(-y,dx,x,dy,16),i,12);

			x = ((wal.x-globalposx)<<8); y = ((wal.y-globalposy)<<8);
			globalx1 = dmulscale(-x,dx,-y,dy,16);
			globaly1 = mulscale(dmulscale(-y,dx,x,dy,16),i,12);

			globalx2 = dmulscale(globalx2,dx,globaly2,dy,16);
			globaly2 = mulscale(dmulscale(-globaly2,dx,globalx2,dy,16),i,12);
		}
		if ((globalorientation&0x4) != 0)
		{
			i = globalx; globalx = -globaly; globaly = -i;
			i = globalx1; globalx1 = globaly1; globaly1 = i;
			i = globalx2; globalx2 = -globaly2; globaly2 = -i;
		}
		if ((globalorientation&0x10) != 0) { globalx1 = -globalx1; globalx2 = -globalx2; globalx = -globalx; }
		if ((globalorientation&0x20) != 0) { globaly1 = -globaly1; globaly2 = -globaly2; globaly = -globaly; }

		daz = dmulscale(wx,globalposy-wal.y,-wy,globalposx-wal.x,9) + ((daz-globalposz)<<8);
		globalx2 = mulscale(globalx2,daz,20); globalx = mulscale(globalx,daz,28);
		globaly2 = mulscale(globaly2,-daz,20); globaly = mulscale(globaly,-daz,28);

		i = 8-(picsiz[globalpicnum]&15); j = 8-(picsiz[globalpicnum]>>4);
		if ((globalorientation&8) != 0) { i++; j++; }
		globalx1 <<= (i+12); globalx2 <<= i; globalx <<= i;
		globaly1 <<= (j+12); globaly2 <<= j; globaly <<= j;

		if (dastat == 0)
		{
			globalx1 += ((sec.ceilingxpanning)<<24);
			globaly1 += ((sec.ceilingypanning)<<24);
		}
		else
		{
			globalx1 += ((sec.floorxpanning)<<24);
			globaly1 += ((sec.floorypanning)<<24);
		}

		a.asm1 = -(globalzd>>(16-BITSOFPRECISION));

		globvis = globalvisibility;
		if (sec.visibility != 0) globvis = mulscale(globvis,((sec.visibility+16)),4);
		globvis = mulscale(globvis,daz,13);
		globvis = mulscale(globvis,xdimscale,16);
		
		j = globalpal; 
		a.setupslopevlin((picsiz[globalpicnum]&15)+(((picsiz[globalpicnum]>>4))<<8),waloff[globalpicnum],-ylookup[1]);

		l = (globalzd>>16);

		shinc = mulscale(globalz,xdimenscale,16);
		if (shinc > 0) shoffs = (4<<15); else shoffs = ((16380-ydimen)<<15);	// JBF: was 2044
		if (dastat == 0) y1 = umost[dax1]; else y1 = Math.max(umost[dax1],dplc[dax1]);
		m1 = mulscale(y1,globalzd,16) + (globalzx>>6);
		//Avoid visibility overflow by crossing horizon
		if (globalzd > 0) m1 += (globalzd>>16); else m1 -= (globalzd>>16);
		m2 = m1+l;
		mptr1 = y1+(shoffs>>15); 
		mptr2 = y1+(shoffs>>15)+1;

		for(x=dax1;x<=dax2;x++)
		{
			if (dastat == 0) { y1 = umost[x]; y2 = Math.min(dmost[x],uplc[x])-1; }
			else { y1 = Math.max(umost[x],dplc[x]); y2 = dmost[x]-1; }
			if (y1 <= y2)
			{
				nptr1 = y1+(shoffs>>15);
				nptr2 = y2+(shoffs>>15);
				while (nptr1 <= mptr1)
				{
					slopalookup[mptr1--] = palookup[j][engine.getpalookup(mulscale(krecipasm(m1),globvis,24),globalshade)<<8];
					m1 -= l;
				}
				while (nptr2 >= mptr2)
				{
					slopalookup[mptr2++] = palookup[j][engine.getpalookup(mulscale(krecipasm(m2),globvis,24),globalshade)<<8];
					m2 += l;
				}

				globalx3 = (int) (globalx2>>10);
				globaly3 = (int) (globaly2>>10);
				a.asm3 = mulscale(y2,globalzd,16) + (globalzx>>6);
				a.slopevlin(ylookup[y2]+x+frameoffset,krecipasm(a.asm3>>3),nptr2,y2-y1+1,globalx1,globaly1);

				if ((x&15) == 0) engine.faketimerhandler();
			}
			globalx2 += globalx;
			globaly2 += globaly;
			globalzx += globalz;
			shoffs += shinc;
		}
	}
	
	private void ceilscan(int x1, int x2, int sectnum)
	{
		int i, j, ox, oy, x, y1, y2, twall, bwall;
		SECTOR sec;

		sec = sector[sectnum];
		if (sec.ceilingpal != globalpalwritten)
		{
			globalpalwritten = sec.ceilingpal;
			a.setpalookupaddress(globalpalwritten);
		}

		globalzd = sec.ceilingz-globalposz;
		if (globalzd > 0) return;
		globalpicnum = sec.ceilingpicnum;
		if (globalpicnum >= MAXTILES) globalpicnum = 0;
		engine.setgotpic(globalpicnum);
		if ((tilesizx[globalpicnum] <= 0) || (tilesizy[globalpicnum] <= 0)) return;
		if ((picanm[globalpicnum]&192) != 0) globalpicnum += engine.animateoffs(globalpicnum, sectnum);

		if (waloff[globalpicnum] == null) engine.loadtile(globalpicnum);
		globalbufplc = waloff[globalpicnum];

		globalshade = sec.ceilingshade;
		globvis = globalcisibility;
		if (sec.visibility != 0) globvis = mulscale(globvis,(sec.visibility+16), 4);
		globalorientation = sec.ceilingstat;


		if ((globalorientation&64) == 0)
		{
			globalx1 = singlobalang; globalx2 = singlobalang;
			globaly1 = cosglobalang; globaly2 = cosglobalang;
			globalxpanning = (globalposx<<20);
			globalypanning = -(globalposy<<20);
		}
		else
		{
			j = sec.wallptr;
			ox = wall[wall[j].point2].x - wall[j].x;
			oy = wall[wall[j].point2].y - wall[j].y;
			i = engine.ksqrt(ox*ox+oy*oy); if (i == 0) i = 1024; else i = 1048576/i;
			globalx1 = mulscale(dmulscale(ox,singlobalang,-oy,cosglobalang,10),i,10);
			globaly1 = mulscale(dmulscale(ox,cosglobalang,oy,singlobalang,10),i,10);
			globalx2 = -globalx1;
			globaly2 = -globaly1;

			ox = ((wall[j].x-globalposx)<<6); oy = ((wall[j].y-globalposy)<<6);
			i = dmulscale(oy,cosglobalang,-ox,singlobalang,14);
			j = dmulscale(ox,cosglobalang,oy,singlobalang,14);
			ox = i; oy = j;
			globalxpanning = (globalx1*ox - globaly1*oy);
			globalypanning = (globaly2*ox + globalx2*oy);
		}
		globalx2 = mulscale(globalx2,viewingrangerecip,16);
		globaly1 = mulscale(globaly1,viewingrangerecip,16);
		globalxshift = (byte) (8-(picsiz[globalpicnum]&15));
		globalyshift = (byte) (8-(picsiz[globalpicnum]>>4));
		if ((globalorientation&8) != 0) { globalxshift++; globalyshift++; }

		if ((globalorientation&0x4) > 0)
		{
			i = globalxpanning; globalxpanning = globalypanning; globalypanning = i;
			i = globalx2; globalx2 = -globaly1; globaly1 = -i;
			i = globalx1; globalx1 = globaly2; globaly2 = i;
		}
		if ((globalorientation&0x10) > 0) { globalx1 = -globalx1; globaly1 = -globaly1; globalxpanning = -globalxpanning; }
		if ((globalorientation&0x20) > 0) { globalx2 = -globalx2; globaly2 = -globaly2; globalypanning = -globalypanning; }
		globalx1 <<= globalxshift; globaly1 <<= globalxshift;
		globalx2 <<= globalyshift;  globaly2 <<= globalyshift;
		globalxpanning <<= globalxshift; globalypanning <<= globalyshift;
		globalxpanning += ((sec.ceilingxpanning)<<24);
		globalypanning += ((sec.ceilingypanning)<<24);
		globaly1 = (-globalx1-globaly1)*halfxdimen;
		globalx2 = (globalx2-globaly2)*halfxdimen;

		a.sethlinesizes(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4,globalbufplc);

		globalx2 += globaly2*(x1-1);
		globaly1 += globalx1*(x1-1);
		globalx1 = mulscale(globalx1,globalzd,16);
		globalx2 = mulscale(globalx2,globalzd,16);
		globaly1 = mulscale(globaly1,globalzd,16);
		globaly2 = mulscale(globaly2,globalzd,16);
		globvis = (int) klabs(mulscale(globvis,globalzd,10));

		if ((globalorientation&0x180) == 0)
		{
			y1 = umost[x1]; y2 = y1;
			for(x=x1;x<=x2;x++)
			{
				twall = umost[x]-1; bwall = Math.min(uplc[x],dmost[x]);
				if (twall < bwall-1)
				{
					if (twall >= y2)
					{
						while (y1 < y2-1) a.hline(x-1,++y1);
						y1 = twall;
					}
					else
					{
						while (y1 < twall) a.hline(x-1,++y1);
						while (y1 > twall) lastx[y1--] = x;
					}
					while (y2 > bwall) a.hline(x-1,--y2);
					while (y2 < bwall) lastx[y2++] = x;
				}
				else
				{
					while (y1 < y2-1) a.hline(x-1,++y1);
					if (x == x2) { globalx2 += globaly2; globaly1 += globalx1; break; }
					y1 = umost[x+1]; y2 = y1;
				}
				globalx2 += globaly2; globaly1 += globalx1;
			}
			while (y1 < y2-1) a.hline(x2,++y1);
			engine.faketimerhandler();
			return;
		}

		int forswitch = globalorientation&0x180;
		switch(forswitch)
		{
			case 128:
				a.msethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
			case 256:
				a.settransnormal();
				a.tsethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
			case 384:
				a.settransreverse();
				a.tsethlineshift(picsiz[globalpicnum]&15,picsiz[globalpicnum]>>4);
				break;
		}

		y1 = umost[x1]; y2 = y1;
		for(x=x1;x<=x2;x++)
		{
			twall = umost[x]-1; bwall = Math.min(uplc[x],dmost[x]);
			if (twall < bwall-1)
			{
				if (twall >= y2)
				{
					while (y1 < y2-1) slowhline(x-1,++y1);
					y1 = twall;
				}
				else
				{
					while (y1 < twall) slowhline(x-1,++y1);
					while (y1 > twall) lastx[y1--] = x;
				}
				while (y2 > bwall) slowhline(x-1,--y2);
				while (y2 < bwall) lastx[y2++] = x;
			}
			else
			{
				while (y1 < y2-1) slowhline(x-1,++y1);
				if (x == x2) { globalx2 += globaly2; globaly1 += globalx1; break; }
				y1 = umost[x+1]; y2 = y1;
			}
			globalx2 += globaly2; globaly1 += globalx1;
		}
		while (y1 < y2-1) slowhline(x2,++y1);
		engine.faketimerhandler();
	}

	@Override
	public void clearview(int dacol) {
		if(frameplace != null)
			Arrays.fill(frameplace, (byte) dacol);
	}

	@Override
	public void nextpage() {
		System.arraycopy(frameplace, 0, getCanvas().getFrame(), 0, frameplace.length);
		getCanvas().update();
	}

	@Override
	public void rotatesprite(int sx, int sy, int z, int a, int picnum, int dashade, int dapalnum, int dastat, int cx1,
			int cy1, int cx2, int cy2) {
		
		int i;
		PermFifo per, per2;
		
		if (picnum >= MAXTILES)
		  return;
		
		if ((cx1 > cx2) || (cy1 > cy2)) return;
		if (z <= 16) return;
		if ((picanm[picnum]&192) != 0) picnum += engine.animateoffs((short) picnum, 0xc000);
		if ((tilesizx[picnum] <= 0) || (tilesizy[picnum] <= 0)) return;
		
		// Experimental / development bits. ONLY FOR INTERNAL USE!
		//  bit RS_CENTERORIGIN: see dorotspr_handle_bit2
		////////////////////
		
		if (((dastat&128) == 0) || (numpages < 2) || (beforedrawrooms != 0))
		{
			dorotatesprite(sx,sy,z,a,picnum,dashade,dapalnum,dastat,cx1,cy1,cx2,cy2,guniqhudid);
		}
		
		if (((dastat&64) != 0) && (cx1 <= 0) && (cy1 <= 0) && (cx2 >= xdim-1) && (cy2 >= ydim-1) &&
		      (sx == (160<<16)) && (sy == (100<<16)) && (z == 65536L) && (a == 0) && ((dastat&1) == 0))
		  permhead = permtail = 0;
		
		if ((dastat&128) == 0) return;
		if (numpages >= 2)
		{
		  per = permfifo[permhead];
		  if(per == null)
		  	per = new PermFifo();
		  per.sx = sx; per.sy = sy; per.z = z; per.a = (short) a;
		  per.picnum = (short) picnum;
		  per.dashade = (short) dashade; per.dapalnum = (short) dapalnum;
		  per.dastat = (short) dastat;
		  per.pagesleft = (short) (numpages+((beforedrawrooms&1)<<7));
		  per.cx1 = cx1; per.cy1 = cy1; per.cx2 = cx2; per.cy2 = cy2;
		  per.uniqid = guniqhudid;   //JF extension
		
		  //Would be better to optimize out true bounding boxes
		  if ((dastat&64) != 0)  //If non-masking write, checking for overlapping cases
		  {
		      for (i=permtail; i!=permhead; i=((i+1)&(MAXPERMS-1)))
		      {
		          per2 = permfifo[i];
		          if(per2 == null)
		      	  	per2 = new PermFifo();
		          if ((per2.pagesleft&127) == 0) continue;
		          if (per2.sx != per.sx) continue;
		          if (per2.sy != per.sy) continue;
		          if (per2.z != per.z) continue;
		          if (per2.a != per.a) continue;
		          if (tilesizx[per2.picnum] > tilesizx[per.picnum]) continue;
		          if (tilesizy[per2.picnum] > tilesizy[per.picnum]) continue;
		          if (per2.cx1 < per.cx1) continue;
		          if (per2.cy1 < per.cy1) continue;
		          if (per2.cx2 > per.cx2) continue;
		          if (per2.cy2 > per.cy2) continue;
		          per2.pagesleft = 0;
		      }
		      if ((per.z == 65536) && (per.a == 0))
		          for (i=permtail; i!=permhead; i=((i+1)&(MAXPERMS-1)))
		          {
		              per2 = permfifo[i];
		              if(per2 == null)
		  	      	  	per2 = new PermFifo();
		              if ((per2.pagesleft&127) == 0) continue;
		              if (per2.z != 65536) continue;
		              if (per2.a != 0) continue;
		              if (per2.cx1 < per.cx1) continue;
		              if (per2.cy1 < per.cy1) continue;
		              if (per2.cx2 > per.cx2) continue;
		              if (per2.cy2 > per.cy2) continue;
		              if ((per2.sx>>16) < (per.sx>>16)) continue;
		              if ((per2.sy>>16) < (per.sy>>16)) continue;
		              if ((per2.sx>>16)+tilesizx[per2.picnum] > (per.sx>>16)+tilesizx[per.picnum]) continue;
		              if ((per2.sy>>16)+tilesizy[per2.picnum] > (per.sy>>16)+tilesizy[per.picnum]) continue;
		              per2.pagesleft = 0;
		          }
		  }
		
		  permhead = ((permhead+1)&(MAXPERMS-1));
		}
	}
	
	private void dorotatesprite(int sx, int sy, int z, int ang, int picnum, int dashade, int dapalnum, int dastat, int cx1, int cy1, int cx2, int cy2, int uniqid)
	{
		int xoff = 0, yoff = 0;
		int x, y;
		
		if (cx1 < 0) cx1 = 0;
		if (cy1 < 0) cy1 = 0;
		if (cx2 > xdim-1) cx2 = xdim-1;
		if (cy2 > ydim-1) cy2 = ydim-1;

		int xsiz = tilesizx[picnum]; 
		int ysiz = tilesizy[picnum];
		if ((dastat&16) != 0) { xoff = 0; yoff = 0; }
		else
		{
			xoff = (int) ((byte) ((picanm[picnum] >> 8) & 255)) + (xsiz >> 1);
			yoff = (int) ((byte) ((picanm[picnum] >> 16) & 255)) + (ysiz >> 1);
		}
		
		if ((dastat&4) != 0) yoff = ysiz-yoff;

		int cosang = sintable[(ang+512)&2047]; 
		int sinang = sintable[ang&2047];
		
		if ((dastat&2) != 0)  //Auto window size scaling
		{
			if ((dastat&8) == 0)
			{
				x = xdimenscale;   //= scale(xdimen,yxaspect,320);
				sx = ((cx1+cx2+2)<<15)+scale(sx-(320<<15),xdimen,320);
				sy = ((cy1+cy2+2)<<15)+mulscale(sy-(200<<15),x,16);
			}
			else
			{
				//If not clipping to startmosts, & auto-scaling on, as a
				//hard-coded bonus, scale to full screen instead
				x = scale(xdim,yxaspect,320);
				sx = (xdim<<15)+32768+scale(sx-(320<<15),xdim,320);
				sy = (ydim<<15)+32768+mulscale(sy-(200<<15),x, 16);
			}
			z = mulscale(z,x, 16);
		}
		
		int xv = mulscale(cosang,z, 14), xv2;
		int yv = mulscale(sinang,z, 14), yv2;

		if (((dastat&2) != 0) || ((dastat&8) == 0)) //Don't aspect unscaled perms
		{
			xv2 = mulscale(xv,xyaspect, 16);
			yv2 = mulscale(yv,xyaspect, 16);
		}
		else
		{
			xv2 = xv;
			yv2 = yv;
		}
		
		nry1[0] = sy - (yv*xoff + xv*yoff);
		nry1[1] = nry1[0] + yv*xsiz;
		nry1[3] = nry1[0] + xv*ysiz;
		nry1[2] = nry1[1]+nry1[3]-nry1[0];
		int i = (cy1<<16); if ((nry1[0]<i) && (nry1[1]<i) && (nry1[2]<i) && (nry1[3]<i)) return;
		i = (cy2<<16); if ((nry1[0]>i) && (nry1[1]>i) && (nry1[2]>i) && (nry1[3]>i)) return;

		nrx1[0] = sx - (xv2*xoff - yv2*yoff);
		nrx1[1] = nrx1[0] + xv2*xsiz;
		nrx1[3] = nrx1[0] - yv2*ysiz;
		nrx1[2] = nrx1[1]+nrx1[3]-nrx1[0];
		i = (cx1<<16); if ((nrx1[0]<i) && (nrx1[1]<i) && (nrx1[2]<i) && (nrx1[3]<i)) return;
		i = (cx2<<16); if ((nrx1[0]>i) && (nrx1[1]>i) && (nrx1[2]>i) && (nrx1[3]>i)) return;

		int gx1 = nrx1[0]; 
		int gy1 = nry1[0];   //back up these before clipping
		
		int npoints;
		if ((npoints = clippoly4(cx1<<16,cy1<<16,(cx2+1)<<16,(cy2+1)<<16)) < 3) return;

		int lx = nrx1[0]; 
		int rx = nrx1[0];
		
		int nextv = 0;
		for(int v=npoints-1;v>=0;v--)
		{
			int x1 = nrx1[v]; 
			int x2 = nrx1[nextv];
			int dax1 = (x1>>16);
			if (x1 < lx) lx = x1;
			int dax2 = (x2>>16); 
			if (x1 > rx) rx = x1;
			if (dax1 != dax2)
			{
				int y1 = nry1[v]; 
				int y2 = nry1[nextv];
				long yinc = divscale(y2-y1,x2-x1, 16);

				if (dax2 > dax1)
				{
					int yplc = y1 + mulscale((dax1<<16)+65535-x1,yinc, 16);
					qinterpolatedown16short(uplc,dax1,dax2-dax1,yplc,yinc);
				}
				else
				{
					int yplc = y2 + mulscale((dax2<<16)+65535-x2,yinc, 16);
					qinterpolatedown16short(dplc,dax2,dax1-dax2,yplc,yinc);
				}
			}
			nextv = v;
		}

		if (waloff[picnum] == null) engine.loadtile(picnum);
		engine.setgotpic(picnum);
		byte[] bufplc = waloff[picnum];

		int palookupshade = engine.getpalookup(0,dashade)<<8;

		i = (int) divscale(1,z, 32);
		xv = mulscale(sinang,i, 14);
		yv = mulscale(cosang,i, 14);
		if (((dastat&2) != 0) || ((dastat&8) == 0)) //Don't aspect unscaled perms
		{
			yv2 = mulscale(-xv,yxaspect, 16);
			xv2 = mulscale(yv,yxaspect, 16);
		}
		else
		{
			yv2 = -xv;
			xv2 = yv;
		}

		int x1 = (lx>>16); 
		int x2 = (rx>>16);

		int oy = 0;
		x = (x1<<16)-1-gx1; y = (oy<<16)+65535-gy1;
		int bx = dmulscale(x,xv2,y,xv, 16);
		int by = dmulscale(x,yv2,y,yv, 16);
		
		if ((dastat&4) != 0) { yv = -yv; yv2 = -yv2; by = (ysiz<<16)-1-by; }
		
		if ((dastat&1) == 0)
		{
			if ((dastat&64) != 0)
				a.setupspritevline(dapalnum,palookupshade,xv,yv,ysiz);
			else
				a.msetupspritevline(dapalnum,palookupshade,xv,yv,ysiz);
		}
		else
		{
			a.tsetupspritevline(dapalnum,palookupshade,xv,yv,ysiz);
			if ((dastat&32) != 0) a.settransreverse(); else a.settransnormal();
		}

		for(x=x1;x<x2;x++)
		{
			bx += xv2; by += yv2;
			int y1 = uplc[x]; int y2 = dplc[x];
			if ((dastat&8) == 0)
			{
				if (startumost[x] > y1) y1 = startumost[x];
				if (startdmost[x] < y2) y2 = startdmost[x];
			}
			if (y2 <= y1) continue;

			switch(y1-oy)
			{
				case -1: bx -= xv; by -= yv; oy = y1; break;
				case 0: break;
				case 1: bx += xv; by += yv; oy = y1; break;
				default: bx += xv*(y1-oy); by += yv*(y1-oy); oy = y1; break;
			}

			int p = ylookup[y1]+x;

			if ((dastat&1) == 0)
			{
				if ((dastat&64) != 0) 
					a.spritevline(bx&65535,by&65535,y2-y1+1,bufplc,(bx>>16)*ysiz+(by>>16),p);
				else {
					a.mspritevline(bx&65535,by&65535,y2-y1+1,bufplc,(bx>>16)*ysiz+(by>>16),p);
				}
			}
			else
			{
				a.tspritevline(bx&65535,by&65535,y2-y1+1,bufplc,(bx>>16)*ysiz+(by>>16),p);
			}
			engine.faketimerhandler();
		}
	}
	
	private int clippoly4(int cx1, int cy1, int cx2, int cy2)
	{
		int n, nn, z, zz, x, x1, x2, y, y1, y2, t;

		nn = 0; z = 0;
		do
		{
			zz = ((z+1)&3);
			x1 = nrx1[z]; x2 = nrx1[zz]-x1;

			if ((cx1 <= x1) && (x1 <= cx2)) {
				nrx2[nn] = x1; nry2[nn] = nry1[z]; nn++;
			}

			if (x2 <= 0) x = cx2; else x = cx1;
			t = x-x1;
			if (((t-x2)^t) < 0) {
				nrx2[nn] = x; nry2[nn] = nry1[z]+scale(t,nry1[zz]-nry1[z],x2); nn++;
			}

			if (x2 <= 0) x = cx1; else x = cx2;
			t = x-x1;
			if (((t-x2)^t) < 0) {
				nrx2[nn] = x; nry2[nn] = nry1[z]+scale(t,nry1[zz]-nry1[z],x2); nn++;
			}

			z = zz;
		} while (z != 0);
		if (nn < 3) return(0);

		n = 0; z = 0;
		do
		{
			zz = z+1; if (zz == nn) zz = 0;
			y1 = nry2[z]; y2 = nry2[zz]-y1;

			if ((cy1 <= y1) && (y1 <= cy2)) {
				nry1[n] = y1; nrx1[n] = nrx2[z]; n++;
			}

			if (y2 <= 0) y = cy2; else y = cy1;
			t = y-y1;
			if (((t-y2)^t) < 0) {
				nry1[n] = y; nrx1[n] = nrx2[z]+scale(t,nrx2[zz]-nrx2[z],y2); n++;
			}

			if (y2 <= 0) y = cy1; else y = cy2;
			t = y-y1;
			if (((t-y2)^t) < 0) {
				nry1[n] = y; nrx1[n] = nrx2[z]+scale(t,nrx2[zz]-nrx2[z],y2); n++;
			}

			z = zz;
		} while (z != 0);
		return(n);
	}

	private void qinterpolatedown16short(short[] bufptr, int offset, int num, long val, long add)
	{ // ...maybe the same person who provided this too?
	    for(int i=0;i<num;i++) { bufptr[i + offset] = (short)(val>>16); val += add; }
	}
	
	void qinterpolatedown16(int[] bufptr, int offset, int num, long val, long add)
	{ // gee, I wonder who could have provided this...
	    for(int i=0;i<num;i++) { bufptr[i + offset] = (int) (val>>16); val += add; }
	}


	@Override
	public String getname() {
		
		return "Classic";
	}

	@Override
	public void drawoverheadmap(int cposx, int cposy, int czoom, short cang) {
		
	}

	@Override
	public void drawmapview(int dax, int day, int zoome, int ang) {
		
	}

	@Override
	public int printext(int xpos, int ypos, int col, int backcol, char[] text, int fontsize) {
		int stx = xpos;
		int charxsiz = 8;
		char[] fontptr = textfont;
		if (fontsize != 0) { fontptr = smalltextfont; charxsiz = 4; }

		for(int i=0; text[i] != 0;i++)
		{
			int ptr = bytesperline*(ypos+7)+(stx-fontsize);
			
			for(int y=7;y>=0;y--)
			{
				for(int x=charxsiz-1;x>=0;x--)
				{
					if ((fontptr[y + (text[i]<<3)]&pow2char[7-fontsize-x]) != 0) {
						frameplace[ptr + x] = (byte) col;
					}
					else if (backcol >= 0) {
						frameplace[ptr + x] = (byte) backcol;
					}
				}
				ptr -= bytesperline;
			}
			stx += charxsiz;
		}
		
		return 0;
	}

	@Override
	public int printchar(int xpos, int ypos, int col, int backcol, char ch, int fontsize) {
		
		return 0;
	}

	@Override
	public ByteBuffer getframebuffer(int x, int y, int w, int h, int format) {
		
		return null;
	}

	@Override
	public void drawline256(int x1, int y1, int x2, int y2, int col) {
		
	}
	
	
	

	public void dosetaspect()
	{
		int i, j, k, x, xinc;

		if (xyaspect != oxyaspect)
		{
			oxyaspect = xyaspect;
			j = xyaspect*320;
			lookups[horizlookup2+horizycent-1] = (int) divscale(131072,j,26);
			for(i=ydim*4-1;i>=0;i--)
				if (i != (horizycent-1))
				{
					lookups[horizlookup+i] = (int) divscale(1,i-(horizycent-1),28);
					lookups[horizlookup2+i] = (int) divscale(klabs(lookups[horizlookup+i]),j,14);
				}
		}
		if ((xdimen != oxdimen) || (viewingrange != oviewingrange))
		{
			oxdimen = xdimen;
			oviewingrange = viewingrange;
			xinc = mulscale(viewingrange*320,xdimenrecip,32);
			x = (640<<16)-mulscale(xinc,xdimen,1);
			for(i=0;i<xdimen;i++)
			{
				j = (x&65535); k = (x>>16); x += xinc;
				if (j != 0) j = mulscale(radarang[k+1]-radarang[k],j,16);
				radarang2[i] = (short)((radarang[k]+j)>>6);
			}

			for(i=1;i<65536;i++) distrecip[i] = (int) divscale(xdimen,i,20);
//			nytooclose = xdimen*2100;
//			nytoofar = 65536*16384-1048576; XXX
		}
	}

	public void settiltang(int tilt) {}
	public void setdrunk(float intensive) {}
	public float getdrunk() {return 0;}
	public void palfade(HashMap<String, FadeEffect> fades) {}
	public void preload() {}
	public void precache(int dapicnum, int dapalnum, int datype) {}
	public void gltexapplyprops() {}
	public void gltexinvalidateall(int flags) { getCanvas().changepalette(curpalette); }
	public void gltexinvalidate(int dapicnum, int dapalnum, int dameth) {}
	
	private void scansector(short sectnum)
	{
		WALL wal, wal2;
		SPRITE spr; 
		int xs, ys, x1, y1, x2, y2, xp1, yp1, xp2=0, yp2=0, templong;
		int z, zz, startwall, endwall, numscansbefore, scanfirst, bunchfrst;
		short nextsectnum;

		if (sectnum < 0) return;

		if (automapping != 0) 
			show2dsector[sectnum>>3] |= pow2char[sectnum&7];

		sectorborder[0] = sectnum;
		sectorbordercnt = 1;
		do
		{
			sectnum = sectorborder[--sectorbordercnt];

			for(z=headspritesect[sectnum];z>=0;z=nextspritesect[z])
			{
				spr = sprite[z];
				if ((((spr.cstat&0x8000) == 0) || (showinvisibility)) &&
					  (spr.xrepeat > 0) && (spr.yrepeat > 0) &&
					  (spritesortcnt < MAXSPRITESONSCREEN))
				{
					xs = spr.x-globalposx; 
					ys = spr.y-globalposy;
					if (((spr.cstat&48) != 0) || (xs*cosglobalang+ys*singlobalang > 0))
					{
						if (tsprite[spritesortcnt] == null)
							tsprite[spritesortcnt] = new SPRITE();
						tsprite[spritesortcnt].set(sprite[z]);

						tsprite[spritesortcnt++].owner = (short) z;
					}
				}
			}

			gotsector[sectnum>>3] |= pow2char[sectnum&7];

			bunchfrst = numbunches;
			numscansbefore = numscans;

			if(sector[sectnum] == null) continue;
			
			startwall = sector[sectnum].wallptr;
			endwall = startwall + sector[sectnum].wallnum;
			scanfirst = numscans;
			
			if(startwall < 0 || endwall < 0) continue;
			for(z=startwall; z<endwall;z++)
			{
				wal = wall[z];
				if(wal == null || wal.point2 < 0 || wal.point2 >= MAXWALLS) continue;
				nextsectnum = wal.nextsector;

				wal2 = wall[wal.point2];
				if(wal2 == null) continue;
				x1 = wal.x-globalposx; y1 = wal.y-globalposy;
				x2 = wal2.x-globalposx; y2 = wal2.y-globalposy;

				if ((nextsectnum >= 0) && ((wal.cstat&32) == 0))
					if ((gotsector[nextsectnum>>3]&pow2char[nextsectnum&7]) == 0)
					{
						templong = x1*y2-x2*y1;
						if ((templong+262144) < 524288)
							if (mulscale(templong,templong,5) <= (x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))
								sectorborder[sectorbordercnt++] = nextsectnum;
					}

				if ((z == startwall) || (wall[z-1].point2 != z))
				{
					xp1 = dmulscale(y1,cosglobalang,-x1,singlobalang,6);
					yp1 = dmulscale(x1,cosviewingrangeglobalang,y1,sinviewingrangeglobalang,6);
				}
				else
				{
					xp1 = xp2;
					yp1 = yp2;
				}
				xp2 = dmulscale(y2,cosglobalang,-x2,singlobalang,6);
				yp2 = dmulscale(x2,cosviewingrangeglobalang,y2,sinviewingrangeglobalang,6);
			
				do {
					if ((yp1 < 256) && (yp2 < 256)) { break; }
	
					//If wall's NOT facing you
					if (dmulscale(xp1,yp2,-xp2,yp1,32) >= 0) { break; }
	
					if(numscans >= MAXWALLSB) break;
					
					if (xp1 >= -yp1)
					{
						if ((xp1 > yp1) || (yp1 == 0)) { break; }
						xb1[numscans] = halfxdimen + scale(xp1,halfxdimen,yp1);
						if (xp1 >= 0) xb1[numscans]++;   //Fix for SIGNED divide
						if (xb1[numscans] >= xdimen) xb1[numscans] = xdimen-1;
						yb1[numscans] = yp1;
					}
					else
					{
						if (xp2 < -yp2) { break; }
						xb1[numscans] = 0;
						templong = yp1-yp2+xp1-xp2;
						if (templong == 0) { break; }
						yb1[numscans] = yp1 + scale((yp2-yp1),(xp1+yp1),templong);
					}
					
					if (yb1[numscans] < 256) { break; }
	
					if (xp2 <= yp2)
					{
						if ((xp2 < -yp2) || (yp2 == 0)) { break; }
						xb2[numscans] = halfxdimen + scale(xp2,halfxdimen,yp2) - 1;
						if (xp2 >= 0) xb2[numscans]++;   //Fix for SIGNED divide
						if (xb2[numscans] >= xdimen) xb2[numscans] = xdimen-1;
						yb2[numscans] = yp2;
					}
					else
					{
						if (xp1 > yp1) { break; }
						xb2[numscans] = xdimen-1;
						templong = xp2-xp1+yp1-yp2;
						if (templong == 0) { break; }
						yb2[numscans] = yp1 + scale((yp2-yp1),(yp1-xp1),templong);
					}
					if ((yb2[numscans] < 256) || (xb1[numscans] > xb2[numscans])) { break; }
	
					//Made it all the way!
					thesector[numscans] = sectnum; thewall[numscans] = (short) z;
					rx1[numscans] = xp1; ry1[numscans] = yp1;
					rx2[numscans] = xp2; ry2[numscans] = yp2;
					p2[numscans] = (short) (numscans+1);
					numscans++;
				}
				while(false);
				
				if ((wall[z].point2 < z) && (scanfirst < numscans))
				{ p2[numscans-1] = (short) scanfirst; scanfirst = numscans; }
			}

			for(z=numscansbefore;z<numscans;z++) {
				if(z >= MAXWALLSB || p2[z] >= MAXWALLSB) continue;
				if ((wall[thewall[z]].point2 != thewall[p2[z]]) || (xb2[z] >= xb1[p2[z]])) {
					bunchfirst[numbunches++] = p2[z]; 
					p2[z] = -1;
				}
			}

			for(z=bunchfrst;z<numbunches;z++)
			{
				if(p2[z] >= MAXWALLSB) continue;
				for(zz=bunchfirst[z];p2[zz]>=0;zz=p2[zz]);
				bunchlast[z] = (short) zz;
			}
		} while (sectorbordercnt > 0);
	}
	
	private boolean spritewallfront(SPRITE s, int w)
	{
		WALL wal = wall[w]; int x1 = wal.x; int y1 = wal.y;
		wal = wall[wal.point2];
		return (dmulscale(wal.x-x1,s.y-y1,-(s.x-x1),wal.y-y1,32) >= 0);
	}

	private int bunchfront(int b1, int b2)
	{
		int x1b1, x2b1, x1b2, x2b2, b1f, b2f, i;

		b1f = bunchfirst[b1]; 
		x1b1 = xb1[b1f]; 
		x2b2 = (xb2[bunchlast[b2]]+1);
		if (x1b1 >= x2b2) return(-1);
		b2f = bunchfirst[b2]; 
		x1b2 = xb1[b2f]; 
		x2b1 = (xb2[bunchlast[b1]]+1);
		if (x1b2 >= x2b1) return(-1);

		if (x1b1 >= x1b2) {
			for (i = b2f; xb2[i] <= x1b1 && p2[i] != -1; i = p2[i]);
			return (wallfront(b1f, i));
		}

		for (i = b1f; xb2[i] <= x1b2 && p2[i] != -1; i = p2[i]);
		return (wallfront(i, b2f));
	}
	
	
	private int wallfront(int l1, int l2) {
		WALL wal;
		int x11, y11, x21, y21, x12, y12, x22, y22, dx, dy, t1, t2;

		wal = wall[thewall[l1]]; x11 = wal.x; y11 = wal.y;
		wal = wall[wal.point2]; x21 = wal.x; y21 = wal.y;
		wal = wall[thewall[l2]]; x12 = wal.x; y12 = wal.y;
		wal = wall[wal.point2]; x22 = wal.x; y22 = wal.y;

		dx = x21-x11; dy = y21-y11;
		t1 = dmulscale(x12-x11,dy,-dx,y12-y11,2); //p1(l2) vs. l1
		t2 = dmulscale(x22-x11,dy,-dx,y22-y11,2); //p2(l2) vs. l1
		if (t1 == 0) { t1 = t2; if (t1 == 0) return(-1); }
		if (t2 == 0) t2 = t1;
		if ((t1^t2) >= 0)
		{
			t2 = (int) dmulscale(globalposx-x11,dy,-dx,globalposy-y11,2); //pos vs. l1
			return((t2^t1) >= 0 ? 1 : 0);
		}

		dx = x22-x12; dy = y22-y12;
		t1 = dmulscale(x11-x12,dy,-dx,y11-y12,2); //p1(l1) vs. l2
		t2 = dmulscale(x21-x12,dy,-dx,y21-y12,2); //p2(l1) vs. l2
		if (t1 == 0) { t1 = t2; if (t1 == 0) return(-1); }
		if (t2 == 0) t2 = t1;
		if ((t1^t2) >= 0)
		{
			t2 = dmulscale(globalposx-x12,dy,-dx,globalposy-y12,2); //pos vs. l2
			return((t2^t1) < 0 ? 1 : 0);
		}
		return(-2);
	}
	
	private int owallmost(short[] mostbuf, int w, int z)
	{
		int bad, inty, xcross, y;
		int s1, s2, s3, s4, ix1, ix2, iy1, iy2;
		int i;

		z <<= 7;
		s1 = mulscale(globaluclip,yb1[w],20); s2 = mulscale(globaluclip,yb2[w],20);
		s3 = mulscale(globaldclip,yb1[w],20); s4 = mulscale(globaldclip,yb2[w],20);
		bad = (z<s1?1:0)+((z<s2?1:0)<<1)+((z>s3?1:0)<<2)+((z>s4?1:0)<<3);

		ix1 = xb1[w]; iy1 = yb1[w];
		ix2 = xb2[w]; iy2 = yb2[w];

		if ((bad&3) == 3)
		{
			for (i=ix1; i<=ix2; i++) mostbuf[i] = 0;
			return(bad);
		}

		if ((bad&12) == 12)
		{
			for (i=ix1; i<=ix2; i++) mostbuf[i] = (short) ydimen;
			return(bad);
		}

		if ((bad&3) != 0)
		{
			long t = divscale((z-s1),(s2-s1), 30);
			inty = yb1[w] + mulscale(yb2[w]-yb1[w],t,30);
			xcross = xb1[w] + scale(mulscale(yb2[w],t,30),(xb2[w]-xb1[w]),inty);

			if ((bad&3) == 2)
			{
				if (xb1[w] <= xcross) { iy2 = inty; ix2 = xcross; }
				for (i=(xcross+1); i<=xb2[w]; i++) mostbuf[i] = 0;
			}
			else
			{
				if (xcross <= xb2[w]) { iy1 = inty; ix1 = xcross; }
				for (i=xb1[w]; i<=xcross; i++) mostbuf[i] = 0;
			}
		}

		if ((bad&12) != 0)
		{
			long t = divscale((z-s3),(s4-s3), 30);
			inty = yb1[w] + mulscale(yb2[w]-yb1[w],t,30);
			xcross = xb1[w] + scale(mulscale(yb2[w],t,30),xb2[w]-xb1[w],inty);

			if ((bad&12) == 8)
			{
				if (xb1[w] <= xcross) { iy2 = inty; ix2 = xcross; }
				for (i=(xcross+1); i<=xb2[w]; i++) mostbuf[i] = (short) ydimen;
			}
			else
			{
				if (xcross <= xb2[w]) { iy1 = inty; ix1 = xcross; }
				
				for (i=xb1[w]; i<=xcross; i++) mostbuf[i] = (short) ydimen;
			}
		}

		y = (scale(z,xdimenscale,iy1)<<4);
		long yinc = ((scale(z,xdimenscale,iy2)<<4)-y) / (ix2-ix1+1);

		qinterpolatedown16short(mostbuf,ix1,ix2-ix1+1,y+((int)globalhoriz<<16),yinc);

		if (mostbuf[ix1] < 0) mostbuf[ix1] = 0;
		if (mostbuf[ix1] > ydimen) mostbuf[ix1] = (short) ydimen;
		if (mostbuf[ix2] < 0) mostbuf[ix2] = 0;
		if (mostbuf[ix2] > ydimen) mostbuf[ix2] = (short) ydimen;

		return(bad);
	}
	
	
	private int wallmost(short[] mostbuf, int w, short sectnum, int dastat)
	{
		int bad, i, j, y, z, inty, intz, xcross, yinc, fw;
		int x1, y1, z1, x2, y2, z2, xv, yv, dx, dy, dasqr, oz1, oz2;
		int s1, s2, s3, s4, ix1, ix2, iy1, iy2;
		long t;

		if (dastat == 0)
		{
			z = (sector[sectnum].ceilingz-globalposz);
			if ((sector[sectnum].ceilingstat&2) == 0) return(owallmost(mostbuf,w,z));
		}
		else
		{
			z = (sector[sectnum].floorz-globalposz);
			if ((sector[sectnum].floorstat&2) == 0) return(owallmost(mostbuf,w,z));
		}

		i = thewall[w];
		if (i == sector[sectnum].wallptr) return(owallmost(mostbuf,w,z));

		x1 = wall[i].x; x2 = wall[wall[i].point2].x-x1;
		y1 = wall[i].y; y2 = wall[wall[i].point2].y-y1;

		fw = sector[sectnum].wallptr; i = wall[fw].point2;
		dx = wall[i].x-wall[fw].x; dy = wall[i].y-wall[fw].y;
		dasqr = krecipasm(engine.ksqrt(dx*dx+dy*dy));

		if (xb1[w] == 0)
			{ xv = cosglobalang+sinviewingrangeglobalang; yv = singlobalang-cosviewingrangeglobalang; }
		else
			{ xv = x1-globalposx; yv = y1-globalposy; }
		i = (xv*(y1-globalposy)-yv*(x1-globalposx)); j = (yv*x2-xv*y2);
		if (klabs(j) > klabs(i>>3)) i = (int) divscale(i,j, 28);
		if (dastat == 0)
		{
			t = mulscale(sector[sectnum].ceilingheinum,dasqr,15);
			z1 = sector[sectnum].ceilingz;
		}
		else
		{
			t = mulscale(sector[sectnum].floorheinum,dasqr,15);
			z1 = sector[sectnum].floorz;
		}
		z1 = dmulscale(dx*t,mulscale(y2,i,20)+((y1-wall[fw].y)<<8),
				-dy*t,mulscale(x2,i,20)+((x1-wall[fw].x)<<8),24)+((z1-globalposz)<<7);


		if (xb2[w] == xdimen-1)
			{ xv = cosglobalang-sinviewingrangeglobalang; yv = singlobalang+cosviewingrangeglobalang; }
		else
			{ xv = (x2+x1)-globalposx; yv = (y2+y1)-globalposy; }
		i = (xv*(y1-globalposy)-yv*(x1-globalposx)); j = (yv*x2-xv*y2);
		if (klabs(j) > klabs(i>>3)) i = (int) divscale(i,j, 28);
		if (dastat == 0)
		{
			t = mulscale(sector[sectnum].ceilingheinum,dasqr,15);
			z2 = sector[sectnum].ceilingz;
		}
		else
		{
			t = mulscale(sector[sectnum].floorheinum,dasqr,15);
			z2 = sector[sectnum].floorz;
		}
		z2 = dmulscale(dx*t,mulscale(y2,i,20)+((y1-wall[fw].y)<<8),
				-dy*t,mulscale(x2,i,20)+((x1-wall[fw].x)<<8),24)+((z2-globalposz)<<7);

		s1 = mulscale(globaluclip,yb1[w],20); s2 = mulscale(globaluclip,yb2[w],20);
		s3 = mulscale(globaldclip,yb1[w],20); s4 = mulscale(globaldclip,yb2[w],20);
		bad = (z1<s1?1:0)+((z2<s2?1:0)<<1)+((z1>s3?1:0)<<2)+((z2>s4?1:0)<<3);

		ix1 = xb1[w]; ix2 = xb2[w];
		iy1 = yb1[w]; iy2 = yb2[w];
		oz1 = z1; oz2 = z2;

		if ((bad&3) == 3)
		{
			for (i=ix1; i<=ix2; i++) mostbuf[i] = 0;
			return(bad);
		}

		if ((bad&12) == 12)
		{
			for (i=ix1; i<=ix2; i++) mostbuf[i] = (short) ydimen;
			return(bad);
		}

		if ((bad&3) != 0)
		{
			t = divscale((oz1-s1),(s2-s1+oz1-oz2), 30);
			inty = (yb1[w] + mulscale(yb2[w]-yb1[w],t,30));
			intz = (oz1 + mulscale(oz2-oz1,t,30));
			xcross = (xb1[w] + scale(mulscale(yb2[w],t,30),xb2[w]-xb1[w],inty));

			if ((bad&3) == 2)
			{
				if (xb1[w] <= xcross) { z2 = intz; iy2 = inty; ix2 = xcross; }
				for (i=xcross+1; i<=xb2[w]; i++) mostbuf[i] = 0;
			}
			else
			{
				if (xcross <= xb2[w]) { z1 = intz; iy1 = inty; ix1 = xcross; }
				for (i=xb1[w]; i<=xcross; i++) mostbuf[i] = 0;
			}
		}

		if ((bad&12) != 0)
		{
			t = divscale((oz1-s3),(s4-s3+oz1-oz2), 30);
			inty = (yb1[w] + mulscale(yb2[w]-yb1[w],t,30));
			intz = oz1 + mulscale(oz2-oz1,t,30);
			xcross = (xb1[w] + scale(mulscale(yb2[w],t,30),(xb2[w]-xb1[w]),inty));
			if ((bad&12) == 8)
			{
				if (xb1[w] <= xcross) { z2 = intz; iy2 = inty; ix2 = xcross; }
				for (i=xcross+1; i<=xb2[w]; i++) mostbuf[i] = (short) ydimen;
			}
			else
			{
				if (xcross <= xb2[w]) { z1 = intz; iy1 = inty; ix1 = xcross; }
				for (i=xb1[w]; i<=xcross; i++) mostbuf[i] = (short) ydimen;
			}
		}

		y = scale(z1,xdimenscale,iy1)<<4;
		yinc = ((scale(z2,xdimenscale,iy2)<<4)-y) / (ix2-ix1+1);
		
		qinterpolatedown16short(mostbuf,ix1,ix2-ix1+1,y+((int)globalhoriz<<16),yinc);

		if (mostbuf[ix1] < 0) mostbuf[ix1] = 0;
		if (mostbuf[ix1] > ydimen) mostbuf[ix1] = (short) ydimen;
		if (mostbuf[ix2] < 0) mostbuf[ix2] = 0;
		if (mostbuf[ix2] > ydimen) mostbuf[ix2] = (short) ydimen;

		return(bad);
	}
	
	public int krecipasm(int i) {
		i = Float.floatToIntBits(i);
		return(reciptable[(i>>12)&2047]>>(((i-0x3f800000)>>23)&31))^(i>>31);
	}
	
	
	private void slowhline(int xr, int yp)
	{
		int xl = lastx[yp]; if (xl > xr) return;
		int r = lookups[horizlookup2+yp-(int)globalhoriz+horizycent];
		a.asm1 = (int) (globalx1*r);
		a.asm2 = (int) (globaly2*r);

		a.hlinepal = globalpalwritten;
		a.hlineshade = (engine.getpalookup(mulscale(r,globvis,16),globalshade)<<8);
		
		if ((globalorientation&256) == 0)
		{
			a.mhline(globalbufplc,globaly1*r+globalxpanning-a.asm1*(xr-xl),(xr-xl)<<16,0,
				globalx2*r+globalypanning-a.asm2*(xr-xl),ylookup[yp]+xl);
			return;
		}
		a.thline(globalbufplc,globaly1*r+globalxpanning-a.asm1*(xr-xl),(xr-xl)<<16,0,
			globalx2*r+globalypanning-a.asm2*(xr-xl),ylookup[yp]+xl);
	}
	
	public JCanvas getCanvas()
	{
		return display.getCanvas();
	}

}
