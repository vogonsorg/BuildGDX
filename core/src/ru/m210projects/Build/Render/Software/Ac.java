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

public class Ac implements A {

	private int transmode = 0;
	private int gbxinc, gbyinc, glogx, glogy;
	private int gpal, gshade;
	private int bpl, gpinc, ghlinepal;
	private byte[] gtrans, gbuf, frameplace;
	private byte[][] palookup;
	private int[] reciptable;

	private int bzinc;
	private int asm1, asm2;
	private int hlinepal, hlineshade;

	public Ac(byte[] frameplace, byte[][] palookup, int[] reciptable) {
		this.frameplace = frameplace;
		this.palookup = palookup;
		this.reciptable = reciptable;
	}

	// Global variable functions
	@Override
	public void setvlinebpl(int dabpl) {
		bpl = dabpl;
	}

	@Override
	public void fixtransluscence(byte[] datrans) {
		gtrans = datrans;
	}

	@Override
	public void settransnormal() {
		transmode = 0;
	}

	@Override
	public void settransreverse() {
		transmode = 1;
	}

	// Ceiling/floor horizontal line functions
	@Override
	public void sethlinesizes(int logx, int logy, byte[] bufplc) {
		glogx = logx;
		glogy = logy;
		gbuf = bufplc;
	}

	@Override
	public void setpalookupaddress(int paladdr) {
		ghlinepal = paladdr;
	}

	@Override
	public void setuphlineasm4(int bxinc, int byinc) {
		gbxinc = bxinc;
		gbyinc = byinc;
	}

	@Override
	public void hlineasm4(int cnt, int skiploadincs, int paloffs, int by, int bx, int p) {
		if (skiploadincs == 0) {
			gbxinc = asm1;
			gbyinc = asm2;
		}

		try {
			for (; cnt >= 0; cnt--) {
				int index = ((bx >>> (32 - glogx)) << glogy) + (by >>> (32 - glogy));
				frameplace[p] = palookup[ghlinepal][(gbuf[index] & 0xFF) + paloffs];
				bx -= gbxinc;
				by -= gbyinc;
				p--;
			}
		} catch (Throwable e) {
		}
	}

	// Sloped ceiling/floor vertical line functions
	@Override
	public void setupslopevlin(int logylogx, byte[] bufplc, int pinc, int bzinc) {
		glogx = (logylogx & 255);
		glogy = (logylogx >> 8);
		gbuf = bufplc;
		gpinc = pinc;
		this.bzinc = (bzinc >> 3);
	}

	@Override
	public void slopevlin(int p, int pal, int slopaloffs, int cnt, int bx, int by, int x3, int y3, int[] slopalookup,
			int bz) {
		try {
			int u, v, i, index;
			for (; cnt > 0; cnt--) {
				i = krecipasm(bz >> 6);
				bz += bzinc;
				u = bx + x3 * i;
				v = by + y3 * i;

				index = ((u >>> (32 - glogx)) << glogy) + (v >>> (32 - glogy));
				frameplace[p] = palookup[pal][(gbuf[index] & 0xFF) + slopalookup[slopaloffs]];
				slopaloffs--;
				p += gpinc;
			}
		} catch (Throwable e) {
		}
	}

	private int krecipasm(int i) { // Copied from software renderer
		i = Float.floatToIntBits(i);
		return (reciptable[(i >> 12) & 2047] >> (((i - 0x3f800000) >> 23) & 31)) ^ (i >> 31);
	}

	// Wall,face sprite/wall sprite vertical line functions
	@Override
	public void setupvlineasm(int neglogy) {
		glogy = neglogy;
	}

