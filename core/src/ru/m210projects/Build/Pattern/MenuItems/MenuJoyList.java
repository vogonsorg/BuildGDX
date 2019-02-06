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

import static ru.m210projects.Build.Engine.getInput;
import static ru.m210projects.Build.Engine.totalclock;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Pattern.BuildConfig.*;
import ru.m210projects.Build.Input.ButtonMap;
import ru.m210projects.Build.Input.GPManager;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.Align;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuJoyList extends MenuKeyboardList {

	private final GPManager gpmanager;
	private final int menupal;
	
	public MenuJoyList(GPManager gpmanager, BuildConfig cfg, int menupal, BuildFont font, int x, int y, int width,
			int len, MenuProc callback) {
		super(cfg, font, x, y, width, len, callback);
		this.menupal = menupal;
		this.gpmanager = gpmanager;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		Align align = font.getAlign(null);
		int px = x, py = y;
		for(int i = l_nMin; i >= 0 && i < l_nMin + nItems && i < len; i++) {	
			int pal = 0;
			int shade = handler.getShade(false);
			String text = keynames[i];
			String key;
			
			if(i == Move_Forward) {
				text = "Menu_up";
				pal = menupal;
			}
			if(i == Move_Backward) {
				text = "Menu_down";
				pal = menupal;
			}
			if(i == Turn_Left) {
				text = "Menu_left";
				pal = menupal;
			}
			if(i == Turn_Right) {
				text = "Menu_right";
				pal = menupal;
			}
			if(i == Turn_Around) 
				py += 4;
			
			if(i == Open) {
				text += " / Menu_enter";
				pal = menupal;
			}
			
			if(i == Menu_open) 
				pal = menupal;
			
			if(cfg.gpadkeys[i] >= 0)
				key = ButtonMap.buttonName(cfg.gpadkeys[i]);
			else key = "N/A";

			if ( i == l_nFocus ) {
				shade = handler.getShade(m_pMenu.mGetFocusedItem(this));
				if(l_set == 1 && (totalclock & 0x20) != 0)
				{
					key = "____";
				}
			}

			char[] k = key.toCharArray();
			
			font.drawText(px, py, text.toCharArray(), shade, pal, TextAlign.Left, 0, false);		
			font.drawText(x + width - 1 - font.getAlign(k).x, py, k, shade, 0, TextAlign.Left, 0, false);		
	
			py += align.y;
		}
		
		int nList = BClipLow(len - nItems, 1);
		int posy = (((nItems) * align.y - 13)) * l_nMin / nList;

		scrollX = x + width + 15;
		handler.mDrawSlider(scrollX, y, posy, 87, true);
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		if(l_set == 0) {
			switch(opt)
			{
			case MWUP:
				if(l_nMin > 0)
					l_nMin--;
				return false;
			case MWDW:
				if(l_nMin < len - nItems)
					l_nMin++;
				return false;
			case UP:
				l_nFocus--;
				if(l_nFocus >= 0 && l_nFocus < l_nMin)
					l_nMin--;
				if(l_nFocus < 0) {
					l_nFocus = len - 1;
					l_nMin = len - nItems;
				}
				
				return false;
			case DW:
				l_nFocus++;
				if(l_nFocus >= l_nMin + nItems && l_nFocus < len)
					l_nMin++;
				if(l_nFocus >= len) {
					l_nFocus = 0;
					l_nMin = 0;
				}
				return false;
			case ENTER:
			case LMB:
				if(opt == MenuOpt.LMB && scrollTouch)
				{
					l_nFocus = -1;
					int nList = BClipLow(len - nItems, 1);
					int nRange = nItems * font.getAlign(null).y - 13;
					int py = y + 4;
					float dr = (float)(touchY - py) / nRange;
					l_nMin = (int) BClipRange(dr * nList, 0, nList);
					
					return false;
				}
				if(l_nFocus != -1 && callback != null) 
					callback.run(handler, this);
				
				getInput().resetKeyStatus();
				return false;
			case DELETE:
				cfg.gpadkeys[l_nFocus] = -1;
				if(l_nFocus == Show_Console) {
					Console.setCaptureKey(cfg.gpadkeys[Show_Console], 3);
				}
				return false;
			default:
				return m_pMenu.mNavigation(opt);
			}
		}
		else
		{
			l_pressedId = opt;
			if(callback != null)
				callback.run(handler, this);

			if(l_nFocus == Menu_open 
					|| l_nFocus == Open
					|| l_nFocus == Move_Forward
					|| l_nFocus == Move_Backward
					|| l_nFocus == Turn_Left
					|| l_nFocus == Turn_Right) {
				gpmanager.resetButtonStatus();
			}

			return false;
		}
	}
}
