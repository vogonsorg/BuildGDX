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

import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.xdim;
import static ru.m210projects.Build.Engine.ydim;
import static ru.m210projects.Build.Gameutils.*;
import static ru.m210projects.Build.Strhandler.Bitoa;
import static ru.m210projects.Build.Strhandler.buildString;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Pattern.BuildFont;
import ru.m210projects.Build.Pattern.BuildFont.Align;
import ru.m210projects.Build.Pattern.BuildFont.TextAlign;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler.MenuOpt;

public class MenuSlider extends MenuItem
{
	public int value;
	public int min, max, step;
	public int background, slider;
	public MenuProc callback;
	public boolean digital;
	public float digitalMax;
	public char[] dbuff; 
	public boolean textShadow;
	
	private int touchX;
	private boolean isTouched;
	private MenuSlider touchedObj;
	protected Engine draw;
	
	public MenuSlider(Engine draw, Object text, BuildFont font, boolean textShadow, int x, int y, int width, int value, int min, int max, 
			int step, MenuProc callback, int background, int slider, boolean digital) 
	{
		super(text, font);
		
		this.flags = 3 | 4;
		this.x = x;
		this.y = y;
		this.width = width;
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = BClipRange(value, min, max);
		this.callback = callback;
		dbuff = new char[10];

		this.digital = digital;
		digitalMax = 0;
		if(background >= 0)
			this.background = background;
		if(slider >= 0)
			this.slider = slider;
		
		this.textShadow = textShadow;
		this.draw = draw;
	}
	
	@Override
	public void draw(MenuHandler handler) {
		int aly = font.getAlign(text).y;
		int shade = handler.getShade(m_pMenu.mGetFocusedItem(this));
		int pal = handler.getPal(font, (flags & 3) == 3);

		if ( text != null )
			font.drawText(x, y, text, shade, pal, TextAlign.Left, 0, textShadow);
		   
		int cx = x + width - 1 - tilesizx[background] / 2;
		draw.rotatesprite(cx << 16, (aly / 2 + y) << 16, 65536, 0, background, 0, pal, 10, 0, 0, xdim - 1, ydim - 1);
		if(digital)
		{
			if(digitalMax == 0)
				Bitoa(value, dbuff);
			else {
				String val = Float.toString(value / digitalMax);
				int index = val.indexOf('.');
				buildString(dbuff, 0, val);
				Arrays.fill(dbuff, index + 4, dbuff.length, (char)0);
			}

			font.drawText(x + width - tilesizx[background] - font.getAlign(dbuff).x - 10, y, dbuff, shade, pal, TextAlign.Left, 0, false);
		}
		
		int nRange = max - min;
		int xrange = tilesizx[background] - 8;
		int dx = xrange * (value - min) / nRange - xrange / 2;
		draw.rotatesprite(  (cx + dx) << 16, (aly / 2 + y) << 16, 65536, 0, slider, 0, pal, 10, 0, 0, xdim - 1, ydim - 1);
	}

	@Override
	public void open() {
		
	}
	
	@Override
	public void close() {
		
	}

	@Override
	public boolean mouseAction(int mx, int my) {
		touchX = mx;
		isTouched = false;

		if(!Gdx.input.isTouched() && touchedObj != this)
			touchedObj = null;
		
		if(text != null)
		{
			Align align = font.getAlign(text);
			if(mx > x && mx < x + align.x)
			{
				if(my > y && my < y + align.y) 
					return true;
			}
		}

		int cx = x + width - 1 - tilesizx[background];
		Align align = font.getAlign(null);
		if(mx > cx && mx < cx + tilesizx[background] )
			if(my > y && my < y + align.y) {
				isTouched = true;
				if(Gdx.input.isTouched())
					touchedObj = this;
			}
		
		return isTouched;
	}

	@Override
	public boolean callback(MenuHandler handler, MenuOpt opt) {
		int val;

		switch(opt) {
		case UP:
			m_pMenu.mNavUp();
			break;
		case DW:
			m_pMenu.mNavDown();
			break;
		case LEFT:
		case MWDW:
			if(value <= 0) {
				int dv = (value - step) % -step;
		        val = value - step - dv;
		        if ( dv < 0 )
		        	val += step;
			}
			else 
			{
		        int dv = (value - 1) % step;
		        val = value - 1 - dv;
		        if ( dv < 0 )
		        	val -= step;
			}
			value = BClipRange(val, min, max);
			if(callback != null) {
				callback.run(handler, this);
			}
			break;
		case RIGHT:
		case MWUP:
			if ( value < 0 )
		    {
		        int dv = (value - 1) % -step;
		        val = value - 1 - dv;
		        if ( dv < 0 )
		        	val += step;
		    } else
		    {
		        int dv = (value + step) % step;
		        val = value + step - dv;
		        if ( dv < 0 )
		        	val -= step;
		    }
			value = BClipRange(val, min, max);
			if(callback != null) 
				callback.run(handler, this);
			break;
		case ENTER:
			if(callback != null) 
				callback.run(handler, this);
			break;
		case LMB:
			if(touchedObj == this)
			{
				int x1 = x + width - tilesizx[background] + 2;
				float dr = (float)(touchX - x1) / (tilesizx[background] - 4);
				value = (int) BClipRange(dr * max, min, max);
				if(callback != null) 
					callback.run(handler, this);
			} 
		default:
			return m_pMenu.mNavigation(opt);
		}
		
		return false;
	}
}