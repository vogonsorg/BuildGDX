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

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Input.Keymap;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.Align;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuKeyboardList extends MenuItem
{
	public int len;
	public int l_nMin = 0;
	public int l_nFocus, nItems;
	public int l_set = 0; 
	public MenuOpt l_pressedId;
	public MenuProc callback;
	protected String[] keynames;
	protected BuildConfig cfg;
	
	protected int touchY;
	protected int scrollX, scrollY;
	protected boolean scrollTouch;

	public MenuKeyboardList(BuildConfig cfg, BuildFont font, int x, int y, int width, int len, MenuProc callback)
	{
		super(null, font);
		this.flags = 3;
		this.cfg = cfg;
		this.keynames = cfg.keynames;
		this.nItems = keynames.length;
		this.x = x;
		this.y = y;
		this.width = width;
		this.len = len;
		this.callback = callback;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		Align ali = font.getAlign(null);
		int px = x, py = y;
		for(int i = l_nMin; i >= 0 && i < l_nMin + nItems && i < len; i++) {	
			int shade = handler.getShade(null);
			String text = keynames[i];
			String key;
			
			if(cfg.primarykeys[i] != 0)
				key = Keymap.toString(cfg.primarykeys[i]);
			else key = "N/A";
			
			if(cfg.secondkeys[i] != 0)
				key += " or " + Keymap.toString(cfg.secondkeys[i]);

			if ( i == l_nFocus ) {
				if(l_set == 1 && (totalclock & 0x20) != 0)
					key = "____";
			}

			char[] k = key.toCharArray();
			font.drawText(px, py, text.toCharArray(), shade, 0, TextAlign.Left, 0, false);

			font.drawText(x + width - 1 - font.getAlign(k).x, py, k, shade, 0, TextAlign.Left, 0, false);		
			
			if(cfg.mousekeys[i] != 0)
				key = Keymap.toString(cfg.mousekeys[i]);
			else key = " - ";
			if ( i == l_nFocus ) {
				shade = handler.getShade(m_pMenu.m_pItems[m_pMenu.m_nFocus]);
				if(l_set == 1 && (totalclock & 0x20) != 0)
				{
					key = "____";
				}
			}
			k = key.toCharArray();
			font.drawText(x + width - 1 - font.getAlign(k).x + 60, py, k, shade, 0, TextAlign.Left, 0, false);	
				
			py += ali.y;
		}
		
		int nList = BClipLow(len - nItems, 1);
		int posy = (((nItems) * ali.y - 13)) * l_nMin / nList;

		scrollX = x + width + 65;
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
				if(l_nFocus == -1) return false;
				
				cfg.primarykeys[l_nFocus] = 0;
				cfg.secondkeys[l_nFocus] = 0;
				cfg.mousekeys[l_nFocus] = 0;
				
				if(l_nFocus == Show_Console) {
					Console.setCaptureKey(cfg.primarykeys[Show_Console], 0);
					Console.setCaptureKey(cfg.secondkeys[Show_Console], 1);
					Console.setCaptureKey(cfg.mousekeys[Show_Console], 2);
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
			
			if(l_nFocus == Menu_open) 
				getInput().resetKeyStatus();

			return false;
		}
	}
	
	@Override
	public void open() {
	}

	@Override
	public void close() {
	}
	
	@Override
	public boolean mouseAction(int mx, int my) {
		if(l_set != 0)
			return false;
		
		if(!Gdx.input.isTouched()) 
			scrollTouch= false;
		
		touchY = my;
		if(mx > scrollX && mx < scrollX + 14) 
		{
			if(Gdx.input.isTouched())
				scrollTouch = true;
			else scrollTouch = false;
			return true;
		}
		
		if(!scrollTouch) {
			Align align = font.getAlign(null);
			int py = y;
	
			for(int i = l_nMin; i >= 0 && i < l_nMin + nItems && i < len; i++) {	
				if(my > py && my < py + align.y)
				{
					l_nFocus = i;
					return true;
				}
			    
				py += align.y;
			}
		}
		
		return false;
	}
}
