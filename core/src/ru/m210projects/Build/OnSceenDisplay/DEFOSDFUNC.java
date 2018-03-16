package ru.m210projects.Build.OnSceenDisplay;

import static ru.m210projects.Build.Engine.palette;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.OnSceenDisplay.Console.BGCTILE;
import static ru.m210projects.Build.OnSceenDisplay.Console.BGTILE;
import static ru.m210projects.Build.OnSceenDisplay.Console.BGTILE_SIZEX;
import static ru.m210projects.Build.OnSceenDisplay.Console.BGTILE_SIZEY;
import static ru.m210projects.Build.OnSceenDisplay.Console.BITS;
import static ru.m210projects.Build.OnSceenDisplay.Console.BITSTH;
import static ru.m210projects.Build.OnSceenDisplay.Console.BITSTL;
import static ru.m210projects.Build.OnSceenDisplay.Console.BORDTILE;
import static ru.m210projects.Build.OnSceenDisplay.Console.PALETTE;
import static ru.m210projects.Build.OnSceenDisplay.Console.SHADE;
import static ru.m210projects.Build.OnSceenDisplay.Console.osdkey;
import ru.m210projects.Build.Engine;

import com.badlogic.gdx.Input.Keys;

public class DEFOSDFUNC implements OSDFunc {
	
	private Engine engine;
	private int white = -1;
	public DEFOSDFUNC(Engine engine){
		this.engine = engine;
		
		// find the palette index closest to white
        int k = 0;
        for (int i = 0; i < 256; i+=3)
        {
            int j = (palette[3*i]&0xFF)+(palette[3*i+1]&0xFF)+(palette[3*i+2]&0xFF);
            if (j > k) { k = j; white = i; }
        }
        
        osdkey[0] = Keys.GRAVE;
	}

	@Override
	public void drawchar(int x, int y, char ch, int shade, int pal, int scale) {
		x = (x << 3) + 4;
		y = (y << 3);
		engine.printchar256(x, y, white, -1, ch, 0);
	}

	@Override
	public void drawosdstr(int x, int y, int ptr, int len, int shade, int pal, int scale) {
		char[][] osdtext = Console.getTextPtr();
		if (ptr >= 0 && ptr < osdtext.length) {
			char[] text = osdtext[ptr];
			engine.printext256(4+(x<<3),4+(y<<3), white, -1, text, 0);
		}
	}

	@Override
	public void drawstr(int x, int y, char[] text, int len, int shade, int pal, int scale) {
		engine.printext256(4+(x<<3),(y<<3), white, -1, text, 0);
	}
	
	@Override
	public void drawcursor(int x, int y, int type, int lastkeypress, int scale) {
		char ch = '_';
		if(type != 0)
			ch = '#';
		
		if ((lastkeypress & 0x40l) == 0) 
			engine.printchar256(4+(x<<3),(y<<3)+2, white, -1, ch, 0);
	}
	
	@Override
	public int gettime() {
		return Engine.totalclock;
	}

	@Override
	public long getticksfunc() {
		return engine.getticks();
	}

	@Override
	public void clearbg(int col, int row) {
		int x, y, xsiz, ysiz, tx2, ty2;
		int daydim, bits;

		bits = BITSTH;

		daydim = (row << 3) + 3;

		xsiz = tilesizx[BGTILE];
		ysiz = tilesizy[BGTILE];

		if (xsiz <= 0 || ysiz <= 0)
			return;

		tx2 = xdim / xsiz;
		ty2 = daydim / ysiz;

		for (x = tx2; x >= 0; x--)
			for (y = ty2; y >= 0; y--)
				engine.rotatesprite(x * xsiz << 16, y * ysiz << 16,
						65536, 0, BGTILE, SHADE, PALETTE, bits, 0,
						0, xdim, daydim);

		xsiz = tilesizx[BGCTILE];
		ysiz = tilesizy[BGCTILE];

		if (xsiz <= 0 || ysiz <= 0)
			return;

		engine.rotatesprite((xdim - xsiz) << 15,
				(daydim - ysiz) << 16, 65536, 0, BGCTILE,
				SHADE - 32, PALETTE, BITSTL, 0, 0, xdim, daydim);

		xsiz = tilesizy[BORDTILE];
		if (xsiz <= 0)
			return;

		tx2 = xdim / xsiz;
		ysiz = tilesizx[BORDTILE];

		for (x = tx2; x >= 0; x--)
			engine.rotatesprite(x * xsiz << 16, (daydim - 1) << 16,
					65536, 0, BORDTILE, SHADE + 12, PALETTE, BITS,
					0, 0, xdim, daydim + 1);
	}

	@Override
	public void showosd(int shown) {
		// fix for TCs like Layre which don't have the BGTILE for
		// some reason
		// most of this is copied from my dummytile stuff in defs.c
		if (tilesizx[BGTILE] == 0 || tilesizy[BGTILE] == 0)
			engine.allocatepermanenttile(BGTILE, BGTILE_SIZEX, BGTILE_SIZEY);
	}

	@Override
	public int getcolumnwidth(int width) {
		return width/8 - 3;
	}

	@Override
	public int getrowheight(int height) {
		return height/8;
	}

	@Override
	public boolean textHandler(String text) {
		return false;
	}
}
