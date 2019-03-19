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
import static ru.m210projects.Build.Gameutils.BClipLow;
import static ru.m210projects.Build.Gameutils.BClipRange;
import static ru.m210projects.Build.Pattern.BuildConfig.*;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildConfig.GameKeys;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public abstract class MenuKeyboardList extends MenuList
{
	public int l_set = 0; 
	public MenuOpt l_pressedId;
	protected KeyType[] keynames;
	protected BuildConfig cfg;
	
	protected SliderDrawable slider;
	protected int scrollerX, scrollerHeight;
	protected int touchY;
	protected boolean isTouched;
	protected int pal2;

	public MenuKeyboardList(SliderDrawable slider, BuildConfig cfg, BuildFont font, int x, int y, int width, int len, int list_pal_left, int list_pal_right, MenuProc callback)
	{
		super(null, font, x, y, width, 0, null, callback, len);
		this.slider = slider;
		this.cfg = cfg;
		this.keynames = cfg.keymap;
		this.len = keynames.length;
		this.pal = list_pal_left;
		this.pal2 = list_pal_right;
	}
	
	public abstract String getKeyName(int keycode);
	
	public int mFontOffset() {
		return font.getHeight() + 2;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		int px = x, py = y;
		for(int i = l_nMin; i >= 0 && i < l_nMin + nListItems && i < len; i++) {	
			int shade = handler.getShade(i == l_nFocus? m_pMenu.m_pItems[m_pMenu.m_nFocus] : null);
			int pal1 = this.pal; 
			int pal2 = this.pal2; 
			
			if(i == l_nFocus)
				pal2 = pal1 = handler.getPal(font, m_pMenu.m_pItems[m_pMenu.m_nFocus]);
			
			String text = keynames[i].getName();
			String key = getKeyName(cfg.primarykeys[i]);
		
			if(cfg.secondkeys[i] != 0)
				key += " or " + getKeyName(cfg.secondkeys[i]);

			if ( i == l_nFocus ) {
				if(l_set == 1 && (totalclock & 0x20) != 0)
					key = "____";
			}

			char[] k = key.toCharArray();
			font.drawText(px, py, text.toCharArray(), shade, pal1, TextAlign.Left, 2, fontShadow);

			font.drawText(x + width / 2 - 1 - font.getWidth(k) + 40, py, k, shade, pal2, TextAlign.Left, 2, fontShadow);		
			
			if(cfg.mousekeys[i] != 0)
				key = getKeyName(cfg.mousekeys[i]);
			else key = " - ";
			if ( i == l_nFocus ) {
				if(l_set == 1 && (totalclock & 0x20) != 0)
				{
					key = "____";
				}
			}
			k = key.toCharArray();
			font.drawText(x + width - slider.getScrollerWidth() - 2 - font.getWidth(k), py, k, shade, pal2, TextAlign.Left, 2, fontShadow);	
				
			py += mFontOffset();
		}
		
		scrollerHeight = nListItems * mFontOffset();

		//Files scroll
		int nList = BClipLow(len - nListItems, 1);
		int posy = y + (scrollerHeight - slider.getScrollerHeight()) * l_nMin / nList;
		
		scrollerX = x + width - slider.getScrollerWidth() + 5;
		slider.drawScrollerBackground(scrollerX, y, scrollerHeight, 0, 0);
		slider.drawScroller(scrollerX, posy, handler.getShade(isTouched ? m_pMenu.m_pItems[m_pMenu.m_nFocus] : null), 0);
		
		handler.mPostDraw(this);
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
				if(l_nMin < len - nListItems)
					l_nMin++;
				return false;
			case UP:
				l_nFocus--;
				if(l_nFocus >= 0 && l_nFocus < l_nMin)
					l_nMin--;
				if(l_nFocus < 0) {
					l_nFocus = len - 1;
					l_nMin = len - nListItems;
				}
				
				return false;
			case DW:
				l_nFocus++;
				if(l_nFocus >= l_nMin + nListItems && l_nFocus < len)
					l_nMin++;
				if(l_nFocus >= len) {
					l_nFocus = 0;
					l_nMin = 0;
				}
				return false;
			case ENTER:
			case LMB:
				if ( (flags & 4) == 0 ) return false;
				
				if(opt == MenuOpt.LMB && isTouched)
				{
					if(len <= nListItems)
						return false;

					int nList = BClipLow(len - nListItems, 1);
					int nRange = scrollerHeight;
					int py = y;

					l_nFocus = -1;
					l_nMin = BClipRange(((touchY - py) * nList) / nRange, 0, nList);
					
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
				
				if(l_nFocus == GameKeys.Show_Console.getNum()) {
					Console.setCaptureKey(cfg.primarykeys[l_nFocus], 0);
					Console.setCaptureKey(cfg.secondkeys[l_nFocus], 1);
					Console.setCaptureKey(cfg.mousekeys[l_nFocus], 2);
				}
				return false;
			case PGUP:
				l_nFocus -= (nListItems - 1);
				if(l_nFocus >= 0 && l_nFocus < l_nMin)
					if(l_nMin > 0) l_nMin -= (nListItems - 1);
				if(l_nFocus < 0 || l_nMin < 0) {
					l_nFocus = 0;
					l_nMin = 0;
				}
				return false;
			case PGDW:
				l_nFocus += (nListItems - 1);
				if(l_nFocus >= l_nMin + nListItems && l_nFocus < len)
					l_nMin += (nListItems - 1);
				if(l_nFocus >= len || l_nMin > len - nListItems) {
					l_nFocus = len - 1;
					if(len >= nListItems)
						l_nMin = len - nListItems;
					else l_nMin = len - 1;
				}
				return false;
			case HOME:
				l_nFocus = 0;
				l_nMin = 0;
				return false;
			case END:
				l_nFocus = len - 1;
				if(len >= nListItems)
					l_nMin = len - nListItems;
				else l_nMin = len - 1;
				return false;
			default:
				return m_pMenu.mNavigation(opt);
			}
		}
		else
		{
			l_pressedId = opt;
			if((flags & 4) != 0 && callback != null)
				callback.run(handler, this);
			
			if(l_nFocus == MenuKeys.Menu_Open.getNum()) 
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
			isTouched = false;
				
		touchY = my;
		if(mx > scrollerX && mx < scrollerX + slider.getScrollerWidth()) 
		{
			if(Gdx.input.isTouched())
				isTouched = true;
			else isTouched = false;
			return true;
		}

		if(!isTouched)
		{
			int py = y;
			for(int i = l_nMin; i >= 0 && i < l_nMin + nListItems && i < len; i++) {	
				if(my >= py && my < py + font.getHeight())
				{
					l_nFocus = i;
					return true;
				}
			    
				py += mFontOffset();
			}
		}

		return false;
	}
}
