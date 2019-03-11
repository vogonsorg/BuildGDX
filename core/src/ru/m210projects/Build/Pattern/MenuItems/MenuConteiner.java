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

package ru.m210projects.Build.Pattern.MenuItems;

import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuConteiner extends MenuItem
{
	public int num;
	public MenuProc callback;
	public char[][] list;
	public BuildFont listFont;
	
	public MenuConteiner(Object text, BuildFont font, int x, int y, int width, String[] list, int num, MenuProc callback)
	{
		super(text, font);
		this.listFont = font;
		this.flags = 3 | 4;
		if(list != null)
		{
			this.list = new char[list.length][];
			for(int i = 0; i < list.length; i++)
				this.list[i] = list[i].toCharArray();
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.callback = callback;
		this.num = num;
		this.pal = 0;
	}
	
	public MenuConteiner(Object text, BuildFont font, BuildFont listFont, int x, int y, int width, String[] list, int num, MenuProc callback)
	{
		this(text, font, x, y, width, list, num, callback);
		this.listFont = listFont;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		int px = x, py = y;
		
		char[] key = null;
		if(list != null && num != -1 && num < list.length) 
			key = list[num];	

		int pal = handler.getPal(font, this);
		int shade = handler.getShade(this);
		font.drawText(px, py, text, shade, pal, TextAlign.Left, 2, false);
		
		if(key == null) return;

		listFont.drawText(x + width - 1 - listFont.getWidth(key), py, key, shade, handler.getPal(listFont, this), TextAlign.Left, 2, false);
		
		handler.mPostDraw(this);
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		
		switch(opt)
		{
		case LEFT:
		case MWDW:
			if ( (flags & 4) == 0 ) return false;
			if(num > 0) num--;
			else num = 0;
			if(callback != null)
				callback.run(handler, this);
			return false;
		case RIGHT:
		case MWUP:
			if ( (flags & 4) == 0 ) return false;
			if(num < list.length - 1) num++;
			else num = list.length - 1;
			if(callback != null)
				callback.run(handler, this);
			return false;
		case ENTER:
		case LMB:
			if ( (flags & 4) == 0 ) return false;
			if(num < list.length - 1) {
				num++;
			} else num = 0;
			if(callback != null)
				callback.run(handler, this);
			return false;
		default:
			return m_pMenu.mNavigation(opt);
		}
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		if(text != null)
		{
			if(mx > x && mx < x + font.getWidth(text))
				if(my > y && my < y + font.nHeight)
					return true;
		}
		
		if(list == null) return false;
		char[] key = null;
		if(num != -1 && num < list.length) {
			key = list[num];
			int fontx =  listFont.getWidth(key);
			int px = x + width - 1 - fontx;
			if(mx > px && mx < px + fontx)
				if(my > y && my < y + font.nHeight)
					return true;
		}
		
		return false;
	}

	@Override
	public void open() {
	}

	@Override
	public void close() {
	}	
}
