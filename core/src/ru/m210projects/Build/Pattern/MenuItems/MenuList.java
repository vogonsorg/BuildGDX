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

import static ru.m210projects.Build.Engine.totalclock;

import java.util.List;

import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.Align;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuList extends MenuItem
{
	int l_nMin = 0;
	public int l_nFocus;
	int nListItems;
	int align;
	List<char[]> text;
	MenuProc specialCall;
	public BuildMenu nextMenu;
	int nItemHeight = 10;
	
	public MenuList(List<char[]> text, BuildFont font, int x, int y, int width,
			int align, int nItemHeight, BuildMenu nextMenu, MenuProc specialCall,
			int nListItems) {
		
		super(null, font);
		this.text = text;
		this.align = align;
		this.flags = 3;
		this.m_pMenu = null;
		this.x = x;
		this.y = y;
		this.width = width;
		this.nItemHeight = nItemHeight;
		this.nListItems = nListItems;
		this.nextMenu = nextMenu;
		this.specialCall = specialCall;
	}

	@Override
	public void draw(MenuHandler handler) {
		if(text.size() > 0) {
			Align ali = font.getAlign(null);
			int px = x, py = y;
			for(int i = l_nMin; i >= 0 && i < l_nMin + nListItems && i < text.size(); i++) {	
				int pal = 0;
				int shade = 32;
				if ( i == l_nFocus ) {
					if(m_pMenu.mGetFocusedItem(this))
						shade = 32 - (totalclock & 0x3F);
					else { shade = 0; pal = 8; }
				}
			    if(align == 1) 
			        px = width / 2 + x - font.getAlign(text.get(i)).x / 2;
			    if(align == 2) 
			        px = x + width - 1 - font.getAlign(text.get(i)).x;
			    font.drawText(px, py, text.get(i), shade, pal, TextAlign.Left, 0, false);
				py += ali.y + nItemHeight;
			}
		} else {
			int pal = 0;

			String text = "List is empty";
			Align ali = font.getAlign(text.toCharArray());
			int px = x, py = y;		
			if(align == 1) 
		        px = width / 2 + x - ali.x / 2;
		    if(align == 2) 
		        px = x + width - 1 - ali.x;   

		    int shade = handler.getShade(this);
		    font.drawText(px, py, text.toCharArray(), shade, pal, TextAlign.Left, 0, true);
		}
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		switch(opt)
		{
			case MWUP:
				if(l_nMin > 0)
					l_nMin--;
				return false;
			case MWDW:
				if(text != null)
					if(l_nMin < text.size() - nListItems)
						l_nMin++;
				return false;
			case UP:
				l_nFocus--;
				if(l_nFocus >= 0 && l_nFocus < l_nMin)
					if(l_nMin > 0) l_nMin--;
				if(l_nFocus < 0) {
					l_nFocus = text.size() - 1;
					l_nMin = text.size() - nListItems;
					if(l_nMin < 0) l_nMin = 0;
				}
				return false;
			case DW:
				l_nFocus++;
				if(l_nFocus >= l_nMin + nListItems && l_nFocus < text.size())
					l_nMin++;
				if(l_nFocus >= text.size()) {
					l_nFocus = 0;
					l_nMin = 0;
				}
				return false;
			case LEFT:
				m_pMenu.mNavUp();
				return false;
			case RIGHT:
				m_pMenu.mNavDown();
				return false;
			case ENTER:
			case LMB:
				if(text.size() > 0) {
					specialCall.run(handler, this);
					if ( nextMenu != null )
				    	handler.mOpen(nextMenu, -1);
				}
				return false;
			case ESC:
			case RMB:
				//l_nFocus = l_nMin = 0;
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		if(text.size() > 0) {
			Align ali = font.getAlign(null);
			int px = x, py = y;
			for(int i = l_nMin; i >= 0 && i < l_nMin + nListItems && i < text.size(); i++) {	
			    if(align == 1) {
			    	ali = font.getAlign(text.get(i));
			        px = width / 2 + x - ali.x / 2;
			    }
			    if(align == 2) {
			    	ali = font.getAlign(text.get(i));
			        px = x + width - 1 - ali.x;
			    }

			    if(mx > px && mx < px + ali.x)
					if(my > py && my < py + ali.y)
					{
						l_nFocus = i;
						return true;
					}
			    
				py += ali.y + nItemHeight;
			}
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
