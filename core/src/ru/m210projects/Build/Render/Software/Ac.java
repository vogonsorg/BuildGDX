// A.ASM replacement using C
// Mainly by Ken Silverman, with things melded with my port by
// Jonathon Fowler (jonof@edgenetwork.org)
//
// "Build Engine & Tools" Copyright (c) 1993-1997 Ken Silverman
// Ken Silverman's official web site: "http://www.advsys.net/ken"
// See the included license file "BUILDLIC.TXT" for license info.
//
// This file has been modified from Ken Silverman's original release
// by Alexander Makarov-[M210] (m210-2007@mail.ru)


package ru.m210projects.Build.Render.Software;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.mulscale;
import ru.m210projects.Build.Engine;

public class Ac {
	
	private Software r;
	private Engine engine;
	
	private int index, ch;
	private int transmode = 0;
	private int gbxinc, gbyinc, glogx, glogy;
	private int gpal, gshade;
	private int bpl, gpinc, ghlinepal;
	private byte[] gtrans, gbuf;
	public int asm1, asm2, asm3;
	public int hlinepal, hlineshade;
	
	public Ac(Software render)
	{
		this.r = render;
		this.engine = render.engine;
	}
	
	//Global variable functions
	public void setvlinebpl(int dabpl) { bpl = dabpl; }
	public void fixtransluscence(byte[] datrans) { gtrans = datrans; }
	public void settransnormal() { transmode = 0; }
	public void settransreverse() { transmode = 1; }
	
