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
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public abstract class MenuItem {

	public BuildMenu m_pMenu;
	public char[] text;          
	public BuildFont font;      
	public int x = 0;              
	public int y = 0;             
	public int width = 0;
	public int flags = 0;
	public int pal = 0;
	
	public MenuItem(Object text, BuildFont textStyle) {
		if(text != null) {
			if(text instanceof String) 
				this.text = ((String)text).toCharArray();
			else if(text instanceof char[])
				this.text = (char[]) text;
		}
		this.font = textStyle;
	}
	
	public void mCheckEnableItem(boolean nEnable) {
		if (nEnable) 
			flags = 3 | 4;
		else flags = 1;
	}

	public abstract void draw(MenuHandler handler);
	public abstract boolean callback(MenuHandler handler, MenuOpt opt);
	public abstract boolean mouseAction(int mx, int my);
	public abstract void open();
	public abstract void close();
	
}
