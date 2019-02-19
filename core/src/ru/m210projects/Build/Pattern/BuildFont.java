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

import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;

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

	public final int nHeight;
	public final int nScale;
	public final int nFlags;
	public BuildChar[] charInfo;
	protected final Engine draw;

	public BuildFont(Engine draw, int nHeigth, int nScale, int nFlags) {
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

	public int getWidth(char[] text) {
		int width = 0;
	
		if (text != null) {
			int pos = 0;
			while (pos < text.length && text[pos] != 0) {
				width += charInfo[text[pos++]].nWidth;
			}
		}

		return width;
	}

	public int drawChar(int x, int y, char ch, int shade, int pal, int nBits, boolean shadow) {
		if(charInfo[ch].nTile == -1) return 0;
		
		if(charInfo[ch].nTile != nSpace) {
			if(shadow)
				draw.rotatesprite((x + charInfo[ch].xOffset + 1) << 16, (y + charInfo[ch].yOffset + 1) << 16, nScale, 0, charInfo[ch].nTile, 127, 0, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
			draw.rotatesprite((x + charInfo[ch].xOffset) << 16, (y + charInfo[ch].yOffset) << 16, nScale, 0, charInfo[ch].nTile, shade, pal, nFlags | nBits, 0, 0, xdim - 1, ydim - 1);
		}
		return charInfo[ch].nWidth;
	}

	public int drawText(int x, int y, char[] text, int shade, int pal, TextAlign align, int nBits, boolean shadow) {
		if(text == null) return 0;
		
		if ( align != TextAlign.Left )
		{
			int nWidth = getWidth(text);
			if ( align == TextAlign.Center ) 
				nWidth >>= 1;
			x -= nWidth;
		}
		
		int alignx = 0;
		for(int i = 0; i < text.length && text[i] != 0; i++)
			alignx += drawChar(x + alignx, y, text[i], shade, pal, nBits, shadow);
		return alignx;
	}

}
