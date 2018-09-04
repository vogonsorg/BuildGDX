package ru.m210projects.Build.Render.Software;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.mulscale;
import ru.m210projects.Build.Engine;

public class Ac {
	
	private Software r;
	private Engine engine;
	
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
	public void hlineasm4(int cnt, int skiploadincs, int paloffs, long by, long bx, int p)
	{
		if (skiploadincs == 0) { gbxinc = asm1; gbyinc = asm2; }
		for(;cnt>=0;cnt--)
		{
			int index = (int) ((((bx & 0xFFFFFFFFL)>>(32-glogx))<<glogy)+((by & 0xFFFFFFFFL)>>(32-glogy)));
			if(index < 0 || index >= gbuf.length) continue;
			int col = gbuf[index] & 0xFF;
			r.frameplace[p] = palookup[ghlinepal][col + paloffs];
			bx -= gbxinc;
			by -= gbyinc;
			p--;
		}
	}
	
	//Sloped ceiling/floor vertical line functions
	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc)
	{
		glogx = (logylogx&255); glogy = (logylogx>>8);
		gbuf = bufplc; gpinc = pinc;
	}
	
	public void slopevlin(int p, int i, int slopaloffs, int cnt, long bx, long by)
	{
		int bz = asm3; 
		int bzinc = (asm1>>3);
		for(;cnt>0;cnt--)
		{
			i = r.krecipasm(bz>>6); bz += bzinc;
			long u = bx+r.globalx3*i;
			long v = by+r.globaly3*i;

			int index = (int) ((((u & 0xFFFFFFFFL)>>(32-glogx))<<glogy)+((v & 0xFFFFFFFFL)>>(32-glogy)));
			if(index < 0 || index >= gbuf.length) continue;
			int ch = gbuf[index] & 0xFF;
			r.frameplace[p] = (byte) (r.slopalookup[slopaloffs] + ch);
			slopaloffs--;
			p += gpinc;
		}
	}
	
	//Wall,face sprite/wall sprite vertical line functions
	public void setupvlineasm(int neglogy) { glogy = neglogy; }
	public void vlineasm1(int vinc, int pal, int shade, int cnt, long vplc, byte[] bufplc, int bufoffs, int p)
	{
		int index, col;
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;

		for(;cnt>=0;cnt--)
		{
			index = (int) (bufoffs + ((vplc & 0xFFFFFFFFL) >> glogy));
			if(index < 0 || index >= gbuf.length) continue;
			col = gbuf[index] & 0xFF;
			r.frameplace[p] = palookup[gpal][col + gshade];
			p += bpl;
			vplc += vinc;
		}
	}
	
	public void setupmvlineasm(int neglogy) { glogy = neglogy; }
	public void mvlineasm1(int vinc, int pal, int shade, int cnt, long vplc, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;

		for(;cnt>=0;cnt--)
		{
			int index = (int) (bufoffs + ((vplc & 0xFFFFFFFFL) >> glogy));
			if(index < 0 || index >= gbuf.length) continue;
			int ch = gbuf[index] & 0xFF;
			if (ch != 255) r.frameplace[p] = palookup[gpal][ch + gshade];
			p += bpl;
			vplc += vinc;
		}
	}

	public void setuptvlineasm(int neglogy) { glogy = neglogy; }
	public void tvlineasm1(int vinc, int pal, int shade, int cnt, long vplc, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		gpal = pal;
		gshade = shade;
		if (transmode != 0)
		{
			for(;cnt>=0;cnt--)
			{
				int index = (int) (bufoffs + ((vplc & 0xFFFFFFFFL) >> glogy));
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				
				int ch = gbuf[index] & 0xFF;
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				if (ch != 255) r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
				p += bpl;
				vplc += vinc;
			}
		}
		else
		{
			for(;cnt>=0;cnt--)
			{
				int index = (int) (bufoffs + ((vplc & 0xFFFFFFFFL) >> glogy));
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				
				int ch = gbuf[index] & 0xFF;
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				if (ch != 255) r.frameplace[p] = gtrans[((r.frameplace[p]&0xFF)<<8)+dacol];
				p += bpl;
				vplc += vinc;
			}
		}
	}
	
	//Floor sprite horizontal line functions
	public void msethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	public void mhline(byte[] bufplc, long bx, int cntup16, int junk, long by, int p)
	{
		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		for(cntup16>>=16;cntup16>0;cntup16--)
		{
			int index = (int) ((((bx & 0xFFFFFFFFL)>>(32-glogx))<<glogy)+((by & 0xFFFFFFFFL)>>(32-glogy)));
			if(index < 0 || index >= gbuf.length) continue;
			int ch = gbuf[index] & 0xFF;
			if (ch != 255) r.frameplace[p] = palookup[gpal][ch + gshade];

			bx += asm1;
			by += asm2;
			p++;
		}
	}
	
	public void tsethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	public void thline(byte[] bufplc, long bx, int cntup16, int junk, long by, int p)
	{
		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		
		if (transmode != 0)
		{
			for(cntup16>>=16;cntup16>0;cntup16--)
			{
				int index = (int) ((((bx & 0xFFFFFFFFL)>>(32-glogx))<<glogy)+((by & 0xFFFFFFFFL)>>(32-glogy)));
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				int ch = gbuf[index] & 0xFF;
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				if (ch != 255) r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
				bx += asm1;
				by += asm2;
				p++;
			}
		}
		else
		{
			for(cntup16>>=16;cntup16>0;cntup16--)
			{
				int index = (int) ((((bx & 0xFFFFFFFFL)>>(32-glogx))<<glogy)+((by & 0xFFFFFFFFL)>>(32-glogy)));
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				int ch = gbuf[index] & 0xFF;
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				if (ch != 255) r.frameplace[p] = gtrans[+((r.frameplace[p]&0xFF)<<8)+dacol];
				bx += asm1;
				by += asm2;
				p++;
			}
		}
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
		for(;cnt>1;cnt--)
		{
			int index = bufoffs + (bx>>16)*glogy+(by>>16);
			if(index < 0 || index >= gbuf.length) continue;
			int col = gbuf[index] & 0xFF;
			r.frameplace[p] = palookup[gpal][col + gshade];
			
			bx += gbxinc;
			by += gbyinc;
			p += bpl;
		}
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
		for(;cnt>1;cnt--)
		{
			int index = bufoffs + (bx>>16)*glogy+(by>>16);
			if(index < 0 || index >= gbuf.length) continue;
			int col = gbuf[index] & 0xFF;
			if(col != 255) r.frameplace[p] = palookup[gpal][col + gshade];

			bx += gbxinc;
			by += gbyinc;
			p += bpl;
		}
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
		gbuf = bufplc;
		if (transmode != 0)
		{
			for(;cnt>1;cnt--)
			{
				int index = bufoffs + (bx>>16)*glogy+(by>>16);
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				
				int col = gbuf[index] & 0xFF;
				if(col != 255) {
					int dacol = palookup[gpal][col + gshade] & 0xFF;
					if (col != 255) r.frameplace[p] = gtrans[(r.frameplace[p]&0xFF)+(dacol<<8)];
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
				int index = bufoffs + (bx>>16)*glogy+(by>>16);
				if(index < 0 || index >= gbuf.length || gtrans == null) continue;
				int ch = gbuf[index] & 0xFF;
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				if (ch != 255) r.frameplace[p] = gtrans[((r.frameplace[p]&0xFF)<<8)+dacol];
				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		}
	}
	
	public void setupdrawslab (int dabpl, int pal, int shade)
	{ bpl = dabpl; gpal = pal; gshade = shade; }
	public void drawslab (int dx, int v, int dy, int vi, int vptr, int p)
	{
		int x;
		while (dy > 0)
		{
			for(x=0;x<dx;x++) 
				r.frameplace[p+x] = palookup[gpal][((v>>16)+vptr) + gshade];
			p += bpl; v += vi; dy--;
		}
	}
	
	public void hline(int xr, int yp) 
	{
		int xl = r.lastx[yp]; if (xl > xr) return;
		int rr = r.lookups[r.horizlookup2+(int) (yp-globalhoriz+r.horizycent)];
		asm1 = (int) (r.globalx1*rr);
		asm2 = (int) (r.globaly2*rr);
		int s = (engine.getpalookup(mulscale(rr,r.globvis,16),globalshade)<<8);
		
		hlineasm4(xr-xl,0,s,r.globalx2*rr+r.globalypanning,r.globaly1*rr+r.globalxpanning, r.ylookup[yp]+xr);
	}	
}
	
	
