package ru.m210projects.Build.Render.Software;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.Pragmas.mulscale;
import static ru.m210projects.Build.Render.Software.Software.*;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Types.Palette;

public class Ac {
	
	private Software r;
	private Engine engine;
	public Ac(Software render)
	{
		this.r = render;
		this.engine = render.engine;
	}

	public void hline(int xr, int yp) 
	{
		int xl = r.lastx[yp]; if (xl > xr) return;
		int rr = r.lookups[r.horizlookup2+(int) (yp-globalhoriz+r.horizycent)];
		asm1 = r.globalx1*rr;
		asm2 = r.globaly2*rr;
		int s = (engine.getpalookup(mulscale(rr,r.globvis,16),globalshade)<<8);

		hlineasm4(xr-xl,0,s,r.globalx2*rr+r.globalypanning,r.globaly1*rr+r.globalxpanning, r.ylookup[yp]+xr);
	}
	
	public void thline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) //XXX
	{
		char ch;

		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		if (transmode != 0)
		{
			for(cntup16>>=16;cntup16>0;cntup16--)
			{
//				ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
//				if (ch != 255) *((char *)p) = gtrans[(*((char *)p))+(gpal[ch]<<8)];
				bx += asm1;
				by += asm2;
				p++;
			}
		}
		else
		{
			for(cntup16>>=16;cntup16>0;cntup16--)
			{
//				ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
//				if (ch != 255) *((char *)p) = gtrans[((*((char *)p))<<8)+gpal[ch]];
				bx += asm1;
				by += asm2;
				p++;
			}
		}
	}
	
	public void hlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p)
	{
		if (skiploadincs == 0) { gbxinc = asm1; gbyinc = asm2; }
		for(;cnt>=0;cnt--)
		{
			if(((bx>>(32-glogx))<<glogy)+(by>>(32-glogy)) < 0 || ((bx>>(32-glogx))<<glogy)+(by>>(32-glogy)) >= gbuf.length)
				continue; //XXX
			
			int col = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))] & 0xFF;
			int dacol = palookup[ghlinepal][col + paloffs] & 0xFF;
			Palette color = curpalette[dacol];
			
			r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
			bx -= gbxinc;
			by -= gbyinc;
			p--;
		}
	}
	
	//	A XXX
	
	private int transmode = 0;
	private int gbxinc, gbyinc, glogx, glogy, gtrans;
	private int gpal, gshade;
	private int bpl, gpinc, gbufoffs, ghlinepal;
	private byte[] gtransbuf, gbuf;
	public int asm1, asm2, asm3;
	public int hlinepal, hlineshade;
	
	//Global variable functions
	public void setvlinebpl(int dabpl) { bpl = dabpl; }
	public void fixtransluscence(byte[] datrans, int datransoff) { gtransbuf = datrans; gtrans = datransoff; }
	public void settransnormal() { transmode = 0; }
	public void settransreverse() { transmode = 1; }
	
	//Wall,face sprite/wall sprite vertical line functions
	public void setupvlineasm(int neglogy) { glogy = neglogy; }
	public void vlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p)
	{
		gbuf = bufplc;
		gbufoffs = bufoffs;
		gpal = pal;
		gshade = shade;
		
		for(;cnt>=0;cnt--)
		{
			if(gbufoffs + vplc>>glogy < 0 || gbufoffs + vplc>>glogy >= gbuf.length)
				continue; //XXX
			
			int col = gbuf[gbufoffs + vplc>>glogy] & 0xFF;
			int dacol = palookup[gpal][col + gshade] & 0xFF;
			Palette color = curpalette[dacol];
			
			r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
			p += bpl;
			vplc += vinc;
		}
	}
	
	//Floor sprite horizontal line functions
	public void msethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	public void mhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p)
	{
		gbuf = bufplc;
		gpal = hlinepal;
		gshade = hlineshade;
		for(cntup16>>=16;cntup16>0;cntup16--)
		{
			int ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))] & 0xFF;
			
			if (ch != 255) {
				int dacol = palookup[gpal][ch + gshade] & 0xFF;
				Palette color = curpalette[dacol];
				r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
			}
			
			bx += asm1;
			by += asm2;
			p++;
		}
	}
	
	//Sloped ceiling/floor vertical line functions
	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc)
	{
		glogx = (logylogx&255); glogy = (logylogx>>8);
		gbuf = bufplc; gpinc = pinc;
	}
	
	public void slopevlin(int p, int i, int slopaloffs, int cnt, int bx, int by)
	{
		int bz = asm3; int bzinc = (asm1>>3);
		for(;cnt>0;cnt--)
		{
			i = r.krecipasm(bz>>6); bz += bzinc;
			int u = bx+r.globalx3*i;
			int v = by+r.globaly3*i;
			
			if(((u>>(32-glogx))<<glogy)+(v>>(32-glogy)) < 0 || ((u>>(32-glogx))<<glogy)+(v>>(32-glogy)) >= gbuf.length)
				continue; //XXX
			
			int ch = gbuf[((u>>(32-glogx))<<glogy)+(v>>(32-glogy))] & 0xFF;

			Palette color = curpalette[r.slopalookup[slopaloffs] + ch];
			r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);	
			slopaloffs--;
			p += gpinc;
		}
	}
	
	public void tsethlineshift(int logx, int logy) { glogx = logx; glogy = logy; }
	
	public void setpalookupaddress(int paladdr) { ghlinepal = paladdr; }
	
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
		for(;cnt>1;cnt--)
		{
			int col = bufplc[bufoffs + (bx>>16)*glogy+(by>>16)] & 0xFF;
			int dacol = palookup[gpal][col + gshade] & 0xFF;
			Palette color = curpalette[dacol];
			
			r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
			
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
		for(;cnt>1;cnt--)
		{
			int col = bufplc[bufoffs + (bx>>16)*glogy+(by>>16)] & 0xFF;
			if(col != 255) {
				int dacol = palookup[gpal][col + gshade] & 0xFF;
				Palette color = curpalette[dacol];
				
				r.frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
			} 
			
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
	
	public void tspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) //XXX
	{
		if (transmode != 0)
		{
			for(;cnt>1;cnt--)
			{
				int col = bufplc[bufoffs + (bx>>16)*glogy+(by>>16)] & 0xFF;
				if(col != 255) {
//					int dacol = palookup[gpal][col + gshade] & 0xFF;
//					Palette color = curpalette[gtransbuf[gtrans + (dacol << 8)] & 0xFF];
//					
//					frameplace[p] = color.b + (color.g << 8) + (color.r << 16);
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
//				int col = bufplc[bufoffs + (bx>>16)*glogy+(by>>16)] & 0xFF;
//				if (ch != 255) *((char *)p) = gtransbuf[gtrans+((*((char *)p))<<8)+palookup[gpal+ch]];
				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		}
	}
	
	public void sethlinesizes(int logx, int logy, byte[] bufplc)
	{ glogx = logx; glogy = logy; gbuf = bufplc; }
}
