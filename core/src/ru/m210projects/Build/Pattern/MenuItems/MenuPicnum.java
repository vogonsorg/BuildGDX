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

import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuPicnum extends MenuItem
{
	public int nTile;
	public final int defTile;
	protected Engine draw;
	
	public MenuPicnum(Engine draw, Object text, BuildFont font, int x, int y, int nTile, int defTile) 
	{
		super(text, font);
		this.flags = 1;
		this.x = x;
		this.y = y;
		this.nTile = nTile;
		this.defTile = defTile;
		
		this.draw = draw;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		int shade = handler.getShade(m_pMenu.mGetFocusedItem(this));
		
		if ( text != null ) 
			font.drawText(x, y, text, shade, 0, TextAlign.Left, 0, false);

		int stat, picnum, ang;
		if(m_pMenu != null) {
			stat = 64 | 2 | 8;
			picnum = nTile;
		    ang = 0;
		} else {
			stat = 64 | 2 | 4 | 8; //70
			picnum = 0; //nextMenu->nTile
		    ang = 512;
		}

		draw.rotatesprite(100 << 16, 107 << 16, 0x8000, ang, picnum, 0, 0, stat, 0, 0, xdim - 1, ydim - 1);
	}

	@Override
	public boolean mouseAction(int x, int y) {
		return false;
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		return m_pMenu.mNavigation(opt);
	}

	@Override
	public void open() {}

	@Override
	public void close() {}
}