	//Ceiling/floor horizontal line functions
	public void sethlinesizes(int logx, int logy, byte[] bufplc)
	{ glogx = logx; glogy = logy; gbuf = bufplc; }
	public void setpalookupaddress(int paladdr) { ghlinepal = paladdr; }
	public void setuphlineasm4(int bxinc, int byinc) { gbxinc = bxinc; gbyinc = byinc; }
	public void hlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p)
	{
		if (skiploadincs == 0) { gbxinc = asm1; gbyinc = asm2; }
		try {
			for(;cnt>=0;cnt--)
			{
				index = ((bx>>>(32-glogx))<<glogy)+(by>>>(32-glogy));
				r.frameplace[p] = palookup[ghlinepal][(gbuf[index] & 0xFF) + paloffs];
				bx -= gbxinc;
				by -= gbyinc;
				p--;
			}
		} catch (Throwable e) { System.err.println("hlineasm4"); }
	}
		
	
	//Sloped ceiling/floor vertical line functions
	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc)
	{
		glogx = (logylogx&255); glogy = (logylogx>>8);
		gbuf = bufplc; gpinc = pinc;
	}
	
	public void slopevlin(int p, int pal, int slopaloffs, int cnt, int bx, int by)
	{
		try {
			int bz = asm3; 
			int bzinc = (asm1>>3);
			int u, v, i;
			for(;cnt>0;cnt--)
			{
				i = r.krecipasm(bz>>6); bz += bzinc;
				u = bx+r.globalx3*i;
				v = by+r.globaly3*i;
	
				index = ((u>>>(32-glogx))<<glogy)+(v>>>(32-glogy));
				r.frameplace[p] = palookup[pal][(gbuf[index] & 0xFF) + r.slopalookup[slopaloffs]];
				slopaloffs--;
				p += gpinc;
			}
		} catch (Throwable e) { System.err.println("slopevlin"); }
	}
	
	//Wall,face sprite/wall sprite vertical line functions
	public void setupvlineasm(int neglogy) { glogy = neglogy; }
	public void vlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;
		try {
			for(;cnt>=0;cnt--)
			{
				index = bufoffs + (vplc >>> glogy);
				r.frameplace[p] = palookup[gpal][(gbuf[index] & 0xFF) + gshade];
				p += bpl;
				vplc += vinc;
			}
		} catch (Throwable e) { System.err.println("vlineasm1"); }
	}
	
	public void setupmvlineasm(int neglogy) { glogy = neglogy; }
	public void mvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;

		try {
			for(;cnt>=0;cnt--)
			{
				index = bufoffs + (vplc >>> glogy);
				ch = gbuf[index] & 0xFF;
				if (ch != 255) r.frameplace[p] = palookup[gpal][ch + gshade];
				p += bpl;
				vplc += vinc;
			}
		} catch (Throwable e) { System.err.println("mvlineasm1"); }
	}

	public void setuptvlineasm(int neglogy) { glogy = neglogy; }
	public void tvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p)
	{
		int dacol;
		
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;
		try {
			if (transmode != 0)
			{
				for(;cnt>=0;cnt--)
				{
					index = bufoffs + (vplc >>> glogy);
					ch = gbuf[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
					}
					p += bpl;
					vplc += vinc;
				}
			}
			else
			{
				for(;cnt>=0;cnt--)
				{
					index = bufoffs + (vplc >>> glogy);
					ch = gbuf[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[((r.frameplace[p]&0xFF)<<8)+dacol];
					}
					p += bpl;
					vplc += vinc;
				}
			}
		} catch (Throwable e) { System.err.println("tvlineasm1"); }
	}
	
	//Floor sprite horizontal line functions
	public void msethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	public void mhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) //check
	{
		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		try {
			for(cntup16>>=16;cntup16>0;cntup16--)
			{
				index = ((bx>>>(32-glogx))<<glogy)+(by>>>(32-glogy));
				ch = gbuf[index] & 0xFF;
				if (ch != 255) r.frameplace[p] = palookup[gpal][ch + gshade];
	
				bx += asm1;
				by += asm2;
				p++;
			}
		} catch (Throwable e) { System.err.println("mhline"); }
	}
	
	public void tsethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	public void thline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) //check
	{
		int dacol;
		
		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		try {
			if (transmode != 0)
			{
				for(cntup16>>=16;cntup16>0;cntup16--)
				{
					index = ((bx>>>(32-glogx))<<glogy)+(by>>>(32-glogy));
					ch = gbuf[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
					}
					bx += asm1;
					by += asm2;
					p++;
				}
			}
			else
			{
				for(cntup16>>=16;cntup16>0;cntup16--)
				{
					index = ((bx>>>(32-glogx))<<glogy)+(by>>>(32-glogy));
					ch = gbuf[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[+((r.frameplace[p]&0xFF)<<8)+dacol];
					}
					bx += asm1;
					by += asm2;
					p++;
				}
			}
		} catch (Throwable e) { System.err.println("thline"); }
	}
	
	//Rotatesprite vertical line functions
	public void setupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz)
	{
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}
	
	public void spritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		try {
			for(;cnt>1;cnt--)
			{
				index = bufoffs + (bx>>16)*glogy+(by>>16);
				r.frameplace[p] = palookup[gpal][(gbuf[index] & 0xFF) + gshade];
				
				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		} catch (Throwable e) { System.err.println("rotatesprite spritevline"); }
	}
	
	//Rotatesprite vertical line functions
	public void msetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz)
	{
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}

	public void mspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		try {
			for(;cnt>1;cnt--)
			{
				index = bufoffs + (bx>>16)*glogy+(by>>16);
				
				ch = gbuf[index] & 0xFF;
				if(ch != 255) r.frameplace[p] = palookup[gpal][ch + gshade];
	
				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		} catch (Throwable e) { System.err.println("rotatesprite mspritevline"); }
	}
	
	public void tsetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz)
	{
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}
	
	public void tspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p)
	{
		int dacol;
		gbuf = bufplc;
		try {
			if (transmode != 0)
			{
				for(;cnt>1;cnt--)
				{
					index = bufoffs + (bx>>>16)*glogy+(by>>>16);
					ch = gbuf[index] & 0xFF;
					if(ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
					} 
					bx += gbxinc;
					by += gbyinc;
					p += bpl;
				}
			}
			else
			{
				for(;cnt>1;cnt--)
				{
					index = bufoffs + (bx>>>16)*glogy+(by>>>16);
					ch = gbuf[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						r.frameplace[p] = gtrans[((r.frameplace[p]&0xFF)<<8)+dacol];
					}
					bx += gbxinc;
					by += gbyinc;
					p += bpl;
				}
			}
		} catch (Throwable e) { System.err.println("rotatesprite tspritevline"); }
	}
	
	public void setupdrawslab (int dabpl, int pal, int shade)
	{ bpl = dabpl; gpal = pal; gshade = shade; }
	
	public void drawslab (int dx, int v, int dy, int vi, int vptr, int p) //check
	{
		int x;
		while (dy > 0)
		{
			for(x=0;x<dx;x++) 
				r.frameplace[p+x] = palookup[gpal][((v>>>16)+vptr) + gshade];
			p += bpl; v += vi; dy--;
		}
	}
	
	public void hline(int xr, int yp) 
	{
		if (r.lastx[yp] > xr) return;
		int rr = r.lookups[r.horizlookup2+(int) (yp-globalhoriz+r.horizycent)];
		asm1 = (r.globalx1*rr);
		asm2 = (r.globaly2*rr);
		hlineasm4(xr-r.lastx[yp],0,engine.getpalookup(mulscale(rr,r.globvis,16),globalshade)<<8,r.globalx2*rr+r.globalypanning,r.globaly1*rr+r.globalxpanning, r.ylookup[yp]+xr);
	}	
	
	public void drawpixel(byte[] frameplace, int ptr, byte col)
	{
		frameplace[ptr] = col;
	}
}
	
	
