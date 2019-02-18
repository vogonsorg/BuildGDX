
package ru.m210projects.Build.Types;

public class AtlasFont {

	public int charsizx;
	public int charsizy;
	public int atlas;
	public int cols, rows;
	
	/*
		for (int h = 0; h < 256; h++) {
			int row = (h / 16);
			int col = (h % 16);
			tptr = col * fwidth + row * sizx * fheight;
			for (i = 0; i < fwidth; i++) {
				for (j = 0; j < 8; j++) {
					if ((atlas[h * fheight + i] & pow2char[7 - j]) != 0)
						tbuf[tptr + j] = (byte) 255;
				}
				tptr += sizx;
			}	
		}
	*/
	
	public AtlasFont(int atlas, int charsizx, int charsizy, int cols, int rows)
	{
		this.atlas = atlas;
		this.charsizx = charsizx;
		this.charsizy = charsizy;
		this.cols = cols;
		this.rows = rows;
	}

	
}
