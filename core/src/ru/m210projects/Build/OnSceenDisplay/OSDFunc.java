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

package ru.m210projects.Build.OnSceenDisplay;

public interface OSDFunc {
	public void drawchar(int x, int y, char ch, int shade, int pal, int scale);
	public void drawosdstr(int x, int y, int ptr, int len, int shade, int pal, int scale);
	public void drawstr(int x, int y, char[] text, int len, int shade, int pal, int scale);
	public void drawcursor(int x, int y, int type, int lastkeypress, int scale);
	public void drawlogo(int daydim);
	
	public void clearbg(int col, int row);
	public void showosd(int shown);
	public int gettime();
	public long getticksfunc();
	public int getcolumnwidth(int width);
	public int getrowheight(int height);
	public boolean textHandler(String text);
}