	@Override
	public void vlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		try {
			for (; cnt >= 0; cnt--) {
				int index = bufoffs + (vplc >>> glogy);
				frameplace[p] = palookup[pal][(bufplc[index] & 0xFF) + shade];
				p += bpl;
				vplc += vinc;
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void setupmvlineasm(int neglogy) {
		glogy = neglogy;
	}

	@Override
	public void mvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		try {
			for (; cnt >= 0; cnt--) {
				int index = bufoffs + (vplc >>> glogy);
				int ch = bufplc[index] & 0xFF;
				if (ch != 255)
					frameplace[p] = palookup[pal][ch + shade];
				p += bpl;
				vplc += vinc;
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void setuptvlineasm(int neglogy) {
		glogy = neglogy;
	}

	@Override
	public void tvlineasm1(int vinc, int pal, int shade, int cnt, int vplc, byte[] bufplc, int bufoffs, int p) {
		int dacol;
		try {
			if (transmode != 0) {
				for (; cnt >= 0; cnt--) {
					int index = bufoffs + (vplc >>> glogy);
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[pal][ch + shade] & 0xFF;
						frameplace[p] = gtrans[(frameplace[p] & 0xFF) + (dacol << 8)];
					}
					p += bpl;
					vplc += vinc;
				}
			} else {
				for (; cnt >= 0; cnt--) {
					int index = bufoffs + (vplc >>> glogy);
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[pal][ch + shade] & 0xFF;
						frameplace[p] = gtrans[((frameplace[p] & 0xFF) << 8) + dacol];
					}
					p += bpl;
					vplc += vinc;
				}
			}
		} catch (Throwable e) {
		}
	}

	// Floor sprite horizontal line functions

	@Override
	public void sethlineincs(int x, int y) {
		asm1 = x;
		asm2 = y;
	}

	@Override
	public void setuphline(int pal, int shade) {
		hlinepal = pal;
		hlineshade = shade;
	}

	@Override
	public void msethlineshift(int logx, int logy) {
		glogx = logx;
		glogy = logy;
	}

	@Override
	public void mhline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) {
		try {
			for (cntup16 >>= 16; cntup16 > 0; cntup16--) {
				int index = ((bx >>> (32 - glogx)) << glogy) + (by >>> (32 - glogy));
				int ch = bufplc[index] & 0xFF;
				if (ch != 255)
					frameplace[p] = palookup[hlinepal][ch + hlineshade];

				bx += asm1;
				by += asm2;
				p++;
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void tsethlineshift(int logx, int logy) {
		glogx = logx;
		glogy = logy;
	}

	@Override
	public void thline(byte[] bufplc, int bx, int cntup16, int junk, int by, int p) {
		int dacol;
		try {
			if (transmode != 0) {
				for (cntup16 >>= 16; cntup16 > 0; cntup16--) {
					int index = ((bx >>> (32 - glogx)) << glogy) + (by >>> (32 - glogy));
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[hlinepal][ch + hlineshade] & 0xFF;
						frameplace[p] = gtrans[(frameplace[p] & 0xFF) + (dacol << 8)];
					}
					bx += asm1;
					by += asm2;
					p++;
				}
			} else {
				for (cntup16 >>= 16; cntup16 > 0; cntup16--) {
					int index = ((bx >>> (32 - glogx)) << glogy) + (by >>> (32 - glogy));
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[hlinepal][ch + hlineshade] & 0xFF;
						frameplace[p] = gtrans[+((frameplace[p] & 0xFF) << 8) + dacol];
					}
					bx += asm1;
					by += asm2;
					p++;
				}
			}
		} catch (Throwable e) {
		}
	}

	// Rotatesprite vertical line functions
	@Override
	public void setupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}

	@Override
	public void spritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {
		try {
			for (; cnt > 1; cnt--) {
				int index = bufoffs + (bx >> 16) * glogy + (by >> 16);
				frameplace[p] = palookup[gpal][(bufplc[index] & 0xFF) + gshade];

				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		} catch (Throwable e) {
		}
	}

	// Rotatesprite vertical line functions
	@Override
	public void msetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}

	@Override
	public void mspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {
		try {
			for (; cnt > 1; cnt--) {
				int index = bufoffs + (bx >> 16) * glogy + (by >> 16);

				int ch = bufplc[index] & 0xFF;
				if (ch != 255)
					frameplace[p] = palookup[gpal][ch + gshade];

				bx += gbxinc;
				by += gbyinc;
				p += bpl;
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void tsetupspritevline(int pal, int shade, int bxinc, int byinc, int ysiz) {
		gpal = pal;
		gshade = shade;
		gbxinc = bxinc;
		gbyinc = byinc;
		glogy = ysiz;
	}

	@Override
	public void tspritevline(int bx, int by, int cnt, byte[] bufplc, int bufoffs, int p) {

		int dacol;
		try {
			if (transmode != 0) {
				for (; cnt > 1; cnt--) {
					int index = bufoffs + (bx >> 16) * glogy + (by >> 16);
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						frameplace[p] = gtrans[(frameplace[p] & 0xFF) + (dacol << 8)];
					}
					bx += gbxinc;
					by += gbyinc;
					p += bpl;
				}
			} else {
				for (; cnt > 1; cnt--) {
					int index = bufoffs + (bx >> 16) * glogy + (by >> 16);
					int ch = bufplc[index] & 0xFF;
					if (ch != 255) {
						dacol = palookup[gpal][ch + gshade] & 0xFF;
						frameplace[p] = gtrans[((frameplace[p] & 0xFF) << 8) + dacol];
					}
					bx += gbxinc;
					by += gbyinc;
					p += bpl;
				}
			}
		} catch (Throwable e) {
		}
	}

	@Override
	public void setupdrawslab(int dabpl, int pal, int shade, int trans) {
		bpl = dabpl;
		gpal = pal;
		gshade = shade;
		transmode = trans;
	}

	@Override
	public void drawslab(int dx, int v, int dy, int vi, byte[] data, int vptr, int p) {
		int x;
		int dacol;
		switch (transmode) {
		case 0:
			while (dy > 0) {
				for (x = 0; x < dx; x++)
					frameplace[p + x] = palookup[gpal][(data[(v >>> 16) + vptr] & 0xFF) + gshade];
				p += bpl;
				v += vi;
				dy--;
			}
			break;
		case 1:
			while (dy > 0) {
				for (x = 0; x < dx; x++) {
					dacol = palookup[gpal][(data[(v >>> 16) + vptr] & 0xFF) + gshade] & 0xFF;
					frameplace[p + x] = gtrans[(frameplace[p + x] & 0xFF) + (dacol << 8)];
				}
				p += bpl;
				v += vi;
				dy--;
			}
			break;
		case 2:
			while (dy > 0) {
				for (x = 0; x < dx; x++) {
					dacol = palookup[gpal][(data[(v >>> 16) + vptr] & 0xFF) + gshade] & 0xFF;
					frameplace[p + x] = gtrans[((frameplace[p + x] & 0xFF) << 8) + dacol];
				}
				p += bpl;
				v += vi;
				dy--;
			}
			break;
		}
	}

	@Override
	public void drawpixel(int ptr, byte col) {
		frameplace[ptr] = col;
	}
}
