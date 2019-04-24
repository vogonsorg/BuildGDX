//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Pattern;

import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Pragmas.*;
import static ru.m210projects.Build.Strhandler.toCharArray;

import ru.m210projects.Build.Engine;

public class BuildFont {
	
	public byte[] atlas;

	public enum TextAlign { Left, Center, Right };

	public static final int nSpace = -2;

	protected class BuildChar {
		public int nTile = -1;
		public short nWidth;
		public short xOffset, yOffset;
	}

	protected int nHeight;
	protected int nScale;
	protected int nFlags;
	protected BuildChar[] charInfo;
	protected final Engine draw;
	
	protected BuildFont(Engine draw) {
		this.draw = draw;
		charInfo = new BuildChar[256];
		for (int i = 0; i < 256; i++)
			charInfo[i] = new BuildChar();
	}
	
	protected BuildFont(Engine draw, int nHeigth, int nScale, int nFlags) {
		this.draw = draw;
		this.nHeight = nHeigth;
		this.nScale = nScale;
		this.nFlags = nFlags;
		charInfo = new BuildChar[256];
		for (int i = 0; i < 256; i++)
			charInfo[i] = new BuildChar();
	}

	protected void addChar(char ch, int nTile, int nWidth, int nScale, int xOffset, int yOffset) {
		charInfo[ch].nTile = nTile;
		charInfo[ch].xOffset = (short) xOffset;
		charInfo[ch].yOffset = (short) yOffset;
		charInfo[ch].nWidth = (short) nWidth;
	}
	
	public int getWidth(char ch)
	{
		return charInfo[ch].nWidth;
	}

	public int getWidth(char[] text) {
		int width = 0;
	
		if (text != null) {
			int pos = 0;
			while (pos < text.length && text[pos] != 0) {
				if(text[pos] >= 256) {
					pos++;
					continue;
				}
				width += charInfo[text[pos++]].nWidth;
			}
		}

		return width;
	}
	
	public int getHeight()
	{
		return nHeight;
	}
	
	public int drawChar(int x, int y, char ch, int shade, int pal, int nBits, boolean shadow) {
		if(charInfo[ch].nTile == -1) return 0;
		
		if(charInfo[ch].nTile != nSpace && tilesizx[charInfo[ch].nTile] != 0 && tilesizy[charInfo[ch].nTile] != 0) {
			if(shadow)
				draw.rotatesprite((x + charInfo[ch].xOffset + 1) << 16, (y + charInfo[ch].yOffset + 1) << 16, nScale, 0, charInfo[ch].nTile, 127, 0, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
			draw.rotatesprite((x + charInfo[ch].xOffset) << 16, (y + charInfo[ch].yOffset) << 16, nScale, 0, charInfo[ch].nTile, shade, pal, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
		}
		return charInfo[ch].nWidth;
	}
	
	public int drawText(int x, int y, String text, int shade, int pal, TextAlign textAlign, int nBits, boolean shadow) {
		if(text == null) return 0;
		
		return drawText(x, y, toCharArray(text), shade, pal, textAlign, nBits, shadow);
	}
	
	public int drawText(int x, int y, char[] text, int shade, int pal, TextAlign textAlign, int nBits, boolean shadow) {
		if(text == null) return 0;
		
		if ( textAlign != TextAlign.Left )
		{
			int nWidth = getWidth(text);
			if ( textAlign == TextAlign.Center ) 
				nWidth >>= 1;
			x -= nWidth;
		}
		
		int alignx = 0;
		for(int i = 0; i < text.length && text[i] != 0; i++) {
			if(text[i] >= 256)
				continue;
			alignx += drawChar(x + alignx, y, text[i], shade, pal, nBits, shadow);
		}
		return alignx;
	}
	
	// Scale font
	
	public int getWidth(char ch, int scale)
	{
		int zoom = mulscale(0x10000, mulscale(scale, nScale, 16), 16);
		return  scale(charInfo[ch].nWidth, zoom, nScale);
	}
	
	public int getWidth(char[] text, int scale) {
		int width = 0;
	
		int zoom = mulscale(0x10000, mulscale(scale, nScale, 16), 16);
		
		if (text != null) {
			int pos = 0;
			while (pos < text.length && text[pos] != 0) {
				if(text[pos] >= 256) {
					pos++;
					continue;
				}
				width += scale(charInfo[text[pos++]].nWidth, zoom, nScale);
			}
		}

		return width;
	}
	
	public int getHeight(int scale)
	{
		int zoom = mulscale(0x10000, mulscale(scale, nScale, 16), 16);
		return scale(nHeight, zoom, nScale);
	}
	
	public int drawChar(int x, int y, char ch, int scale, int shade, int pal, int nBits, boolean shadow) {
		if(charInfo[ch].nTile == -1) return 0;

		int zoom = mulscale(0x10000, mulscale(scale, nScale, 16), 16);
		if(charInfo[ch].nTile != nSpace && tilesizx[charInfo[ch].nTile] != 0 && tilesizy[charInfo[ch].nTile] != 0) {
			if(shadow)
				draw.rotatesprite((x + charInfo[ch].xOffset + 1) << 16, (y + charInfo[ch].yOffset + 1) << 16, zoom, 0, charInfo[ch].nTile, 127, 0, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
			draw.rotatesprite((x + charInfo[ch].xOffset) << 16, (y + charInfo[ch].yOffset) << 16, zoom, 0, charInfo[ch].nTile, shade, pal, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
		}
		return scale(charInfo[ch].nWidth, zoom, nScale); 
	}
	
	public int drawText(int x, int y, char[] text, int scale, int shade, int pal, TextAlign textAlign, int nBits, boolean shadow) {
		if(text == null) return 0;
		
		if ( textAlign != TextAlign.Left )
		{
			int nWidth = getWidth(text, scale);
			if ( textAlign == TextAlign.Center ) 
				nWidth >>= 1;
			x -= nWidth;
		}
	
		int alignx = 0;
		for(int i = 0; i < text.length && text[i] != 0; i++) {
			if(text[i] >= 256)
				continue;
			alignx += drawChar(x + alignx, y, text[i], scale, shade, pal, nBits, shadow);
		}
		return alignx;
	}
	
	public int drawText(int x, int y, String text, int scale, int shade, int pal, TextAlign textAlign, int nBits, boolean shadow) {
		if(text == null) return 0;
		
		return drawText(x, y, toCharArray(text), scale, shade, pal, textAlign, nBits, shadow);
	}

}